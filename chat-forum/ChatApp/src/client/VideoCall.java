/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package client;

import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;

/**
 *
 * @author Maryam_Otunara
 */
public class VideoCall extends javax.swing.JFrame {

    static Socket VideoClientSocket;
    static Socket AudioClientSocket;
    static ObjectInputStream VideoInputStream;
    static ObjectOutputStream VideoOutputStream;
    static Webcam camera;
    static TargetDataLine targetLine;
    static SourceDataLine sourceLine;
    static OutputStream AudioOutputStream;
    static InputStream AudioInputStream;
    
    /**
     * Creates new form VideoCall
     */
    public VideoCall() {
        initComponents();
        
        setTitle("Video Call Client");
        
        //initVideoCall();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    
    public void initVideoCall(String host){
        
        try {
            AudioClientSocket = new Socket(host, 12345);
            System.out.println("Audio Connected");
            VideoClientSocket = new Socket(host, 12346);
            System.out.println("Video Connected");
            
            AudioFormat captureFormat = new AudioFormat(8000.0f, 16, 1, true, true);
            DataLine.Info captureInfo = new DataLine.Info(TargetDataLine.class, captureFormat);
            targetLine = (TargetDataLine) AudioSystem.getLine(captureInfo);
            targetLine.open(captureFormat);
            targetLine.start();
            
            AudioFormat playbackFormat = new AudioFormat(8000.0f, 16, 1, true, true);
            DataLine.Info playbackInfo = new DataLine.Info(SourceDataLine.class, playbackFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(playbackInfo);
            sourceLine.open(playbackFormat);
            sourceLine.start();
            
            AudioOutputStream = AudioClientSocket.getOutputStream();
            AudioInputStream = AudioClientSocket.getInputStream();
            
            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead = targetLine.read(buffer, 0, buffer.length);
                    try {
                        AudioOutputStream.write(buffer, 0, bytesRead);
                    } catch (IOException ex) {
                        
                    }
                }
            });
            captureThread.start();
            
            Thread playbackThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (true) {
                    try {
                        int bytesRead = AudioInputStream.read(buffer);
                        sourceLine.write(buffer, 0, bytesRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            playbackThread.start();
            
            VideoOutputStream = new ObjectOutputStream(VideoClientSocket.getOutputStream());
            VideoInputStream = new ObjectInputStream(VideoClientSocket.getInputStream());
            ImageIcon videotoServer, VideofromServer;
            BufferedImage buffervideo;
            List<Webcam> webcams = Webcam.getWebcams();
            camera = webcams.get(1);
            camera = Webcam.getDefault();
            
            camera.open();
            
            while (true) {
                buffervideo = camera.getImage();
                videotoServer = new ImageIcon(buffervideo);
                VideoOutputStream.writeObject(videotoServer);
                VideoOutputStream.flush();
                jLabel1.setIcon(videotoServer);
                VideofromServer = (ImageIcon) VideoInputStream.readObject();
                jLabel2.setIcon(VideofromServer);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                if (VideoOutputStream  != null) {
                    VideoOutputStream.close();
                }
                if (VideoInputStream != null) {
                    VideoInputStream.close();
                }
                if (VideoClientSocket != null) {
                    VideoClientSocket.close();
                }
                if (camera != null) {
                    camera.close();
                }
                if (targetLine != null) {
                    targetLine.drain();
                    targetLine.close();
                }
                if (AudioOutputStream != null) {
                    AudioOutputStream.close();
                }
                if (sourceLine != null) {
                    sourceLine.drain();
                    sourceLine.close();
                }
            } catch (Exception e) {
                
            }
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(254, 248, 254));

        jLabel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Client", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Gulim", 0, 14))); // NOI18N

        jLabel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Gulim", 0, 14))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(VideoCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VideoCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VideoCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VideoCall.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VideoCall().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JLabel jLabel1;
    public static javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
