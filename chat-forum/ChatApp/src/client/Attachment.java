/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */
public class Attachment extends javax.swing.JFrame {

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    String myusername;
    String host;
    int port;
    StringTokenizer st;
    String sendTo;
    String file;
    Client ClientFrame;
    
    /**
     * Creates new form Attachment
     */
    public Attachment() {
        initComponents();
        
        setTitle("Send Attachment");

        jProgressBar1.setVisible(false);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    public boolean prepare(String u, String h, int p, Client m) {
        this.host = h;
        this.myusername = u;
        this.port = p;
        this.ClientFrame = m;

        try {
            socket = new Socket(host, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            //  Format: CMD_SHARINGSOCKET [sender]
            String format = "CMD_SHARINGSOCKET " + myusername;
            dos.writeUTF(format);
            System.out.println(format);

            new Thread(new SendFileThread(this)).start();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    class SendFileThread implements Runnable {

        Attachment form;

        public SendFileThread(Attachment form) {
            this.form = form;
        }

        private void closeMe() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            dispose();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String data = dis.readUTF();
                    st = new StringTokenizer(data);
                    String cmd = st.nextToken();
                    switch (cmd) {
                        case "CMD_RECEIVE_FILE_ERROR":
                            // Format: CMD_RECEIVE_FILE_ERROR [Message]
                            String msg = "";
                            while (st.hasMoreTokens()) {
                                msg = msg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(Attachment.this, msg,
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            this.closeMe();
                            break;

                        case "CMD_RECEIVE_FILE_ACCEPT":
                            // Format: CMD_RECEIVE_FILE_ACCEPT [Message]
                            new Thread(new SendingFileThread(socket, file, sendTo, myusername, Attachment.this)).start();
                            break;

                        case "CMD_SEND_FILE_ERROR":
                            String emsg = "";
                            while (st.hasMoreTokens()) {
                                emsg = emsg + " " + st.nextToken();
                            }
                            System.out.println(emsg);
                            JOptionPane.showMessageDialog(Attachment.this, emsg, "Error", JOptionPane.ERROR_MESSAGE);
                            form.updateAttachment(false);
                            form.disableButtons(false);
                            form.updateBtn("Send File");
                            break;

                        case "CMD_SENDFILERESPONSE":
                            /*
                            Format: CMD_SENDFILERESPONSE [username] [Message]
                             */
                            String rReceiver = st.nextToken();
                            String rMsg = "";
                            while (st.hasMoreTokens()) {
                                rMsg = rMsg + " " + st.nextToken();
                            }
                            form.updateAttachment(false);
                            JOptionPane.showMessageDialog(Attachment.this, rMsg, "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            break;
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public void showOpenDialog() {
        int intval = jFileChooser1.showOpenDialog(this);
        if (intval == jFileChooser1.APPROVE_OPTION) {
            jTextField1.setText(jFileChooser1.getSelectedFile().toString());
        } else {
            jTextField1.setText("");
        }
    }

    public void disableButtons(boolean d) {
        if (d) {
            jTextField1.setEditable(false);
            jButton1.setEnabled(false);
            jButton2.setEnabled(false);
            jTextField2.setEditable(false);
            jProgressBar1.setVisible(true);
        } else {
            jTextField1.setEditable(true);
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
            jTextField2.setEditable(true);
            jProgressBar1.setVisible(false);
        }
    }

    public void setMyTitle(String s) {
        setTitle(s);
    }

    protected void closeThis() {
        dispose();
    }

    public String getThisFilename(String path) {
        File p = new File(path);
        String fname = p.getName();
        return fname.replace(" ", "_");
    }

    public void updateAttachment(boolean b) {
        ClientFrame.updateAttachment(b);
    }

    public void updateBtn(String str) {
        jButton2.setText(str);
    }

    public void updateProgress(int val) {
        jProgressBar1.setValue(val);
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(254, 248, 254));

        jLabel1.setFont(new java.awt.Font("Rockwell", 1, 18)); // NOI18N
        jLabel1.setText("Send File");

        jLabel2.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jLabel2.setText("Select File:");

        jLabel3.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jLabel3.setText("Receiver:");

        jTextField1.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N

        jTextField2.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N

        jButton1.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jButton1.setText("Browse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jButton2.setText("Send");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jProgressBar1.setForeground(new java.awt.Color(153, 255, 51));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(192, 192, 192)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(46, 46, 46)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addGap(38, 38, 38))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(57, 57, 57)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
                .addGap(23, 23, 23))
        );

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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Browse
        
        showOpenDialog();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Send

        sendTo = jTextField2.getText();
        file = jTextField1.getText();

        if ((sendTo.length() > 0) && (file.length() > 0)) {
            try {
                // Format: CMD_SEND_FILE_XD [sender] [receiver] [filename]
                //jTextField1.setText("");
                String fname = getThisFilename(file);
                String format;
                format = "CMD_SEND_FILE_XD " + myusername + " " + sendTo + " " + fname;                             
                dos.writeUTF(format);
                System.out.println(format);
                updateBtn("Sending...");
                jButton2.setEnabled(false);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Incomplete Form.!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        //Close Attachment Frame

        ClientFrame.updateAttachment(false);
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_formWindowClosing

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
            java.util.logging.Logger.getLogger(Attachment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Attachment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Attachment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Attachment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Attachment().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
