/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import client.modify_profile;
import java.sql.Blob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringTokenizer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */
public class SocketThread implements Runnable {

    Socket socket, calleeSocket;
    Server serverform;
    DataInputStream dis;
    DataOutputStream dos;
    StringTokenizer st;
    dbconnection con;
    PreparedStatement ps;
    ResultSet rs;
    String client, oname, sname, username, email, phone, password, filesharing_username, photo_path, caller, callee;
    byte[] photo;

    final int BUFFER_SIZE = 100;

    public SocketThread(Socket socket, Server serverform) {
        this.serverform = serverform;
        this.socket = socket;

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createConnection(String receiver, String sender, String filename) {
        try {

            Socket s = serverform.getClientList(receiver);
            if (s != null) {
                DataOutputStream dosS = new DataOutputStream(s.getOutputStream());
                // Format:  CMD_FILE_XD [sender] [receiver] [filename]
                String format = "CMD_FILE_XD " + sender + " " + receiver + " " + filename;
                dosS.writeUTF(format);

            } else {

                serverform.appendMessage("Client was not found '" + receiver + "'");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + receiver + "' was not found in the list, make sure it is on the online list.!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                serverform.appendMessage(data + " connected \n");
                /**
                 * Check COMMAND *
                 */
                switch (CMD) {
                    case "CMD_LOGIN":
                        /**
                         * CMD_LOGIN [Username] [PASSWORD]*
                         */
                        username = st.nextToken();
                        password = st.nextToken();
                        client = username;
                        try {
                            DataOutputStream dos;
                            con = new dbconnection();
                            ps = con.psStatement("Select * from userinfo where username=? and password=? ");
                            ps.setString(1, username);
                            ps.setString(2, password);
                            rs = ps.executeQuery();

                            if (rs.next()) {

                                serverform.appendMessage(username + " connected \n");

                                dos = new DataOutputStream(socket.getOutputStream());
                                dos.writeUTF("CMD_LOGINCONFIRMED " + username + " " + socket);
                                dos.flush();

                                /*// Notify all other connected clients
                                String notificationMessage = "User " + username + " has logged in.";
                                for (Object client : serverform.clientList) {
                                    if (!client.equals(username)) {
                                        // Get the socket of the other clients and send the notification
                                        Socket clientSocket = getClientSocket(client);
                                        DataOutputStream clientDos = new DataOutputStream(clientSocket.getOutputStream());
                                        clientDos.writeUTF("CMD_NOTIFYUSERLOGIN " + client);
                                        clientDos.flush();
                                    }
                                }*/

                                serverform.setClientList(username);
                                serverform.setSocketList(socket);
                                serverform.appendMessage("" + username + " logged in successfully");
                            } else {
                                dos = new DataOutputStream(socket.getOutputStream());
                                dos.writeUTF("CMD_LOGINNOTCONFIRMED " + username);
                                dos.flush();
                            }
                        } catch (Exception e) {
                            serverform.appendMessage("Error when trying to log in" + username + e);
                        }

                        break;

                    case "CMD_REGISTER":
                        /**
                         * CMD_REGISTER [SNAME] [ONAME] [EMAIL] [PHONE]
                         * [USERNAME] [PASSWORD]*
                         */
                        //DataOutputStream dos = null;
                        try {
                        sname = st.nextToken();
                        oname = st.nextToken();
                        email = st.nextToken();
                        phone = st.nextToken();
                        username = st.nextToken();
                        password = st.nextToken();
                        photo_path = st.nextToken();
                        client = username;

                        /*int length = dis.readInt();
                            byte[] p = new byte[length];
                            dis.readFully(p);*/
                        File defaultPhotoFile = new File(photo_path);
                        photo = Files.readAllBytes(defaultPhotoFile.toPath());

                        con = new dbconnection();
                        PreparedStatement ps = con.psStatement("insert into userinfo values (?,?,?,?,?,?,?,?) ");
                        ps.setString(1, sname);
                        ps.setString(2, oname);
                        ps.setString(3, email);
                        ps.setString(4, phone);
                        ps.setString(5, username);
                        ps.setString(6, password);
                        ps.setBytes(7, photo);
                        ps.setString(8, photo_path);

                        int rs = ps.executeUpdate();
                        serverform.appendMessage(username + " connected \n");

                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("CMD_REGISTERCONFIRMED " + username);
                        dos.flush();
                        serverform.appendMessage("" + username + " REGISTERED  successfully");

                    } catch (Exception e) {
                        dos.writeUTF("CMD_REGISTRATIONNOTCONFIRMED " + username);
                        serverform.appendMessage("Error when trying to register" + username + e);
                    }

                    break;

                    //case "CMD_CHAT":
                    /**
                     * CMD_CHAT [from] [sendTo] [message] *
                     */
                    /*String from = st.nextToken();
                        String sendTo = st.nextToken();
                        String msg = "";
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        Socket tsoc = serverform.getClientList(sendTo);
                        try {
                            dos = new DataOutputStream(tsoc.getOutputStream());
                            String content = from + " " + msg;
                            dos.writeUTF("CMD_CHAT " + content);
                            // Now we need to store this message in the database
                            try {
                                con = new dbconnection();
                                // table format sender, reciever, msg, timestamp, status
                                PreparedStatement ps = con.psStatement("insert into messages values (?,?,?,?,?)");
                                ps.setString(1, from);
                                ps.setString(2, sendTo);
                                ps.setString(3, msg);
                                // to get timestamp
                                LocalDateTime now = LocalDateTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String Timestamp = now.format(formatter);
                                ps.setString(4, Timestamp);
                                ps.setString(5, "Not Read");
                                int rs = ps.executeUpdate();
                                con.close();
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(serverform, e);
                            }

                            serverform.appendMessage("Message: From " + from + " To " + sendTo + " : " + msg);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, e);

                        }
                        break;*/
                    case "CMD_CHAT":
                        /**
                         * CMD_CHAT [from] [sendTo] [message] *
                         */
                        String from = st.nextToken();
                        String sendTo = st.nextToken();
                        String msg = "";
                        while (st.hasMoreTokens()) {
                            msg = msg + " " + st.nextToken();
                        }
                        Socket tsoc = serverform.getClientList(sendTo);

                        // Now we need to store this message in the database
                        try {
                            con = new dbconnection();
                            // table format sender, receiver, msg, timestamp, status
                            PreparedStatement ps = con.psStatement("insert into messages values (?,?,?,?,?)");
                            ps.setString(1, from);
                            ps.setString(2, sendTo);
                            ps.setString(3, msg);
                            // to get timestamp
                            LocalDateTime now = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            String Timestamp = now.format(formatter);
                            ps.setString(4, Timestamp);
                            ps.setString(5, "Not Read");
                            int rs = ps.executeUpdate();
                            con.close();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(serverform, e);
                        }

                        if (tsoc != null) {
                            try {
                                dos = new DataOutputStream(tsoc.getOutputStream());
                                String content = from + " " + msg;
                                dos.writeUTF("CMD_CHAT " + content);
                                serverform.appendMessage("Message: From " + from + " To " + sendTo + " : " + msg);
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, e);
                            }
                        } else {
                            serverform.appendMessage("Message saved to database. Receiver '" + sendTo + "' is offline.");
                        }
                        break;

                    case "CMD_GETMESSAGES":

                        /**
                         * CMD_GETMESSAGES [from] [users]*
                         */
                        String with = st.nextToken();
                        String me = st.nextToken();
                        Socket mysocket = serverform.getClientList(me);

                        try {
                            dos = new DataOutputStream(mysocket.getOutputStream());

                            // Now we need to store this message in the database
                            con = new dbconnection();
                            // table format sender, reciever, msg, timestamp, status
                            ps = con.psStatement("select * from messages  where sender=? and receiver=? union "
                                    + "select * from messages  where sender=? and receiver=? order by timestamp");
                            ps.setString(1, me);
                            ps.setString(2, with);
                            ps.setString(3, with);
                            ps.setString(4, me);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                String sender = rs.getString(1);
                                String message = rs.getString(3);
                                String time = rs.getString(4);
                                if (sender.equalsIgnoreCase(me)) {
                                    dos.writeUTF("CMD_FETCHMSG " + me + ":" + message + " :" + time);
                                } else {
                                    dos.writeUTF("CMD_FETCHMSG " + with + ":" + message + " :" + time);
                                }

                            }

                            // Here you can now modify status from Not Read to Read
                            ps = con.psStatement("update messages  set status=? where sender=? and receiver=?");
                            ps.setString(1, "Read");
                            ps.setString(2, with);
                            ps.setString(3, me);
                            int rs = ps.executeUpdate();

                            con.close();

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(serverform, e);
                        }

                        serverform.appendMessage("Retrieve messages between " + with + " and " + me);

                        break;

                    case "CMD_NOTIFICATION":

                        /**
                         * CMD_GETMESSAGES [from] [users]*
                         */
                        String friend = st.nextToken();
                        String myusername = st.nextToken();
                        Socket Mysocket = serverform.getClientList(myusername);
                        int count = 0;

                        try {
                            dos = new DataOutputStream(Mysocket.getOutputStream());
                            con = new dbconnection();

                            ps = con.psStatement("SELECT count(*) FROM messages where sender=? and receiver=? and status=?");
                            ps.setString(1, friend);
                            ps.setString(2, myusername);
                            ps.setString(3, "Not Read");
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                count = rs.getInt(1);
                            }

                            dos.writeUTF("CMD_NOTIFICATION You have " + count + " Unread message(s) from " + friend);

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(serverform, e);
                        }

                        serverform.appendMessage("Notification for " + friend + " and " + myusername);

                        break;

                    /*case "CMD_GETALLUSERS":
                        try {
                        con = new dbconnection();
                        ps = con.psStatement("SELECT username FROM userinfo");
                        rs = ps.executeQuery();

                        dos = new DataOutputStream(socket.getOutputStream());

                        // Using StringBuilder to collect usernames
                        StringBuilder userList = new StringBuilder();
                        while (rs.next()) {
                            String username = rs.getString("username");
                            //serverform.setUserList(username);
                            userList.append(username).append(" ");
                        }
                        
                        dos.writeUTF("CMD_ALLUSERS " + userList.toString().trim());
                    } catch (Exception e) {
                        serverform.appendMessage("Error retrieving users: " + e.getMessage());
                    }
                    break;*/
                    case "CMD_GETALLUSERS":
                    try {
                        con = new dbconnection();
                        ps = con.psStatement("SELECT username FROM userinfo");
                        rs = ps.executeQuery();

                        dos = new DataOutputStream(socket.getOutputStream());
                        StringBuilder users = new StringBuilder("CMD_ALLUSERS");
                        while (rs.next()) {
                            String username = rs.getString("username");
                            //serverform.setUserList(username);
                            users.append(" ").append(username);
                        }
                        dos.writeUTF(users.toString());
                    } catch (Exception e) {
                        serverform.appendMessage("Error retrieving users: " + e.getMessage());
                    }
                    break;

                    case "CMD_GETUSERDETAILS":
                        String myUsername = st.nextToken();
                        con = new dbconnection();
                        try {
                            PreparedStatement ps = con.psStatement("SELECT * FROM userinfo WHERE username = ?");
                            ps.setString(1, myUsername);
                            ResultSet rs = ps.executeQuery();

                            if (rs.next()) {
                                String surname = rs.getString("surname");
                                String othername = rs.getString("othername");
                                String email = rs.getString("email");
                                String phoneno = rs.getString("phoneno");
                                String password = rs.getString("password");
                                String photopath = rs.getString("photopath");

                                // Construct response string
                                String userDetails = String.format("%s %s %s %s %s %s %s",
                                        myUsername, surname, othername, email, phoneno, password, photopath);

                                dos.writeUTF("CMD_USERDETAILSFOUND " + userDetails);
                                dos.flush();
                            }
                        } catch (Exception e) {
                            serverform.appendMessage("Error retrieving user details for " + username + ": " + e.getMessage());
                        }
                        break;

                    case "CMD_MODIFY":
                        /**
                         * CMD_MODIFY [SNAME] [ONAME] [EMAIL] [PHONE] [USERNAME]
                         * [PASSWORD]*
                         */
                        dos = null;
                        try {
                            sname = st.nextToken();
                            oname = st.nextToken();
                            email = st.nextToken();
                            phone = st.nextToken();
                            username = st.nextToken();
                            password = st.nextToken();
                            photo_path = st.nextToken();
                            client = username;

                            /*int length = dis.readInt();
                            byte[] p = new byte[length];
                            dis.readFully(p);*/
                            File defaultPhotoFile = new File(photo_path);
                            photo = Files.readAllBytes(defaultPhotoFile.toPath());

                            con = new dbconnection();
                            PreparedStatement ps = con.psStatement("update userinfo set surname=?, othername=?,"
                                    + " email=?, phoneno=?, password=?, photo=?, photopath=? where username=? ");
                            ps.setString(1, sname);
                            ps.setString(2, oname);
                            ps.setString(3, email);
                            ps.setString(4, phone);
                            ps.setString(5, password);
                            ps.setBytes(6, photo);
                            ps.setString(7, photo_path);
                            ps.setString(8, username);

                            int rs = ps.executeUpdate();
                            serverform.appendMessage(username + " connected \n");

                            dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("CMD_MODIFYCONFIRMED " + username);
                            dos.flush();
                            serverform.appendMessage("" + username + " MODIFIED successfully");

                        } catch (Exception e) {
                            dos.writeUTF("CMD_MODIFYNOTCONFIRMED " + username);
                            serverform.appendMessage("Error when trying to modify" + username + e);
                        }

                        break;

                    case "CMD_SHARINGSOCKET":
                        serverform.appendMessage("CMD_SHARINGSOCKET : Client stablish a socket connection for file sharing...");
                        String file_sharing_username = st.nextToken();
                        filesharing_username = file_sharing_username;
                        serverform.setClientFileSharingUsername(file_sharing_username);
                        serverform.setClientFileSharingSocket(socket);
                        serverform.appendMessage("CMD_SHARINGSOCKET : Username: " + file_sharing_username);

                        break;

                    case "CMD_SENDFILE":
                        serverform.appendMessage("CMD_SENDFILE : Client sending a file...");
                        /*
                        Format: CMD_SENDFILE [Filename] [Size] [Recipient] [Sender]  from: Sender Format
                        Format: CMD_SENDFILE [Filename] [Size] [Sender] to Receiver Format
                         */
                        String file_name = st.nextToken();
                        String filesize = st.nextToken();
                        String sendto = st.nextToken();
                        String Sender = st.nextToken();
                        serverform.appendMessage("CMD_SENDFILE : From: " + Sender);
                        serverform.appendMessage("CMD_SENDFILE : To: " + sendto);
                        serverform.appendMessage("CMD_SENDFILE : preparing connections..");
                        Socket cSock = serverform.getClientFileSharingSocket(sendto);

                        if (cSock != null) {
                            try {

                                DataOutputStream cDos = new DataOutputStream(cSock.getOutputStream());
                                cDos.writeUTF("CMD_SENDFILE " + file_name + " " + filesize + " " + Sender);
                                InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();

                                byte[] buffer = new byte[BUFFER_SIZE];
                                int cnt;

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                while ((cnt = input.read(buffer)) > 0) {
                                    sendFile.write(buffer, 0, cnt);
                                    baos.write(buffer, 0, cnt);
                                }
                                byte[] fileBytes = baos.toByteArray();
                                InputStream theFile = new ByteArrayInputStream(fileBytes);

                                try {
                                    con = new dbconnection();
                                    PreparedStatement ps = con.psStatement("insert into files values (?,?,?,?)");
                                    ps.setString(1, Sender);
                                    ps.setString(2, sendto);
                                    ps.setString(3, file_name);
                                    ps.setBlob(4, theFile);

                                    int rs = ps.executeUpdate();
                                    serverform.appendMessage(file_name + " saved to database \n");
                                } catch (Exception e) {
                                    serverform.appendMessage("File not saving to database");
                                }
                                sendFile.flush();
                                sendFile.close();
                                serverform.removeClientFileSharing(sendto);
                                serverform.removeClientFileSharing(Sender);

                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                            }
                        } else {
                            /*   FORMAT: CMD_SENDFILEERROR  */
                            serverform.removeClientFileSharing(Sender);
                            serverform.appendMessage("CMD_SENDFILE : Client '" + sendto + "' was not found.!");
                            dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("CMD_SENDFILEERROR " + "Client '" + sendto + "' was not found, File Sharing will exit.");
                        }
                        break;

                    case "CMD_SENDFILERESPONSE":
                        /*
                        Format: CMD_SENDFILERESPONSE [username] [Message]
                         */
                        String receiver = st.nextToken();
                        String rMsg = "";
                        serverform.appendMessage("[CMD_SENDFILERESPONSE]: username: " + receiver);
                        while (st.hasMoreTokens()) {
                            rMsg = rMsg + " " + st.nextToken();
                        }
                        try {
                            Socket rSock = (Socket) serverform.getClientFileSharingSocket(receiver);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("CMD_SENDFILERESPONSE" + " " + receiver + " " + rMsg);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        break;

                    case "CMD_SEND_FILE_XD":  // Format: CMD_SEND_FILE_XD [sender] [receiver]                        
                        try {
                        String send_sender = st.nextToken();
                        String send_receiver = st.nextToken();
                        String send_filename = st.nextToken();
                        serverform.appendMessage("CMD_SEND_FILE_XD Host: " + send_sender);
                        this.createConnection(send_receiver, send_sender, send_filename);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    break;

                    case "CMD_SEND_FILE_ERROR":
                        // Format:  CMD_SEND_FILE_ERROR [receiver] [Message]
                        String eReceiver = st.nextToken();
                        String eMsg = "";
                        while (st.hasMoreTokens()) {
                            eMsg = eMsg + " " + st.nextToken();
                        }
                        try {

                            Socket eSock = serverform.getClientFileSharingSocket(eReceiver);
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ERROR [Message]
                            eDos.writeUTF("CMD_RECEIVE_FILE_ERROR " + eMsg);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;

                    case "CMD_SEND_FILE_ACCEPT": // Format:  CMD_SEND_FILE_ACCEPT [receiver] [Message]
                        String aReceiver = st.nextToken();
                        String aMsg = "";
                        while (st.hasMoreTokens()) {
                            aMsg = aMsg + " " + st.nextToken();
                        }
                        try {
                            /*  Send Error to the File Sharing host  */
                            Socket aSock = serverform.getClientFileSharingSocket(aReceiver); // get the file sharing host socket for connection
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            //  Format:  CMD_RECEIVE_FILE_ACCEPT [Message]
                            aDos.writeUTF("CMD_RECEIVE_FILE_ACCEPT " + aMsg);
                        } catch (IOException e) {
                            serverform.appendMessage("[CMD_RECEIVE_FILE_ERROR]: " + e.getMessage());
                        }
                        break;

                    case "CMD_INITIATEVOICECALL":

                        /**
                         * CMD_INITIATEVOICECALL [caller] [callee]
                         */
                        caller = st.nextToken();
                        callee = st.nextToken();

                        // Check if the callee is online
                        calleeSocket = serverform.getClientList(callee);
                        if (calleeSocket != null) {
                            try {
                                // Notify the callee about the incoming call
                                DataOutputStream calleeDos = new DataOutputStream(calleeSocket.getOutputStream());
                                calleeDos.writeUTF("CMD_INCOMINGCALL " + caller);

                                // Notify the caller that the call request has been sent
                                dos.writeUTF("CMD_CALLREQUESTSENT " + callee);
                                serverform.appendMessage("Voice call initiated: " + caller + " is calling " + callee);
                            } catch (IOException e) {
                                serverform.appendMessage("Error initiating voice call: " + e.getMessage());
                                try {
                                    dos.writeUTF("CMD_CALLREQUESTERROR " + callee);
                                } catch (IOException ioException) {
                                    serverform.appendMessage("Error sending call request error to caller: " + ioException.getMessage());
                                }
                            }
                        } else {
                            serverform.appendMessage("Voice call initiation failed: " + callee + " is not online.");
                            try {
                                dos.writeUTF("CMD_CALLEENOTONLINE " + callee);
                            } catch (IOException e) {
                                serverform.appendMessage("Error notifying caller about callee not being online: " + e.getMessage());
                            }
                        }
                        break;

                    /*case "CMD_ANSWERCALL":
                        caller = st.nextToken();
                        try {
                            // Notify both parties that the call has been accepted
                            Socket callerSocket = serverform.getClientList(caller);
                            if (callerSocket != null) {
                                DataOutputStream callerDos = new DataOutputStream(callerSocket.getOutputStream());
                                callerDos.writeUTF("CMD_CALLACCEPTED " + username); // 'username' is the callee

                                dos.writeUTF("CMD_CALLACCEPTED " + caller); // Notify the callee as well
                                serverform.appendMessage("Call accepted: " + username + " accepted the call from " + caller);
                            }
                        } catch (IOException e) {
                            serverform.appendMessage("Error handling call acceptance: " + e.getMessage());
                        }
                        break;*/
                    case "CMD_ANSWERCALL":
                        caller = st.nextToken();
                        try {
                            // Notify both parties that the call has been accepted
                            Socket callerSocket = serverform.getClientList(caller);
                            if (callerSocket != null) {
                                DataOutputStream callerDos = new DataOutputStream(callerSocket.getOutputStream());
                                String calleeHostAddress = this.socket.getInetAddress().getHostAddress(); // Get callee's host address

                                // Notify the caller with callee's address
                                callerDos.writeUTF("CMD_CALLACCEPTED " + username + " " + calleeHostAddress); // 'username' is the callee

                                // Notify the callee as well
                                dos.writeUTF("CMD_CALLACCEPTED " + caller + " " + callerSocket.getInetAddress().getHostAddress());
                                serverform.appendMessage("Call accepted: " + username + " accepted the call from " + caller);
                            }
                        } catch (IOException e) {
                            serverform.appendMessage("Error handling call acceptance: " + e.getMessage());
                        }
                        break;

                    case "CMD_DECLINECALL":
                        caller = st.nextToken();
                        try {
                            // Notify the caller that the call has been declined
                            Socket callerSocket = serverform.getClientList(caller);
                            if (callerSocket != null) {
                                DataOutputStream callerDos = new DataOutputStream(callerSocket.getOutputStream());
                                callerDos.writeUTF("CMD_CALLREJECTED " + username); // 'username' is the callee

                                serverform.appendMessage("Call declined: " + username + " declined the call from " + caller);
                            }
                        } catch (IOException e) {
                            serverform.appendMessage("Error handling call decline: " + e.getMessage());
                        }
                        break;

                    case "CMD_INITIATEVIDEOCALL":

                        /**
                         * CMD_INITIATEVOICECALL [caller] [callee]
                         */
                        caller = st.nextToken();
                        callee = st.nextToken();

                        // Check if the callee is online
                        calleeSocket = serverform.getClientList(callee);
                        if (calleeSocket != null) {
                            try {
                                // Notify the callee about the incoming call
                                DataOutputStream calleeDos = new DataOutputStream(calleeSocket.getOutputStream());
                                calleeDos.writeUTF("CMD_INCOMINGVIDEOCALL " + caller);

                                // Notify the caller that the call request has been sent
                                dos.writeUTF("CMD_VIDEOCALLREQUESTSENT " + callee);
                                serverform.appendMessage("Voice call initiated: " + caller + " is calling " + callee);
                            } catch (IOException e) {
                                serverform.appendMessage("Error initiating voice call: " + e.getMessage());
                                try {
                                    dos.writeUTF("CMD_VIDEOCALLREQUESTERROR " + callee);
                                } catch (IOException ioException) {
                                    serverform.appendMessage("Error sending call request error to caller: " + ioException.getMessage());
                                }
                            }
                        } else {
                            serverform.appendMessage("Video call initiation failed: " + callee + " is not online.");
                            try {
                                dos.writeUTF("CMD_VIDEOCALLEENOTONLINE " + callee);
                            } catch (IOException e) {
                                serverform.appendMessage("Error notifying caller about callee not being online: " + e.getMessage());
                            }
                        }
                        break;

                    case "CMD_ANSWERVIDEOCALL":
                        caller = st.nextToken();
                        try {
                            // Notify both parties that the call has been accepted
                            Socket callerSocket = serverform.getClientList(caller);
                            if (callerSocket != null) {
                                DataOutputStream callerDos = new DataOutputStream(callerSocket.getOutputStream());
                                String calleeHostAddress = this.socket.getInetAddress().getHostAddress(); // Get callee's host address

                                // Notify the caller with callee's address
                                callerDos.writeUTF("CMD_VIDEOCALLACCEPTED " + username + " " + calleeHostAddress); // 'username' is the callee

                                // Notify the callee as well
                                dos.writeUTF("CMD_VIDEOCALLACCEPTED " + caller + " " + callerSocket.getInetAddress().getHostAddress());
                                serverform.appendMessage("Call accepted: " + username + " accepted the call from " + caller);
                            }
                        } catch (IOException e) {
                            serverform.appendMessage("Error handling call acceptance: " + e.getMessage());
                        }
                        break;

                    case "CMD_VIDEODECLINECALL":
                        caller = st.nextToken();
                        try {
                            // Notify the caller that the call has been declined
                            Socket callerSocket = serverform.getClientList(caller);
                            if (callerSocket != null) {
                                DataOutputStream callerDos = new DataOutputStream(callerSocket.getOutputStream());
                                callerDos.writeUTF("CMD_VIDEOCALLREJECTED " + username); // 'username' is the callee

                                serverform.appendMessage("Call declined: " + username + " declined the call from " + caller);
                            }
                        } catch (IOException e) {
                            serverform.appendMessage("Error handling call decline: " + e.getMessage());
                        }
                        break;

                    default:
                        serverform.appendMessage("Unknown Command " + CMD);
                        break;
                }
            }
        } catch (IOException e) {

            serverform.removeFromTheList(client);
            serverform.appendMessage("Connection closed..!");
        }
    }

}
