/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import com.mysql.cj.jdbc.Blob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.InflaterInputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import server.VideoCallServer;

/**
 *
 * @author Maryam_Otunara
 */
public class ClientThread implements Runnable {

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    Client clientFrame;
    StringTokenizer token;
    Login loginform;
    registration registerform;
    modify_profile modifyform;
    String username, callee;
    MessageAlert alert;

    final int BUFFER_SIZE = 100;

    public ClientThread(Socket socket, Client ClientFrame) {
        this.clientFrame = ClientFrame;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {

        }
    }

    public ClientThread(Socket socket, Client ClientFrame, Login loginform) {
        this.clientFrame = ClientFrame;
        this.loginform = loginform;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {

        }
    }

    public ClientThread(Socket socket, Client ClientFrame, registration registerform) {
        this.clientFrame = ClientFrame;
        this.registerform = registerform;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {

        }
    }

    public ClientThread(Socket socket, Client ClientFrame, modify_profile modifyform) {
        this.clientFrame = ClientFrame;
        this.modifyform = modifyform;
        this.socket = socket;
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                token = new StringTokenizer(data);
                /**
                 * Get Message CMD *
                 */
                String CMD = token.nextToken();

                switch (CMD) {

                    //  This will inform the client that there's a file receive, Accept or Reject the file  
                    case "CMD_FILE_XD":  // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                        String sender = token.nextToken();
                        String receiver = token.nextToken();
                        String fname = token.nextToken();
                        int confirm = JOptionPane.showConfirmDialog(clientFrame,
                                "From: " + sender + "\nFilename: " + fname + "\nwould you like to Accept.?");
                        alert = new MessageAlert();
                        alert.FileMsg();
                        if (confirm == 0) {

                            clientFrame.openFolder();
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                // Format:  CMD_SEND_FILE_ACCEPT [ToSender] [Message]
                                String format = "CMD_SEND_FILE_ACCEPT " + sender + " accepted";
                                dos.writeUTF(format);

                                /*  this will create a filesharing socket to handle incoming file
                                and this socket will automatically closed when it's done.  */
                                Socket fSoc = new Socket(clientFrame.getMyHost(), clientFrame.getMyPort());
                                DataOutputStream fdos = new DataOutputStream(fSoc.getOutputStream());
                                fdos.writeUTF("CMD_SHARINGSOCKET " + clientFrame.getMyUsername());
                                /*  Run Thread for this   */
                                new Thread(new ReceivingFileThread(fSoc, clientFrame)).start();
                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        } else { // client rejected the request, then send back result to sender
                            try {
                                dos = new DataOutputStream(socket.getOutputStream());
                                // Format:  CMD_SEND_FILE_ERROR [ToSender] [Message]
                                String format = "CMD_SEND_FILE_ERROR " + sender + " Client rejected your request or connection was lost.!";
                                dos.writeUTF(format);
                            } catch (IOException e) {
                                System.out.println("[CMD_FILE_XD]: " + e.getMessage());
                            }
                        }
                        break;

                    case "CMD_LOGINCONFIRMED":
                        username = token.nextToken();
                        String socket = token.nextToken();
                        clientFrame.setVisible(true);
                        clientFrame.username = username;
                        clientFrame.socket = this.socket;
                        clientFrame.setTitle(username);
                        loginform.dispose();
                        JOptionPane.showMessageDialog(clientFrame, "Welcome " + username);
                        break;

                    case "CMD_NOTIFYUSERLOGIN":
                        username = token.nextToken();
                        String notificationMessage = "User " + username + " has logged in.";
                        JOptionPane.showMessageDialog(clientFrame, notificationMessage, "User Logged In", JOptionPane.INFORMATION_MESSAGE);
                        break;

                    case "CMD_LOGINNOTCONFIRMED":
                        JOptionPane.showMessageDialog(loginform, "Wrong Login Details");
                        break;
                    case "CMD_REGISTERCONFIRMED":
                        /*username = token.nextToken();
                        socket = token.nextToken();
                        clientFrame.setVisible(true);
                        clientFrame.username = username;
                        clientFrame.socket = this.socket;
                        clientFrame.setTitle(username);
                        registerform.dispose();
                        JOptionPane.showMessageDialog(clientFrame, "Welcome " + username);*/

                        JOptionPane.showMessageDialog(registerform, "Registration Successful");

                        break;

                    case "CMD_REGISTRATIONNOTCONFIRMED":
                        JOptionPane.showMessageDialog(registerform, "Issue with Registration");
                        break;

                    case "CMD_ONLINE":
                        Vector online = new Vector();
                        while (token.hasMoreTokens()) {
                            String list = token.nextToken();

                            if (!list.equalsIgnoreCase(clientFrame.username)) {

                                online.add(list);

                            }
                        }
                        System.out.println(online);

                        clientFrame.appendOnlineList(online);
                        break;

                    case "CMD_CHAT":

                        String msgs = "";
                        String from = token.nextToken();
                        while (token.hasMoreTokens()) {
                            msgs = msgs + " " + token.nextToken();
                        }

                        String active = clientFrame.jLabel2.getText();
                        // JOptionPane.showMessageDialog(clientFrame, "Host " + clientFrame.username + " from" + from + " active" + active);
                        if (active.equalsIgnoreCase(from)) {
                            alert = new MessageAlert();
                            alert.TextMsg();
                            clientFrame.appendMessage("\n" + from + ": " + msgs);
                        }
                        break;

                    case "CMD_FETCHMSG":

                        String msg = "";

                        while (token.hasMoreTokens()) {
                            msg = msg + " " + token.nextToken();
                        }
                        clientFrame.appendMessage(msg + "\n");

                        break;

                    case "CMD_NOTIFICATION":

                        msg = "";

                        while (token.hasMoreTokens()) {
                            msg = msg + " " + token.nextToken();
                        }
                        clientFrame.appendNotification(msg + "\n");

                        break;

                    case "CMD_MODIFYCONFIRMED":
                        JOptionPane.showMessageDialog(registerform, "Changing Profile Successful");
                        break;
                    case "CMD_MODIFYNOTCONFIRMED":
                        JOptionPane.showMessageDialog(registerform, "Issue with Changing Profile");
                        break;

                    /*case "CMD_ALLUSERS":
                        Vector<String> allUsers = new Vector<>();
                        
                        while (token.hasMoreTokens()) {
                            String user = token.nextToken();
                            allUsers.add(user);
                        }
                        
                        System.out.println(allUsers);
                        clientFrame.appendUserList(allUsers);
                        break;*/
                    case "CMD_ALLUSERS":
                        Vector<String> allUsers = new Vector<>();

                        while (token.hasMoreTokens()) {
                            String user = token.nextToken();
                            allUsers.add(user);
                        }

                        System.out.println(allUsers);
                        clientFrame.appendUserList(allUsers);
                        break;

                    case "CMD_USERDETAILSFOUND":
                        String username = token.nextToken();
                        String sname = token.nextToken();
                        String oname = token.nextToken();
                        String email = token.nextToken();
                        String phoneno = token.nextToken();
                        String password = token.nextToken();
                        String photopath = token.nextToken();

                        /*String userDetails = String.format("User: %s\nSurname: %s\nOthername: %s\nEmail: %s\nPhone: %s\nPassword: %s\nPhoto: %s",
                                username, sname, oname, email, phoneno, password, photopath);

                        JOptionPane.showMessageDialog(modifyform, userDetails);*/
                        modifyform.displayDetails(username, sname, oname, email, phoneno, password, photopath);
                        break;

                    case "CMD_INCOMINGCALL":

                        from = token.nextToken();
                        msg = "Incoming call from " + from;
                        int option = JOptionPane.showConfirmDialog(clientFrame, msg + "\nWould you like to answer?", "Incoming Call", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        alert = new MessageAlert();
                        alert.VoiceCall();

                        try {
                            dos = new DataOutputStream(this.socket.getOutputStream());
                            if (option == JOptionPane.YES_OPTION) {

                                // User chose to answer the call
                                String response = "CMD_ANSWERCALL " + from + " accepted";
                                dos.writeUTF(response);

                                // Implement additional logic to handle answering the call
                                clientFrame.appendMessage("\nYou answered the call from " + from);
                                System.out.println("You answered the call from " + from);
                            } else {

                                // User chose to decline the call
                                String response = "CMD_DECLINECALL " + from + " declined";
                                dos.writeUTF(response);

                                // Implement additional logic to handle declining the call
                                clientFrame.appendMessage("\nYou declined the call from " + from);
                                System.out.println("You declined the call from " + from);
                            }
                        } catch (IOException e) {
                            System.out.println("[CMD_INCOMINGCALL]: " + e.getMessage());
                        }

                        break;

                    /*case "CMD_CALLACCEPTED":
                        callee = token.nextToken();
                        clientFrame.appendMessage("Call accepted by " + callee);
                        // Open voice call panel for communication
                        AudioCall audioCall = new AudioCall();
                        audioCall.initCall(callee); // Assuming callee's host address is passed here
                        audioCall.setVisible(true);
                        break;*/
                    case "CMD_CALLACCEPTED":
                        callee = token.nextToken();
                        String calleeHostAddress = token.nextToken();
                        clientFrame.appendMessage("Call accepted by " + callee + " (" + calleeHostAddress + ")");
                        // Open voice call panel for communication
                        AudioCall audioCall = new AudioCall();
                        audioCall.initAudioCall(calleeHostAddress); // Pass the callee's host address
                        audioCall.setVisible(true);
                        break;

                    case "CMD_CALLREJECTED":
                        callee = token.nextToken();
                        clientFrame.appendMessage("Call rejected by " + callee);
                        // Handle call rejection, possibly close the voice call stream if needed
                        JOptionPane.showMessageDialog(clientFrame, "Call rejected by " + callee, "Call Rejected", JOptionPane.INFORMATION_MESSAGE);
                        break;

                    case "CMD_CALLREQUESTSENT":
                        String to = token.nextToken();
                        msg = "Call request sent to " + to;
                        JOptionPane.showMessageDialog(clientFrame, msg);
                        // Implement additional logic to handle call request sent confirmation
                        System.out.println(msg);
                        break;

                    case "CMD_CALLREQUESTERROR":
                        String errorMsg = "";
                        while (token.hasMoreTokens()) {
                            errorMsg += " " + token.nextToken();
                        }
                        JOptionPane.showMessageDialog(clientFrame, "Call request error: " + errorMsg);
                        // Implement additional logic to handle call request error
                        System.err.println("Call request error: " + errorMsg);
                        break;

                    case "CMD_CALLEENOTONLINE":
                        callee = token.nextToken();
                        msg = "The user " + callee + " is not online.";
                        JOptionPane.showMessageDialog(clientFrame, msg);
                        // Implement additional logic to handle callee not online situation
                        System.out.println(msg);
                        break;

                    case "CMD_INCOMINGVIDEOCALL":

                        from = token.nextToken();
                        msg = "Incoming call from " + from;
                        option = JOptionPane.showConfirmDialog(clientFrame, msg + "\nWould you like to answer?", "Incoming Call", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        alert = new MessageAlert();
                        alert.VideoCall();

                        try {
                            dos = new DataOutputStream(this.socket.getOutputStream());
                            if (option == JOptionPane.YES_OPTION) {

                                // User chose to answer the call
                                String response = "CMD_ANSWERVIDEOCALL " + from + " accepted";
                                dos.writeUTF(response);

                                // Implement additional logic to handle answering the call
                                clientFrame.appendMessage("\nYou answered the call from " + from);
                                System.out.println("You answered the call from " + from);
                            } else {

                                // User chose to decline the call
                                String response = "CMD_DECLINEVIDEOCALL " + from + " declined";
                                dos.writeUTF(response);

                                // Implement additional logic to handle declining the call
                                clientFrame.appendMessage("\nYou declined the call from " + from);
                                System.out.println("You declined the call from " + from);
                            }
                        } catch (IOException e) {
                            System.out.println("[CMD_INCOMINGVIDEOCALL]: " + e.getMessage());
                        }

                        break;
//                    case "CMD_INCOMINGVIDEOCALL":
//                        from = token.nextToken();
//                        msg = "Incoming video call from " + from;
//                        option = JOptionPane.showConfirmDialog(clientFrame, msg + "\nWould you like to answer?", "Incoming Video Call", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//
//                        try {
//                            DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
//                            if (option == JOptionPane.YES_OPTION) {
//                                // User chose to answer the call
//                                String response = "CMD_ANSWERVIDEOCALL " + from + " accepted";
//                                dos.writeUTF(response);
//
//                                // Open video call interface for callee
//                                SwingUtilities.invokeLater(() -> {
//                                    VideoCall videoCall = new VideoCall();
//                                    videoCall.initVideoCall(""); // Pass necessary parameters if needed
//                                    videoCall.setVisible(true);
//                                });
//
//                                clientFrame.appendMessage("\nYou answered the call from " + from);
//                                System.out.println("You answered the call from " + from);
//                            } else {
//                                // User chose to decline the call
//                                String response = "CMD_DECLINEVIDEOCALL " + from + " declined";
//                                dos.writeUTF(response);
//
//                                clientFrame.appendMessage("\nYou declined the call from " + from);
//                                System.out.println("You declined the call from " + from);
//                            }
//                        } catch (IOException e) {
//                            System.out.println("[CMD_INCOMINGVIDEOCALL]: " + e.getMessage());
//                        }
//                        break;

                    case "CMD_VIDEOCALLACCEPTED":
                        callee = token.nextToken();
                        calleeHostAddress = token.nextToken();
                        String localhost = null;
                        clientFrame.appendMessage("Call accepted by " + callee + " (" + calleeHostAddress + ")");
                        // Open video call panel for communication
                        VideoCall videoCall = new VideoCall();
                        videoCall.initVideoCall(localhost); // Pass the callee's host address
                        videoCall.setVisible(true);

                        // Open video call server panel for communication
                        VideoCallServer videoCallServer = new VideoCallServer();
                        videoCallServer.initVideoCallServer();
                        videoCallServer.setVisible(true);
                        break;

                    case "CMD_VIDEOCALLREJECTED":
                        callee = token.nextToken();
                        clientFrame.appendMessage("Call rejected by " + callee);
                        // Handle call rejection, possibly close the voice call stream if needed
                        JOptionPane.showMessageDialog(clientFrame, "Call rejected by " + callee, "Call Rejected", JOptionPane.INFORMATION_MESSAGE);
                        break;

                    case "CMD_VIDEOCALLREQUESTSENT":
                        to = token.nextToken();
                        msg = "Call request sent to " + to;
                        JOptionPane.showMessageDialog(clientFrame, msg);
                        // Implement additional logic to handle call request sent confirmation
                        System.out.println(msg);
                        break;

                    case "CMD_VIDEOCALLREQUESTERROR":
                        errorMsg = "";
                        while (token.hasMoreTokens()) {
                            errorMsg += " " + token.nextToken();
                        }
                        JOptionPane.showMessageDialog(clientFrame, "Call request error: " + errorMsg);
                        // Implement additional logic to handle call request error
                        System.err.println("Call request error: " + errorMsg);
                        break;

                    case "CMD_VIDEOCALLEENOTONLINE":
                        callee = token.nextToken();
                        msg = "The user " + callee + " is not online.";
                        JOptionPane.showMessageDialog(clientFrame, msg);
                        // Implement additional logic to handle callee not online situation
                        System.out.println(msg);
                        break;

                    default:
                        clientFrame.appendMessage("[CMDException]: Unknown Command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
