/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

/**
 *
 * @author Maryam_Otunara
 */

public class AudioCallThread implements Runnable {

    private ServerSocket serverSocket;
    private Server serverform;
    private volatile boolean startServer = true;

    private TargetDataLine targetLine;
    private DataLine.Info captureInfo;
    private SourceDataLine sourceLine;
    private DataLine.Info playbackInfo;

    public AudioCallThread(int port, Server serverform) {
        try {
            this.serverform = serverform;
            serverSocket = new ServerSocket(port);
            serverform.appendMessage(String.format("Audio Server is running on port %d.\n", port));
        } catch (IOException e) {
            if (e instanceof java.net.BindException) {
                serverform.appendMessage("Port " + port + " is already in use.\n");
            } else {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
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

            serverform.appendMessage("Audio Call Server is up.\n");

            while (startServer) {
                Socket clientSocket = serverSocket.accept();
                serverform.appendMessage("Client connected.\n");

                // Handle client connection in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure resources are properly released
            closeResources();
        }
    }

    public void stopServer() {
        startServer = false;
        closeResources();
        serverform.appendMessage("Server is now closed.\n");
    }

    private void closeResources() {
        if (targetLine != null && targetLine.isOpen()) {
            targetLine.stop();
            targetLine.close();
        }
        if (sourceLine != null && sourceLine.isOpen()) {
            sourceLine.stop();
            sourceLine.close();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private volatile boolean activeConnection = true;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];

                // Capture audio thread
                Thread captureThread = new Thread(() -> {
                    while (activeConnection && startServer) {
                        try {
                            int bytesRead = targetLine.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            activeConnection = false;
                            break;
                        }
                    }
                });
                captureThread.start();

                // Playback audio thread
                Thread playbackThread = new Thread(() -> {
                    while (activeConnection && startServer) {
                        try {
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead > 0) {
                                sourceLine.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            activeConnection = false;
                            break;
                        }
                    }
                });
                playbackThread.start();

                // Wait for threads to finish
                captureThread.join();
                playbackThread.join();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                closeClientResources();
            }
        }

        private void closeClientResources() {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}