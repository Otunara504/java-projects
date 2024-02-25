/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */
public class sales_manager extends javax.swing.JFrame {

    static String[] staffDetails;
    String mailTopic;
    String mailBody;

    /**
     * Creates new form sales_manager
     */
    public sales_manager(String[] staffDetails) {
        initComponents();

        this.staffDetails = staffDetails;

        time t = new time();
        Thread t1 = new Thread(t);
        t1.start();

        name n = new name();
        Thread n1 = new Thread(n);
        n1.start();

        displayBirthdayMessage d = new displayBirthdayMessage();
        Thread d1 = new Thread(d);
        d1.start();

        lowStockNotificationTask l = new lowStockNotificationTask();
        Thread l1 = new Thread(l);
        l1.start();

        expirationNotificationTask e = new expirationNotificationTask();
        Thread e1 = new Thread(e);
        e1.start();
        
        BackupProcess backupProcess = new BackupProcess();
        BackupThread backupThread = new BackupThread(backupProcess);
        backupThread.start();
    }

    public class time implements Runnable {

        @Override
        public void run() {
            try {
                for (int i = 1; i < 2; i--) {

                    Date date = new Date();
                    jLabel2.setText(date.toString());
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (Exception e) {
                System.out.println("error");
            }
        }
    }

    public class name implements Runnable {

        @Override
        public void run() {
            try {
                String surname = staffDetails[0];
                String firstname = staffDetails[1];
                String staffName = firstname + " " + surname;
                jLabel3.setText("Sales Manager: " + staffName);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }

    public Date getBirthDate() {
        String dateString = staffDetails[3];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date birthDate = null;
        try {
            birthDate = dateFormat.parse(dateString);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return birthDate;
    }

    // Function to check if today is the birthday
    public boolean isBirthdayToday() {
        Date birthDate = getBirthDate();

        if (birthDate != null) {
            Calendar today = Calendar.getInstance();
            Calendar birthday = Calendar.getInstance();
            birthday.setTime(birthDate);

            return today.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH)
                    && today.get(Calendar.MONTH) == birthday.get(Calendar.MONTH);
        }

        return false;
    }

    public class displayBirthdayMessage implements Runnable {

        @Override
        public void run() {
            try {
                if (isBirthdayToday()) {
                    JOptionPane.showMessageDialog(rootPane, "Happy Birthday 🥳🥳🥳!");
                }
            } catch (Exception e) {
                System.out.println("error");
            }
        }
    }

    public class mail implements Runnable {
        //mail thread

        String email = staffDetails[4];

        @Override
        public void run() {
            String subject = mailTopic;
            String receiver = email;
            String body = mailBody;

            String senderEmail = "project.cbtapp@gmail.com";
            String senderPassword = "stxl vdvf upts ryuc";
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
                JOptionPane.showMessageDialog(rootPane, "Mail Sent");
            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }

    public class lowStockNotificationTask implements Runnable {

        @Override
        public void run() {
            try {
                // Retrieve products with quantity less than 50 from the database
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/pos", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("SELECT * FROM inventory WHERE quantity < 50");
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String productName = rs.getString("productname");
                    int currentQuantity = rs.getInt("quantity");

                    JOptionPane.showMessageDialog(rootPane, "Low stock for product " + productName + ". Current quantity: " + currentQuantity);

                    mailTopic = "Low Stock Notification for " + productName;
                    mailBody = "Low stock for product " + productName + ". Current quantity: " + currentQuantity;
                    //Instantiate mail thread
                    mail m = new mail();
                    Thread m1 = new Thread(m);
                    m1.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class expirationNotificationTask implements Runnable {

        @Override
        public void run() {
            try {
                // Retrieve products with expiration dates within the next ten days from the database
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/pos", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("SELECT * FROM inventory WHERE DATEDIFF(expirydate, NOW()) BETWEEN 1 AND 10");
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String productName = rs.getString("productname");
                    Date expiryDate = rs.getDate("expirydate");

                    JOptionPane.showMessageDialog(rootPane, "Product " + productName + " will expire on " + expiryDate);

                    mailTopic = "Expiration Date Notification for " + productName;
                    mailBody = "Product " + productName + " will expire on " + expiryDate;
                    //Instantiate mail thread
                    mail m = new mail();
                    Thread m1 = new Thread(m);
                    m1.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel1.setBackground(new java.awt.Color(253, 234, 234));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Home Page");

        jLabel2.setFont(new java.awt.Font("Fira Code Light", 0, 14)); // NOI18N
        jLabel2.setText("Time Here");

        jButton1.setBackground(new java.awt.Color(251, 251, 239));
        jButton1.setText("<html>Monitor Cashier's Performance</html>");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(251, 251, 239));
        jButton2.setText("<html>Generate Report</html>");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(251, 251, 239));
        jButton3.setText("Insights");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel3.setText("Name Here");

        jButton4.setText("Logout");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(251, 251, 239));
        jButton5.setText("Cashier");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jButton4)
                        .addGap(165, 165, 165)
                        .addComponent(jLabel1)
                        .addGap(78, 78, 78)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel3)))
                .addContainerGap(219, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(78, 78, 78)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(68, 68, 68))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jButton4))
                .addGap(71, 71, 71)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(17, 17, 17))
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

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Back to login page

        var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to logout?");
        if (choice == JOptionPane.YES_OPTION) {
            
            dispose();
            new login().setVisible(true);
        }
        
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //cashier_performance page

        dispose();
        new cashier_performance(staffDetails).setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //sales_report page

        dispose();
        new sales_report(staffDetails).setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // sales_insights page

        dispose();
        new sales_insights(staffDetails).setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // cashier page

        dispose();
        new cashier(staffDetails).setVisible(true);
    }//GEN-LAST:event_jButton5ActionPerformed

    public class BackupProcess {

        public void checkScheduledBackups() {
            // Retrieve current date and time
            LocalDateTime currentTime = LocalDateTime.now();

            try {
                // Connect to the database
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos",
                        "root", "Ilovesql123%");
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM schedulebackup WHERE "
                        + "DATEDIFF(date, NOW()) BETWEEN 1 AND 5 AND isexecuted = 0");

                ResultSet resultSet = ps.executeQuery();

                while (resultSet.next()) {
                    // Retrieve backup details from the database
                    int hour = resultSet.getInt("hour");
                    int minute = resultSet.getInt("minute");
                    int second = resultSet.getInt("second");
                    String meridian = resultSet.getString("meridian");
                    LocalDate backupDate = resultSet.getObject("date", LocalDate.class);

                    // Check if the current time matches the scheduled time
                    if (matchesScheduledTime(currentTime, hour, minute, second, meridian, backupDate)) {
                        // Trigger backup process
                        String backupPath = "C:\\Users\\Maryam_Otunara\\OneDrive\\Documents\\Backup_SQL"; // Set the backup path
                        backupDatabase(backupPath);

                        // Update database to mark the backup as executed
                        int scheduleId = resultSet.getInt("scheduleid");
                        markBackupAsExecuted(scheduleId);
                    }
                }

                resultSet.close();
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void backupDatabase(String path) {
            // Backup database

            System.out.println("Backing up to " + path);
            String currentTime = LocalDateTime.now().toString();
            String command = "mysqldump -u " + "root" + " -p" + "Ilovesql123%" + " pos -r" + path + "/" + currentTime + "backup.sql";
            //      mysqldump -u shadow -pchirp123 posDB -r"/home/shadow/Documents/backup/backup.sql"
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // String cmd = "mysqldump -u " + databaseUser +" -p"+databasepassword+ "--result-file=~/Documents/backup";
            //Runtime.getRuntime().exec(cmd);
        }

        public void markBackupAsExecuted(int scheduleId) {
            try {
                // Connect to the database
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

                // Prepare the SQL update statement
                PreparedStatement ps = connection.prepareStatement("UPDATE schedulebackup SET isexecuted = 1 WHERE scheduleid = ?");
                ps.setInt(1, scheduleId);

                // Execute the update statement
                int rowsAffected = ps.executeUpdate();

                // Check if the update was successful
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(rootPane, "Backup marked as executed for schedule ID: " + scheduleId);
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Failed to mark backup as executed for schedule ID: " + scheduleId);
                }

                // Close resources
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean matchesScheduledTime(LocalDateTime currentTime, int hour, int minute, int second, String meridian, LocalDate backupDate) {
            // Convert hour to 24-hour format if necessary
            if (meridian.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (meridian.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            // Check if current time matches the scheduled time
            return currentTime.getHour() == hour && currentTime.getMinute() == minute
                    && currentTime.getSecond() == second && currentTime.toLocalDate().isEqual(backupDate);
        }
    }

    public class BackupThread extends Thread {

        private volatile boolean running = true;
        private final BackupProcess backupProcess;

        public BackupThread(BackupProcess backupProcess) {
            this.backupProcess = backupProcess;
        }

        @Override
        public void run() {
            while (running) {
                // Execute backup process
                backupProcess.checkScheduledBackups();

                try {
                    // Sleep for a certain period before checking again
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
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
            java.util.logging.Logger.getLogger(sales_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(sales_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(sales_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(sales_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new sales_manager(staffDetails).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
