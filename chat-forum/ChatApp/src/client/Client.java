/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client;

import client.VoiceNoteThread.TimerRunnable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;

/**
 *
 * @author Maryam_Otunara
 */
public class Client extends javax.swing.JFrame {

    String username, command, password, sname, oname, phone, email;
    String photo_path;
    String host;
    registration registerform;
    Login loginform;
    modify_profile modifyform;
    VoiceNoteThread voiceNoteThread;
    int port;
    Socket socket;
    DataOutputStream dos;
    String SendTo = "";
    Thread t;
    boolean attachmentOpen = false;
    String mydownloadfolder = "C:\\";
    private TimerRunnable timerRunnable;

    /**
     * Creates new form Client
     */
    public Client() {
        initComponents();

        setTitle("Client");

        Notification r = new Notification();
        t = new Thread(r);
        t.start();

    }

    public void verifyLoginDetails(String username, String host, int port, String command, String password, Login loginform) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.loginform = loginform;
        connect();

    }

    public void connect() {
        System.out.println("Login");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */
            dos.writeUTF("CMD_LOGIN " + username + " " + password);
            // dos.writeUTF(command + username+" "+password);
            System.out.println("Login details sent to server");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, loginform);
            Thread thread = new Thread(clientThread);
            thread.start();

            // Run getUserData() in a separate thread
            new Thread(() -> {
                getAllUsers();
            }).start();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void RegistrationDetails(String sname, String oname, String email, String phone, String username, String host, int port, String command, String password, String photo_path, registration registerform) throws IOException {
        this.username = username;
        this.sname = sname;
        this.oname = oname;
        this.email = email;
        this.phone = phone;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.photo_path = photo_path;
        this.registerform = registerform;
        register();
    }

    public void register() {
        System.out.println("Registration");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username and password *
             */

            dos.writeUTF("CMD_REGISTER " + sname + " " + oname + " " + email + " " + phone + " " + username + " " + password + " " + photo_path);
//            dos.writeInt(photo.length);
//            dos.write(photo);

            System.out.println("Registration  details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, registerform);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void ModifyDetails(String sname, String oname, String email, String phone, String username, String host, int port, String command, String password, String photo_path, modify_profile modifyform) throws IOException {
        this.username = username;
        this.sname = sname;
        this.oname = oname;
        this.email = email;
        this.phone = phone;
        this.host = host;
        this.port = port;
        this.command = command;
        this.password = password;
        this.photo_path = photo_path;
        this.modifyform = modifyform;
        modify();
    }

    public void modify() {
        System.out.println("Modify Details");
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send all details *
             */

            // Debug: Print photo length
            //System.out.println("Photo length: " + photo.length);
            dos.writeUTF("CMD_MODIFY " + sname + " " + oname + " " + email + " " + phone + " " + username + " " + password + " " + photo_path);
            /*dos.writeInt(photo.length);
            dos.write(photo);*/
            System.out.println("Modified details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, modifyform);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void appendMessage(String msg) {
        jTextArea1.append(msg);
    }

    public void appendNotification(String msg) {
        jLabel4.setText("");
        jLabel4.setText(msg);
    }

    public void appendOnlineList(Vector list) {

        OnlineList(list);
    }

    private void OnlineList(Vector list) {
        list1.removeAll();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            try {
                Object e = i.next();
                String users = String.valueOf(e);
                list1.add(users);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }

        }
    }

    public void appendUserList(Vector list) {

        UserList(list);
    }

    private void UserList(Vector list) {
        list2.removeAll();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            try {
                Object e = i.next();
                String users = String.valueOf(e);
                list2.add(users);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }

        }
    }

    public void setMyTitle(String s) {
        setTitle(s);
    }

    public String getMyHost() {
        return this.host;
    }

    public int getMyPort() {
        return this.port;
    }

    public String getMyUsername() {
        return this.username;
    }

    //Notification Thread
    public class Notification implements Runnable {

        int count = 0;
        String friend = "";

        @Override
        public void run() {
            while (true) {
                count = list1.getItemCount();
                for (int i = 0; i < count; i++) {
                    friend = list1.getItem(i);
                    try {
                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF("CMD_NOTIFICATION " + friend + " " + username);

                        Thread.sleep(1900);
                    } catch (Exception e) {
                    }

                }

            }

        }
    }

    public void updateAttachment(boolean b) {
        this.attachmentOpen = b;
    }

    public void openFolder() {
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int open = jFileChooser1.showDialog(this, "Select Folder");
        if (open == jFileChooser1.APPROVE_OPTION) {
            mydownloadfolder = jFileChooser1.getSelectedFile().toString() + "\\";
        } else {
            mydownloadfolder = "C:\\";
        }
    }

    public String getMyDownloadFolder() {
        return this.mydownloadfolder;
    }

    public void getAllUsers() {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("CMD_GETALLUSERS");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error sending command to server: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getUserDetails(String username, String host, int port, String command, modify_profile modifyform) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.command = command;
        this.modifyform = modifyform;

        getDetails();
    }

    public void getDetails() {
        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            /**
             * Send username *
             */

            dos.writeUTF("CMD_GETUSERDETAILS " + username);

            System.out.println("Modified details sent to sever");

            /**
             * Start Client Thread *
             */
            ClientThread clientThread = new ClientThread(socket, this, modifyform);
            Thread thread = new Thread(clientThread);
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to Connect to Server, please try again later.!",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        list1 = new java.awt.List();
        jPanel4 = new javax.swing.JPanel();
        list2 = new java.awt.List();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(254, 248, 254));

        jLabel1.setFont(new java.awt.Font("Rockwell", 1, 18)); // NOI18N
        jLabel1.setText("SST Chat");

        jTabbedPane1.setBackground(new java.awt.Color(254, 248, 254));
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contacts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Gulim", 0, 14))); // NOI18N

        jPanel2.setBackground(new java.awt.Color(254, 248, 254));

        list1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                list1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(list1, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Online", jPanel2);

        jPanel4.setBackground(new java.awt.Color(254, 248, 254));

        list2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                list2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(list2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(list2, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("All", jPanel4);

        jPanel3.setBackground(new java.awt.Color(254, 248, 254));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Messages", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Gulim", 0, 14))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel2.setText("jLabel2");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jButton1.setBackground(new java.awt.Color(14, 14, 79));
        jButton1.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jToggleButton1.setBackground(new java.awt.Color(102, 0, 0));
        jToggleButton1.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jToggleButton1.setForeground(new java.awt.Color(255, 255, 255));
        jToggleButton1.setText("Voice Note");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel3.setText("Notification");

        jLabel4.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 0, 0));
        jLabel4.setText("jLabel4");

        jLabel5.setIcon(new javax.swing.ImageIcon("C:\\Users\\Maryam_Otunara\\OneDrive\\Desktop\\SCHOOL\\Yr3Sem2\\CSC302\\EXAM\\pics\\voicecallicon.png")); // NOI18N
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon("C:\\Users\\Maryam_Otunara\\OneDrive\\Desktop\\SCHOOL\\Yr3Sem2\\CSC302\\EXAM\\pics\\videocallicon.png")); // NOI18N
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(330, 330, 330)
                                .addComponent(jLabel1))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel6)
                        .addGap(19, 19, 19))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(jLabel4))
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTabbedPane1)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jMenuBar1.setBackground(new java.awt.Color(254, 248, 254));
        jMenuBar1.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N

        jMenu1.setText("Profile");

        jMenuItem1.setText("Change Profile");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Attachments");

        jMenuItem2.setText("Send Attachment");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Logout");

        jMenuItem3.setText("Logout");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void list1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_list1MouseClicked
        //Get Messages from the Database

        jTextArea1.setText("");
        SendTo = list1.getSelectedItem();
        jLabel2.setText(SendTo);
        //Get All Messages from Database BETWEEN  you and the friend
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("CMD_GETMESSAGES " + SendTo + " " + username);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_list1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Send Message

        try {
            String to = jLabel2.getText();
            /**
             * CMD_CHAT [from] [sendTo] [message] *
             */
            if (!to.equalsIgnoreCase("")) {
                String message = username + " " + to + " " + jTextArea2.getText();
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_CHAT " + message);
                jTextArea1.append("\n" + username + " : " + jTextArea2.getText());
                jTextArea2.setText("");
            } else {
                JOptionPane.showMessageDialog(rootPane, "Select who you want to Chat With");

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        //Logout

        int confirm = JOptionPane.showConfirmDialog(null, "Confirm Logout.?");
        if (confirm == 0) {
            try {
                socket.close();
                setVisible(false);

                new Login().setVisible(true);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //Change profile

        Client clientform = new Client();
        modify_profile modifyform = new modify_profile();
        modifyform.setVisible(true);
        String myUsername = getMyUsername();
        modifyform.initDetails(myUsername);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        //Send Attachment

        if (!attachmentOpen) {
            Attachment sendfile = new Attachment();
            if (sendfile.prepare(username, host, port, this)) {
                sendfile.setLocationRelativeTo(null);
                sendfile.setVisible(true);
                attachmentOpen = true;
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unable to stablish File Sharing at this moment, please try again later.!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void list2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_list2MouseClicked
        //Get Messages from the Database

        jTextArea1.setText("");
        SendTo = list2.getSelectedItem();
        jLabel2.setText(SendTo);
        //Get All Messages from Database BETWEEN  you and the friend
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("CMD_GETMESSAGES " + SendTo + " " + username);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_list2MouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        //Voice Call

        /**
         * CMD_INITIATEVOICECALL [from] [sendTo] [message]
         */
        var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to start this call?");

        if (choice == JOptionPane.YES_OPTION) {
            try {
                String to = jLabel2.getText().trim();
                if (!to.isEmpty()) {
                    String from = username; // Assuming 'username' is your initiator's username
                    
                    /*int initiatorPort = socket.getLocalPort(); // Get the local port of your socket
                    String initiatorIPAddress = socket.getLocalAddress().getHostAddress(); // Get the local IP address of your socket
                    String receiverUsername = jLabel2.getText().trim();*/

                    // Format the message to send
                    /*String message = String.format("CMD_INITIATEVOICECALL %s %s %s %s",
                            initiatorUsername, initiatorPort, initiatorIPAddress, jTextArea2.getText().trim());*/

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF("CMD_INITIATEVOICECALL " + from + " " + to);

                    jTextArea1.append("\n" + username + " : " + jTextArea2.getText());
                    jTextArea2.setText(""); // Clear the input area after sending
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Select who you want to Call");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(rootPane, "Error sending voice call request: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        // Video Call
        
        /**
         * CMD_INITIATEVIDEOCALL [from] [sendTo] [message]
         */
    var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to start this call?");

    if (choice == JOptionPane.YES_OPTION) {
        try {
            String to = jLabel2.getText().trim();
            if (!to.isEmpty()) {
                String from = username; // Assuming 'username' is your initiator's username

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF("CMD_INITIATEVIDEOCALL " + from + " " + to);

                jTextArea1.append("\n" + username + " : " + jTextArea2.getText());
                jTextArea2.setText(""); // Clear the input area after sending
            } else {
                JOptionPane.showMessageDialog(rootPane, "Select who you want to Call");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPane, "Error sending video call request: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        //Record Voice Note
        
        this.voiceNoteThread = new VoiceNoteThread(this, socket);
        if (jToggleButton1.isSelected()) {
            voiceNoteThread.startRecording();
        } else {
            voiceNoteThread.stopRecording();
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    public static javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    public static javax.swing.JToggleButton jToggleButton1;
    private java.awt.List list1;
    private java.awt.List list2;
    // End of variables declaration//GEN-END:variables
}
