/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Maryam_Otunara
 */
public class dbconnection {

    private Connection dbcon;
    public Statement stm;
    public ResultSet rs;

    private final String driver = "com.mysql.cj.jdbc.Driver";
    private final String path = "jdbc:mysql://localhost:3306/chatapp";
    private final String user = "root";
    private final String pass = "Ilovesql123%";

    public dbconnection() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbcon = DriverManager.getConnection(path, user, pass);
            System.out.println("Connected");
        } catch (Exception e) {

        }
    }

    public PreparedStatement psStatement(String sqlStatement) {
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement(sqlStatement);
            System.out.println("PS Connected");
        } catch (Exception ex) {

        }

        return ps;
    }

    public void close() {
        try {
            dbcon.close();
        } catch (Exception ex) {

        }
    }

    public void executeSQL(String SQL) {
        try {
            stm = dbcon.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stm.executeQuery(SQL);
        } catch (Exception e) {

        }
    }
}
