/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Maryam_Otunara
 */
public class cashier extends javax.swing.JFrame {

    static String[] staffDetails;
    String mailTopic;
    String mailBody;
    String productName;
    int currentQuantity;
    float totalPrice;

    /**
     * Creates new form cashier
     */
    public cashier(String[] staffDetails) {
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
                jLabel3.setText("Attending Cashier: " + staffName);
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

    private void updateStockQuantities() {
        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

            // Iterate through the items in the table
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String productName = model.getValueAt(i, 1).toString();
                int quantitySold = Integer.parseInt(model.getValueAt(i, 2).toString());

                // Retrieve current stock quantity from the database
                String query = "SELECT quantity FROM inventory WHERE productname=?";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setString(1, productName);
                    ResultSet resultSet = ps.executeQuery();

                    if (resultSet.next()) {
                        int currentQuantity = resultSet.getInt("quantity");
                        int newQuantity = currentQuantity - quantitySold;

                        // Update the stock quantity in the database
                        String updateQuery = "UPDATE inventory SET quantity=? WHERE productname=?";
                        try (PreparedStatement updatePs = con.prepareStatement(updateQuery)) {
                            updatePs.setInt(1, newQuantity);
                            updatePs.setString(2, productName);
                            updatePs.executeUpdate();
                        }
                    }
                }
            }

            // Close the database connection
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }

    // Method to generate receipt text
    private String generateReceiptText() {
        StringBuilder receiptBuilder = new StringBuilder();

        receiptBuilder.append("PAU Cooperative Supermarket\n");
        receiptBuilder.append("PAN-ATLANTIC UNIVERSITY\n\n");

        receiptBuilder.append("Product Name\t");
        receiptBuilder.append("Qty\t");
        receiptBuilder.append("Price\t");
        receiptBuilder.append("Total Amount\n");
        
        // Append transaction details from jTable1
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            receiptBuilder.append(model.getValueAt(i, 1)).append("\t")
                    .append(model.getValueAt(i, 2)).append("\t")
                    .append(model.getValueAt(i, 3)).append("\t")
                    .append(model.getValueAt(i, 4)).append("\n");
        }

        // Append total price
        receiptBuilder.append("Total Price: ").append(jTextField3.getText()).append("\n");

        return receiptBuilder.toString();
    }

    // Insert into sales table
    private int insertSalesRecord(Connection con) throws SQLException {
        String insertsalesQuery = "INSERT INTO sales (username, transactiontime, totalamount) VALUES (?, ?, ?)";
        try (PreparedStatement insertsalesPs = con.prepareStatement(insertsalesQuery, Statement.RETURN_GENERATED_KEYS)) {
            String cashierId = staffDetails[7];
            insertsalesPs.setString(1, cashierId);
            LocalDateTime checkoutTime = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(checkoutTime);
            insertsalesPs.setTimestamp(2, timestamp);
            insertsalesPs.setFloat(3, totalPrice);

            int affectedRows = insertsalesPs.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating sales record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = insertsalesPs.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating sales record failed, no ID obtained.");
                }
            }
        }
    }

    // Insert into salesitem table
    private void insertSalesItemRecords(Connection con, int transactionId, JTable jTable1) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        String insertsalesitemsQuery = "INSERT INTO salesitems "
                + "(transactionid, transactiontime, productcode, productname, quantity, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertsalesitemsPs = con.prepareStatement(insertsalesitemsQuery)) {
            for (int i = 0; i < model.getRowCount(); i++) {

                LocalDateTime checkoutTime = LocalDateTime.now();
                Timestamp timestamp = Timestamp.valueOf(checkoutTime);

                String productCode = model.getValueAt(i, 0).toString();
                String productName = model.getValueAt(i, 1).toString();
                int quantity = Integer.parseInt(model.getValueAt(i, 2).toString());
                float price = Float.parseFloat(model.getValueAt(i, 3).toString());

                insertsalesitemsPs.setInt(1, transactionId);
                insertsalesitemsPs.setTimestamp(2, timestamp);
                insertsalesitemsPs.setString(3, productCode);
                insertsalesitemsPs.setString(4, productName);
                insertsalesitemsPs.setInt(5, quantity);
                insertsalesitemsPs.setFloat(6, price);
                insertsalesitemsPs.executeUpdate();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("PAU Cooperative Supermarket");

        jLabel2.setFont(new java.awt.Font("Fira Code Light", 0, 12)); // NOI18N
        jLabel2.setText("Time Here");

        jButton1.setText("Logout");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setFont(new java.awt.Font("Fira Code Light", 0, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Code", "Product Name", "Quantity", "Price (NGN)", "Total Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel3.setText("Cashier Name Here");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Search for:");

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton2.setText("Insert");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Quantity");

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton4.setBackground(new java.awt.Color(0, 0, 102));
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Checkout");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Total Price:");

        jTextField3.setEditable(false);
        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton3.setText("Clear");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton5.setText("Delete");
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(264, 264, 264)
                        .addComponent(jLabel1)
                        .addGap(52, 52, 52)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(10, 10, 10))
                                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(42, 42, 42)
                                        .addComponent(jButton2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton3)
                                        .addGap(51, 51, 51)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(28, 28, 28)
                                        .addComponent(jTextField3))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4)
                                .addGap(94, 94, 94)
                                .addComponent(jButton5)
                                .addGap(86, 86, 86)))))
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jButton1)
                        .addGap(151, 151, 151)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4)
                        .addComponent(jButton5)))
                .addContainerGap())
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
        //Back to login or homepage

        String ROLE_SALES_CASHIER = "Sales Cashier";
        String ROLE_SALES_MANAGER = "Sales Manager";

        int choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to logout?");
        if (choice == JOptionPane.YES_OPTION) {
            String userRole = staffDetails[5];
            if (ROLE_SALES_CASHIER.equals(userRole)) {
                logoutAsSalesCashier();
            } else if (ROLE_SALES_MANAGER.equals(userRole)) {
                logoutAsSalesManager();
            } else {
                JOptionPane.showMessageDialog(rootPane, "Unknown user role: " + userRole);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void logoutAsSalesCashier() {
        dispose();
        new login().setVisible(true);
    }

    private void logoutAsSalesManager() {
        dispose();
        new sales_manager(staffDetails).setVisible(true);
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Insert the product
        try {
            String productname = jTextField1.getText();

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");
            String query = "SELECT * FROM inventory WHERE productname=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, productname);
                ResultSet resultSet = ps.executeQuery();

                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

                while (resultSet.next()) {

                    String retrievedProductCode = resultSet.getString("productcode");
                    String retrievedProductName = resultSet.getString("productname");
                    Date expiryDate = resultSet.getDate("expirydate");
                    Date currentDate = new Date(); // Current date

                    
                    if (expiryDate != null && currentDate.after(expiryDate)) {
                        JOptionPane.showMessageDialog(rootPane,
                                "The product " + retrievedProductName + " has expired and cannot be sold.");
                        return; 
                    }

                    int availableQuantity = resultSet.getInt("quantity"); // Available stock quantity
                    int enteredQuantity = Integer.parseInt(jTextField2.getText());

                    // Check if the entered quantity exceeds the available stock
                    if (enteredQuantity > availableQuantity) {
                        JOptionPane.showMessageDialog(rootPane, "Only "
                                + availableQuantity + " units of product " + retrievedProductName + " available.");
                        return; // Exit the method if quantity exceeds available stock
                    }

                    String strquantity = jTextField2.getText();
                    int quantity = Integer.parseInt(strquantity);

                    float price = resultSet.getFloat("price");

                    float totalAmount = price * quantity;

                    model.addRow(new Object[]{retrievedProductCode, retrievedProductName, quantity, price, totalAmount});

                    jTextField1.setText("");
                    jTextField2.setText("");
                }
            }

            // Calculate total price
            totalPrice = 0;
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                totalPrice += Float.parseFloat(jTable1.getValueAt(i, 3).toString());
            }

            // Display total price in jTextField3
            jTextField3.setText(String.valueOf(totalPrice));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        //Checkout

        var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to checkout?");
        if (choice == JOptionPane.YES_OPTION) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos",
                        "root", "Ilovesql123%");

                int transactionId = insertSalesRecord(con);
                insertSalesItemRecords(con, transactionId, jTable1);
                con.close();
                
                updateStockQuantities();

                displayReceipt(transactionId); // Display receipt

                // Clear the table and total price field after successful checkout
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.setRowCount(0);
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void displayReceipt(int transactionId) {

        LocalDateTime checkoutTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCheckoutTime = checkoutTime.format(formatter);
        String receiptText = generateReceiptText() + "\nCheckout Time: " + formattedCheckoutTime;
        new receipt(receiptText).setVisible(true);
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        //Clear fields

        jTextField1.setText("");
        jTextField2.setText("");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //Delete row
        
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int selectedRowIndex = jTable1.getSelectedRow();
        if (selectedRowIndex != -1) {

            model.removeRow(selectedRowIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            java.util.logging.Logger.getLogger(cashier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(cashier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(cashier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(cashier.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new cashier(staffDetails).setVisible(true);
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
