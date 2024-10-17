/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Maryam_Otunara
 */
public class VoiceNoteThread implements Runnable {

    private TargetDataLine line;
    private ByteArrayOutputStream byteArrayOutputStream;
    private boolean isRecording = false;
    private TimerRunnable timerRunnable;
    private Thread timerThread;
    private Client clientFrame;
    private Socket socket;

    public VoiceNoteThread(Client clientFrame, Socket socket) {
        this.clientFrame = clientFrame;
        this.socket = socket;
    }

    @Override
    public void run() {
        startRecording();
    }

    public void startRecording() {
        final int sampleRate = 16000;
        final int SampleSizeInBits = 16;
        final int channel = 1;
        final boolean bigEndian = false;

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sampleRate, SampleSizeInBits,
                channel, (SampleSizeInBits / 8) * channel,
                sampleRate, bigEndian);

        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            System.out.println(info);
            line.open(format);
            line.start();

            byteArrayOutputStream = new ByteArrayOutputStream();
            isRecording = true;

            Thread recordingThread = new Thread(() -> {
                try {
                    byte buffer[] = new byte[line.getBufferSize() / 5];
                    while (isRecording) {
                        int count = line.read(buffer, 0, buffer.length);
                        if (count > 0) {
                            byteArrayOutputStream.write(buffer, 0, count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            recordingThread.start();

            // Start timer thread
            timerRunnable = new TimerRunnable();
            timerThread = new Thread(timerRunnable);
            timerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void stopRecording() {
        isRecording = false;
        line.stop();
        line.close();
        
        if (timerRunnable != null) {
            timerRunnable.stop();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Recorded Audio");
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
        int userSelection = fileChooser.showSaveDialog(clientFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".wav")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".wav");
            }
            try {
                AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                        line.getFormat(), byteArrayOutputStream.size());
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, fileToSave);
                audioInputStream.close();
                JOptionPane.showMessageDialog(clientFrame, "Recording saved successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(clientFrame, "Error saving recording.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }*/
    public void stopRecording() {
        if (line == null) {
            System.err.println("TargetDataLine is null, cannot stop recording.");
            return;
        }

        isRecording = false;
        line.stop();
        line.close();

        // Save recorded audio to a file
        File audioFile = saveRecordedAudio();

        // Send the saved audio file to the server
        if (audioFile != null) {
            sendAudioFileToServer(audioFile);
        }
    }

    private File saveRecordedAudio() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Recorded Audio");
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
        int userSelection = fileChooser.showSaveDialog(clientFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".wav")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".wav");
            }
            try {
                AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                        line.getFormat(), byteArrayOutputStream.size());
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, fileToSave);
                audioInputStream.close();
                JOptionPane.showMessageDialog(clientFrame, "Recording saved successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                return fileToSave;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(clientFrame, "Error saving recording.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private void sendAudioFileToServer(File audioFile) {
        try {
            // Open a data output stream to send the file
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // Send command to indicate file transfer
            dos.writeUTF("CMD_SEND_AUDIO_FILE");

            // Send file name
            dos.writeUTF(audioFile.getName());

            // Send file content
            byte[] buffer = new byte[4096];
            int bytesRead;
            FileInputStream fis = new FileInputStream(audioFile);
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            // Clean up
            fis.close();
            dos.flush();
            JOptionPane.showMessageDialog(clientFrame, "File sent successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(clientFrame, "Error sending file to server.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class TimerRunnable implements Runnable {

        private volatile boolean running = true;
        private int seconds = 0;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    clientFrame.jToggleButton1.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
                    Thread.sleep(1000);
                    seconds++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
    }
}
