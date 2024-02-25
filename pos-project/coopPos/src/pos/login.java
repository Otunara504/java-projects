/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pos;

//import com.mysql.cj.jdbc.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */
public class login extends javax.swing.JFrame {

    /**
     * Creates new form login
     */
    public login() {
        initComponents();
    }

    private boolean authenticateSales(String username, String password) {
        boolean isValidSales = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

            String query = "SELECT * FROM staffinfo WHERE username = ? AND password = ? AND department = 'Sales Manager'";
            try (PreparedStatement ps = con.prepareStatement(query)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    isValidSales = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValidSales;
    }

    private boolean authenticateCashier(String username, String password) {
        boolean isValidCashier = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

            String query = "SELECT * FROM staffinfo WHERE username = ? AND password = ? AND department = 'Sales Cashier'";
            try (PreparedStatement ps = con.prepareStatement(query)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    isValidCashier = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValidCashier;
    }

    private boolean authenticateInventory(String username, String password) {
        boolean isValidInventory = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

            String query = "SELECT * FROM staffinfo WHERE username = ? AND password = ? AND department = 'Inventory'";
            try (PreparedStatement ps = con.prepareStatement(query)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    isValidInventory = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValidInventory;
    }

    private boolean authenticateIT(String username, String password) {
        boolean isValidIT = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pos", "root", "Ilovesql123%");

            String query = "SELECT * FROM staffinfo WHERE username = ? AND password = ? AND department = 'IT'";
            try (PreparedStatement ps = con.prepareStatement(query)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    isValidIT = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValidIT;
    }

    private String[] getStaffDetails(String username, String password) {
        String[] staffDetailsArray = new String[8];

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                    + "/pos", "root", "Ilovesql123%");
            PreparedStatement ps = con.prepareStatement("select surname, firstname, gender, dob,"
                    + "email, department, phoneno, photo, username"
                    + " from staffinfo where username=? AND password=?");

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                staffDetailsArray[0] = rs.getString("surname");
                staffDetailsArray[1] = rs.getString("firstname");
                staffDetailsArray[2] = rs.getString("gender");
                staffDetailsArray[3] = rs.getString("dob");
                staffDetailsArray[4] = rs.getString("email");
                staffDetailsArray[5] = rs.getString("department");
                staffDetailsArray[6] = rs.getString("phoneno");
                staffDetailsArray[7] = rs.getString("username");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return staffDetailsArray;
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
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("POS Management System");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(242, 19, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Login");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(363, 80, -1, -1));

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Username");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Password");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 250, -1, -1));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 170, 200, -1));

        jPasswordField1.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jPanel1.add(jPasswordField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 250, 200, -1));

        jCheckBox1.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jCheckBox1.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox1.setText("Show Password");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });
        jPanel1.add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 250, -1, -1));

        jButton1.setBackground(new java.awt.Color(0, 0, 102));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 300, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(204, 204, 255));
        jLabel5.setText("<html><u>Forgot Password?</u>");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 340, -1, -1));

        jLabel6.setIcon(new javax.swing.ImageIcon("C:\\Users\\Maryam_Otunara\\OneDrive\\Desktop\\SCHOOL\\Yr3 Sem1\\CSC 301\\EXAM\\login pic.jpg")); // NOI18N
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 500));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 500));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Login

        String username = jTextField1.getText();
        char[] p1 = jPasswordField1.getPassword();
        String password = String.valueOf(p1);

        if (authenticateSales(username, password)) {
            String[] staffDetails = this.getStaffDetails(username, password);

            JOptionPane.showMessageDialog(this, "Login successful");
            dispose();
            new sales_manager(staffDetails).setVisible(true);

        }
        else if (authenticateCashier(username, password)) {
            String[] staffDetails = this.getStaffDetails(username, password);

            JOptionPane.showMessageDialog(this, "Login successful");
            dispose();
            new cashier(staffDetails).setVisible(true);

        } 
        else if (authenticateInventory(username, password)) {
            String[] staffDetails = this.getStaffDetails(username, password);

            JOptionPane.showMessageDialog(this, "Login successful");
            dispose();
            new inventory_manager(staffDetails).setVisible(true);

        } 
        else if (authenticateIT(username, password)) {
            String[] staffDetails = this.getStaffDetails(username, password);

            JOptionPane.showMessageDialog(this, "Login successful");

            dispose();
            new it_homepage(staffDetails).setVisible(true);
            
        } else {
            //System.out.println("Invalid credentials for user: " + username);
            JOptionPane.showMessageDialog(this, "Invalid credentials");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        //Show Password

        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            jPasswordField1.setEchoChar((char) 0); // Show the password
        } else {
            jPasswordField1.setEchoChar('*'); // Hide the password
        }
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // Switching to apply for password page using forgot password label
        dispose();
        new password_application().setVisible(true);
    }//GEN-LAST:event_jLabel5MouseClicked

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
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
