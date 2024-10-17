/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */

public class ServerThread implements Runnable {
    
    ServerSocket server;
    Server serverform;
    boolean StartServer = true;
    
    public ServerThread(int port, Server serverform) {
        try {
            this.serverform = serverform;
            server = new ServerSocket(port);
            serverform.appendMessage(String.format("Server is running on port %d. !\n", port));
        } catch (IOException e) {
            if (e instanceof java.net.BindException) {
                JOptionPane.showMessageDialog(null, "Port " + port + " is already in use.");
            } else {
                System.out.println(e);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (StartServer) {
                Socket socket = server.accept();
                new Thread(new SocketThread(socket, serverform)).start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void stop() {
        try {
            server.close();
            StartServer = false;
            JOptionPane.showMessageDialog(null, "Server is now closed..!");
            serverform.appendMessage("Server closed.! \n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}