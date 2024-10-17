/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */
public class AudioCall extends javax.swing.JFrame {

    TargetDataLine targetLine;
    DataLine.Info captureInfo, playbackInfo;
    Socket socket;
    SourceDataLine sourceLine;
    OutputStream outputStream;
    InputStream inputStream;
    boolean callActive;
    long startTime;
    boolean timerActive;

    /**
     * Creates new form AudioCall
     */
    public AudioCall() {
        initComponents();

        setTitle("Audio Call");
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void initAudioCall(String host) {

        //jLabel1.setText(callee);
        try {
            // Set up audio capture
            AudioFormat captureFormat = new AudioFormat(8000.0f, 16, 1, true, true);
            captureInfo = new DataLine.Info(TargetDataLine.class, captureFormat);
            targetLine = (TargetDataLine) AudioSystem.getLine(captureInfo);
            targetLine.open(captureFormat);
            targetLine.start();

            // Set up audio playback
            AudioFormat playbackFormat = new AudioFormat(8000.0f, 16, 1, true, true);
            playbackInfo = new DataLine.Info(SourceDataLine.class, playbackFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(playbackInfo);
            sourceLine.open(playbackFormat);
            sourceLine.start();

            // Connect to the server
            socket = new Socket(host, 5000);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            callActive = true;
            startTime = System.currentTimeMillis();
            timerActive = true;

            // Capture audio thread
            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (callActive) {
                    int bytesRead = targetLine.read(buffer, 0, buffer.length);
                    try {
                        outputStream.write(buffer, 0, bytesRead);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            captureThread.start();

            // Playback audio thread
            Thread playbackThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (callActive) {
                    try {
                        int bytesRead = inputStream.read(buffer);
                        if (bytesRead > 0) {
                            sourceLine.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            playbackThread.start();
            
            // Timer thread
            Thread timerThread = new Thread(() -> {
                DecimalFormat df = new DecimalFormat("00");
                while (timerActive) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime;
                    long seconds = (elapsedTime / 1000) % 60;
                    long minutes = (elapsedTime / (1000 * 60)) % 60;
                    long hours = (elapsedTime / (1000 * 60 * 60)) % 24;

                    String timeStr = df.format(hours) + ":" + df.format(minutes) + ":" + df.format(seconds);
                    jLabel2.setText(timeStr);

                    try {
                        Thread.sleep(1000); // Update every second
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            timerThread.start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "Could not connect to Server IP Address",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void endCall() {
        callActive = false;
        timerActive = false;
        try {
            if (targetLine != null) {
                targetLine.stop();
                targetLine.close();
            }
            if (sourceLine != null) {
                sourceLine.stop();
                sourceLine.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(254, 248, 254));

        jToggleButton1.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jToggleButton1.setText("Mute");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Gulim", 0, 14)); // NOI18N
        jButton1.setText("End Call");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Rockwell", 0, 16)); // NOI18N
        jLabel1.setText("jLabel1");

        jLabel2.setFont(new java.awt.Font("Rockwell", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 0, 0));
        jLabel2.setText("jLabel2");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(150, 150, 150)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(16, 16, 16))
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

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        //Mute
        
        if (targetLine.isActive()) {
            targetLine.stop();
            jToggleButton1.setText("Unmute");
        } else {
            targetLine.start();
            jToggleButton1.setText("Mute");
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // End Call
        
        endCall();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(AudioCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AudioCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AudioCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AudioCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AudioCall().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
