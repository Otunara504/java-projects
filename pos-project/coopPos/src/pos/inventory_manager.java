/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pos;

import java.io.BufferedReader;
import java.io.FileReader;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Maryam_Otunara
 */
public class inventory_manager extends javax.swing.JFrame {

    static String[] staffDetails;
    String mailTopic;
    String mailBody;

    /**
     * Creates new form inventory_manager
     */
    public inventory_manager(String[] staffDetails) {
        initComponents();

        this.staffDetails = staffDetails;

        time t = new time();
        Thread t1 = new Thread(t);
        t1.start();

        name n = new name();
        Thread n1 = new Thread(n);
        n1.start();

        lowStockNotificationTask l = new lowStockNotificationTask();
        Thread l1 = new Thread(l);
        l1.start();

        displayBirthdayMessage d = new displayBirthdayMessage();
        Thread d1 = new Thread(d);
        d1.start();

        expirationNotificationTask e = new expirationNotificationTask();
        Thread e1 = new Thread(e);
        e1.start();

        loadLowStockItems o = new loadLowStockItems();
        Thread o1 = new Thread(o);
        o1.start();
        
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
                jLabel3.setText("Inventory Manager: " + staffName);
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

                StringBuilder notificationMessage = new StringBuilder("Low stock for the following products:\n");

                while (rs.next()) {
                    String productName = rs.getString("productname");
                    int currentQuantity = rs.getInt("quantity");

                    // Append product information to the notification message
                    notificationMessage.append(productName).append(": ").append(currentQuantity).append("\n");
                }

                if (notificationMessage.length() > "Low stock for the following products:\n".length()) {
                    JOptionPane.showMessageDialog(null, notificationMessage.toString());

                    mailTopic = "LOW STOCK NOTIFICATION";
                    mailBody = "Low stock for the following products " + notificationMessage.toString();
                    //Instantiate mail thread
                    mail m = new mail();
                    Thread m1 = new Thread(m);
                    m1.start();
                }

                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class loadLowStockItems implements Runnable {

        @Override
        public void run() {
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            model.setRowCount(0);

            try {

                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos",
                        "root", "Ilovesql123%");
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM inventory WHERE quantity < 50");
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String productCode = resultSet.getString("productcode");
                    String productName = resultSet.getString("productname");
                    int currentQuantity = resultSet.getInt("quantity");
                    float price = resultSet.getFloat("price");

                    model.addRow(new Object[]{productCode, productName, currentQuantity, price, null});
                }

                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(rootPane, "Error loading low stock items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jDateChooser4 = new com.toedter.calendar.JDateChooser();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(241, 235, 251));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Inventory");

        jLabel2.setFont(new java.awt.Font("Fira Code Light", 0, 14)); // NOI18N
        jLabel2.setText("Time Here");

        jButton1.setText("Logout");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel2.setBackground(new java.awt.Color(255, 255, 204));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Product Name");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Product Code");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Manufacturer");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Manufacturing Date");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Expiry Date");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Quantity");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Price");

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jDateChooser1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jDateChooser2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jSpinner1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5000, 1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("NGN");

        jTextField4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton2.setBackground(new java.awt.Color(0, 51, 102));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Stock");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton10.setBackground(new java.awt.Color(0, 51, 102));
        jButton10.setForeground(new java.awt.Color(255, 255, 255));
        jButton10.setText("Clear");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(65, 65, 65)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                            .addComponent(jTextField1)
                            .addComponent(jTextField2)
                            .addComponent(jTextField3)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSpinner1)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(121, 121, 121)
                        .addComponent(jButton2)
                        .addGap(154, 154, 154)
                        .addComponent(jButton10)))
                .addContainerGap(197, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(32, 32, 32)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addComponent(jLabel7))
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addComponent(jLabel8))
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton10))
                .addGap(43, 43, 43))
        );

        jTabbedPane1.addTab("Stock New Product", jPanel2);

        jPanel3.setBackground(new java.awt.Color(229, 248, 229));

        jLabel12.setText("Select File");

        jButton3.setText("Browse");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Code", "Product Name", "Manufacturer", "Manu Date", "Expiry Date", "Quantity", "Price"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton4.setText("Upload");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Clear");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jLabel12)
                .addGap(38, 38, 38)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(103, 103, 103))
            .addComponent(jScrollPane1)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(104, 104, 104)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addGap(107, 107, 107))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Stock in Bulk", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 204, 204));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Product Code");

        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton6.setText("Search");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Product Name");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Manufacturer");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("Manufacturing Date");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Expiry Date");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Quantity");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Price");

        jTextField7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jTextField8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jDateChooser3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jDateChooser4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jSpinner2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5000, 1));
        jSpinner2.setEnabled(false);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel20.setText("NGN");

        jTextField9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton7.setText("Update");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Delete");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("Clear");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jLabel13)
                        .addGap(41, 41, 41)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jButton6))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jButton7))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addComponent(jLabel20)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                                    .addComponent(jTextField7)
                                    .addComponent(jTextField8)
                                    .addComponent(jDateChooser3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jDateChooser4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSpinner2)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(64, 64, 64)
                                .addComponent(jButton8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton9)))))
                .addGap(87, 87, 87))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(38, 38, 38)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel15)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(40, 40, 40)
                                .addComponent(jLabel16))
                            .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(48, 48, 48)
                        .addComponent(jLabel17))
                    .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Update/Delete Product", jPanel4);

        jPanel5.setBackground(new java.awt.Color(241, 251, 251));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Code", "Product Name", "Quantity", "Price"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jButton11.setText("Update");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("Clear");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Refresh");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jButton11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton13)
                .addGap(148, 148, 148)
                .addComponent(jButton12)
                .addGap(85, 85, 85))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12)
                    .addComponent(jButton13))
                .addContainerGap(47, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Low Stock Management", jPanel5);

        jLabel3.setText("Name Here");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(29, 29, 29)
                .addComponent(jLabel2)
                .addGap(235, 235, 235))
            .addComponent(jTabbedPane1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jButton1))
                .addGap(11, 11, 11)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Stock New Products

        try {
            String productcode = jTextField2.getText();
            String productname = jTextField1.getText();
            String manufacturer = jTextField3.getText();

            SimpleDateFormat dat = new SimpleDateFormat("yyyy-MM-dd");
            String manufacturingdate = dat.format(jDateChooser1.getDate());

            SimpleDateFormat dat2 = new SimpleDateFormat("yyyy-MM-dd");
            String expirydate = dat2.format(jDateChooser2.getDate());

            int quantity = (int) jSpinner1.getValue();

            String strprice = jTextField4.getText();
            float price = Float.parseFloat(strprice);

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                    + "/pos", "root", "Ilovesql123%");
            PreparedStatement ps = con.prepareStatement("insert into inventory values (?,?,?,?,?,?,?)");

            ps.setString(1, productcode);
            ps.setString(2, productname);
            ps.setString(3, manufacturer);
            ps.setString(4, manufacturingdate);
            ps.setString(5, expirydate);
            ps.setInt(6, quantity);
            ps.setFloat(7, price);

            int rs = ps.executeUpdate();
            JOptionPane.showMessageDialog(rootPane, productcode + " stocked");

            jTextField1.setText(null);
            jTextField2.setText(null);
            jTextField3.setText(null);
            jTextField4.setText(null);
            jDateChooser1.setDate(null);
            jDateChooser1.updateUI();
            jDateChooser2.setDate(null);
            jDateChooser2.updateUI();
            jSpinner1.setModel(new SpinnerNumberModel());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        //Clear fields for stocking

        jTextField1.setText(null);
        jTextField2.setText(null);
        jTextField3.setText(null);
        jTextField4.setText(null);
        jDateChooser1.setDate(null);
        jDateChooser1.updateUI();
        jDateChooser2.setDate(null);
        jDateChooser2.updateUI();
        jSpinner1.setModel(new SpinnerNumberModel());
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        //Browse for CSV file

        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");

        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.addChoosableFileFilter(filter);

        int open = jFileChooser1.showOpenDialog(null);

        if (open != jFileChooser1.APPROVE_OPTION) {
            return;
        } else {
            String filename = jFileChooser1.getSelectedFile().getAbsolutePath().toString();
            String file = jFileChooser1.getSelectedFile().getName().toString();
            jTextField5.setText(filename);
            if (!filename.contains("csv")) {
                filename = filename + ".csv";
            }

            try {
                String line;
                DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
                BufferedReader br = new BufferedReader(new FileReader(jFileChooser1.getSelectedFile()));
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    tableModel.addRow(data);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, "Error Reading File");
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        //Upload CSV file

        try {
            DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
            String insertQuery = "INSERT INTO inventory VALUES (?,?,?,?,?,?,?)";
            Class.forName("com.mysql.cj.jdbc.Driver");
            String path = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String pass = "Ilovesql123%";

            java.sql.Connection con = DriverManager.getConnection(path, user, pass);

            PreparedStatement preparedStatement = con.prepareStatement(insertQuery);

            for (int row = 0; row < jTable1.getRowCount(); row++) {
                for (int col = 0; col < jTable1.getColumnCount(); col++) {
                    Object value = jTable1.getValueAt(row, col);
                    preparedStatement.setObject(col + 1, value);
                }

                preparedStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(rootPane, "Product's Stocked");
            tableModel.setRowCount(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Clear Table

        jTextField5.setText("");
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Search for product

        try {
            String productcode = jTextField6.getText();

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                    + "/pos", "root", "Ilovesql123%");
            PreparedStatement ps = con.prepareStatement("select * from inventory where productcode=?");

            ps.setString(1, productcode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jTextField7.setText(rs.getString(2));
                jTextField8.setText(rs.getString(3));

                String dateStr = rs.getString(4);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(dateStr);
                    jDateChooser3.setDate(date);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(rootPane, "Error parsing date: " + e.getMessage());
                }

                String dateStr2 = rs.getString(5);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(dateStr2);
                    jDateChooser4.setDate(date);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(rootPane, "Error parsing date: " + e.getMessage());
                }

                jSpinner2.setValue(rs.getInt(6));

                float fltprice = rs.getFloat(7);
                String strprice = String.valueOf(fltprice);
                jTextField9.setText(strprice);

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // Update Product

        var choice = JOptionPane.showConfirmDialog(rootPane,
                "Are you sure you want to update this product's details?");
        if (choice == JOptionPane.YES_OPTION) {

            try {
                String productcode = jTextField6.getText();
                String productname = jTextField7.getText();
                String manufacturer = jTextField8.getText();

                Date selectedDate = jDateChooser3.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String manufacturingdate = sdf.format(selectedDate);

                Date selectedDate2 = jDateChooser4.getDate();
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                String expirydate = sdf2.format(selectedDate2);

                int quantity = (int) jSpinner2.getValue();

                String strprice = jTextField9.getText();
                float price = Float.parseFloat(strprice);

                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/pos", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("update inventory set productname=?, manufacturer=?,"
                        + "manufacturingdate=?, expirydate=?, quantity=?, price=? where productcode=?");

                ps.setString(1, productname);
                ps.setString(2, manufacturer);
                ps.setString(3, manufacturingdate);
                ps.setString(4, expirydate);
                ps.setInt(5, quantity);
                ps.setFloat(6, price);
                ps.setString(7, productcode);

                int rs = ps.executeUpdate();

                JOptionPane.showMessageDialog(rootPane, productcode + "'s details updated");

                jTextField6.setText(null);
                jTextField7.setText(null);
                jTextField8.setText(null);
                jTextField9.setText(null);
                jDateChooser3.setDate(null);
                jDateChooser3.updateUI();
                jDateChooser4.setDate(null);
                jDateChooser4.updateUI();
                jSpinner2.setModel(new SpinnerNumberModel());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // Delete product

        var choice = JOptionPane.showConfirmDialog(rootPane,
                "Are you sure you want to delete this product?");

        if (choice == JOptionPane.YES_OPTION) {
            try {
                String productcode = jTextField6.getText();

                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/pos", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("delete from inventory where productcode=?");

                ps.setString(1, productcode);
                int rs = ps.executeUpdate();
                JOptionPane.showMessageDialog(rootPane, productcode + "'s record deleted");

                jTextField6.setText(null);
                jTextField7.setText(null);
                jTextField8.setText(null);
                jTextField9.setText(null);
                jDateChooser1.setDate(null);
                jDateChooser1.updateUI();
                jDateChooser2.setDate(null);
                jDateChooser2.updateUI();
                jSpinner1.setModel(new SpinnerNumberModel());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        } else {
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // Clear fields in update product

        jTextField6.setText(null);
        jTextField7.setText(null);
        jTextField8.setText(null);
        jTextField9.setText(null);
        jDateChooser3.setDate(null);
        jDateChooser3.updateUI();
        jDateChooser4.setDate(null);
        jDateChooser4.updateUI();
        jSpinner2.setModel(new SpinnerNumberModel());
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Back to login page

        var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to logout?");
        if (choice == JOptionPane.YES_OPTION) {

            dispose();
            new login().setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // Upload new stock numbers

        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to upload.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");
            PreparedStatement updateStatement = connection.prepareStatement("UPDATE inventory SET quantity = ? WHERE productcode = ?");

            String productCode = (String) model.getValueAt(selectedRow, 0);
            int newQuantity = Integer.parseInt(model.getValueAt(selectedRow, 2).toString());

            // Check if the new quantity is greater than the threshold quantity
            if (newQuantity > 50) {
                updateStatement.setInt(1, newQuantity);
                updateStatement.setString(2, productCode);
                updateStatement.executeUpdate();

                // Remove the row from the table
                model.removeRow(selectedRow);

                JOptionPane.showMessageDialog(this, "Stock number updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 50 to update stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating stock number: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        //Clear

        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        //Refresh 
        
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
        
        loadLowStockItems o = new loadLowStockItems();
        Thread o1 = new Thread(o);
        o1.start();
    }//GEN-LAST:event_jButton13ActionPerformed

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
            java.util.logging.Logger.getLogger(inventory_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(inventory_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(inventory_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(inventory_manager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new inventory_manager(staffDetails).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private com.toedter.calendar.JDateChooser jDateChooser4;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
