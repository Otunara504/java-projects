
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author Maryam_Otunara
 */
public class test extends javax.swing.JFrame {

    String testid;
    String email;
    String name;
    String matno;

    int currentQuestionIndex = 0;
    List<Question> questionList = new ArrayList<>();
    String studentoption = "";
    List<String> studentanswer = new ArrayList<>();

    int score = 0;

    //general details
    String coursename;
    String coursecode;
    String testnature;
    int noofquestions;

    /**
     * Creates new form test
     */
    public test(String testid, String email, String name, String matno) {
        initComponents();
        this.testid = testid;
        this.email = email;
        this.name = name;
        this.matno = matno;

        timer t = new timer();
        Thread t1 = new Thread(t);
        t1.start();

        general_details gd = new general_details();
        Thread gd1 = new Thread(gd);
        gd1.start();

        questions q = new questions();
        Thread q1 = new Thread(q);
        q1.start();
    }

    public class timer implements Runnable {

        @Override
        public void run() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/questionsbank", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("select * from timerdetails where testid=?");
                ps.setString(1, testid);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int hours = rs.getInt(2);
                    int minutes = rs.getInt(3);
                    int seconds = rs.getInt(4);

                    int totalSeconds = hours * 3600 + minutes * 60 + seconds;

                    while (totalSeconds > 0) {
                        int remainingHours = totalSeconds / 3600;
                        int remainingMinutes = (totalSeconds % 3600) / 60;
                        int remainingSeconds = totalSeconds % 60;

                        String formattedTime = String.format("Time remaining %02d:%02d:%02d%n",
                                remainingHours, remainingMinutes, remainingSeconds);
                        jLabel2.setText(formattedTime);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        totalSeconds--;
                    }

                    String formattedTime = String.format("Time remaining %02d:%02d:%02d%n", 0, 0, 0);
                    jLabel2.setText(formattedTime);
                    JOptionPane.showMessageDialog(rootPane, "Time Up", "Time Up", JOptionPane.INFORMATION_MESSAGE);

                    //Submit Thread
                    scriptSubmission s = new scriptSubmission();
                    Thread s2 = new Thread(s);
                    s2.start();

                    //Scores Script
                    scoresSubmission c = new scoresSubmission();
                    Thread c1 = new Thread(c);
                    c1.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class general_details implements Runnable {

        @Override
        public void run() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306"
                        + "/questionsbank", "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("select * from generaldetails where testid=?");
                ps.setString(1, testid);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String department = rs.getString(2);
                    String level = rs.getString(3);
                    coursename = rs.getString(4);
                    coursecode = rs.getString(5);
                    testnature = rs.getString(6);
                    noofquestions = rs.getInt(7);

                    String test_details = coursename + " (" + coursecode + ") " + testnature;
                    jLabel1.setText(test_details);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class questions implements Runnable {

        @Override
        public void run() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/questionsbank",
                        "root", "Ilovesql123%");
                PreparedStatement ps = con.prepareStatement("select * from questions where testid=?");
                ps.setString(1, testid);
                ResultSet rs = ps.executeQuery();

                questionList = new ArrayList<>();

                while (rs.next()) {
                    String number = rs.getString("s/n");
                    String questionText = rs.getString("question");
                    String optionA = rs.getString("optiona");
                    String optionB = rs.getString("optionb");
                    String optionC = rs.getString("optionc");
                    String optionD = rs.getString("optiond");
                    String answer = rs.getString("answer");

                    Question question = new Question(number, questionText, optionA, optionB, optionC, optionD, answer);
                    questionList.add(question);
                }

                // Randomize the order of questions
                Collections.shuffle(questionList);

                studentanswer = new ArrayList<>(Collections.nCopies(noofquestions, ""));

                // Display the first question
                displayQuestion();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void displayQuestion() {
            if (currentQuestionIndex < questionList.size()) {
                Question question = questionList.get(currentQuestionIndex);
                jTextField1.setText(question.getNumber());
                jTextArea1.setText(question.getQuestion());
                jRadioButton1.setText(question.getOptionA());
                jRadioButton2.setText(question.getOptionB());
                jRadioButton3.setText(question.getOptionC());
                jRadioButton4.setText(question.getOptionD());
            }
        }
    }

    // A  class that represent a question
    private static class Question {

        private String number;
        private String question;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String answer;

        public Question(String number, String question, String optionA, String optionB, String optionC, String optionD, String answer) {
            this.number = number;
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.answer = answer;
        }

        public String getNumber() {
            return number;
        }

        public String getQuestion() {
            return question;
        }

        public String getOptionA() {
            return optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public String getAnswer() {
            return answer;
        }
    }

    // Method to display a question
    private void displayQuestion(Question question) {
        jTextField1.setText(question.getNumber());
        jTextArea1.setText(question.getQuestion());
        jRadioButton1.setText(question.getOptionA());
        jRadioButton2.setText(question.getOptionB());
        jRadioButton3.setText(question.getOptionC());
        jRadioButton4.setText(question.getOptionD());
    }

// Method to select the appropriate radio button based on the stored answer
    private void selectRadioButton(String answer) {
        if (answer.equals(jRadioButton1.getText())) {
            jRadioButton1.setSelected(true);
        } else if (answer.equals(jRadioButton2.getText())) {
            jRadioButton2.setSelected(true);
        } else if (answer.equals(jRadioButton3.getText())) {
            jRadioButton3.setSelected(true);
        } else if (answer.equals(jRadioButton4.getText())) {
            jRadioButton4.setSelected(true);
        }
    }

    public String useAnswer() {
        Question question = questionList.get(currentQuestionIndex);
        String correctAnswer = question.getAnswer();
        return correctAnswer;
    }

    //Method to get the selected option from the radio buttons
    private String getSelectedOption() {
        if (jRadioButton1.isSelected()) {
            return jRadioButton1.getText();
        } else if (jRadioButton2.isSelected()) {
            return jRadioButton2.getText();
        } else if (jRadioButton3.isSelected()) {
            return jRadioButton3.getText();
        } else if (jRadioButton4.isSelected()) {
            return jRadioButton4.getText();
        }
        return "N/A"; // Or some default value if none is selected
    }

    public class mail implements Runnable {
        //mail thread

        @Override
        public void run() {
            try {
                String subject = "CBT Password";
                String receiver = email;
                String body = "Dear " + name + ", "
                        + "\n   Your score for " + coursename + " (" + coursecode + ") " + testnature + "is " + score;

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
            } catch (Exception e) {
                System.out.println("error");
            }
        }
    }

    //Scripts submission
    public class scriptSubmission implements Runnable {

        @Override
        public void run() {
            // Perform database submission here
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/responsebank", "root", "Ilovesql123%");

                String sql = "INSERT INTO scripts (testid, matno, name, `s/n`, question, optiona, optionb, optionc, optiond, answer, response) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int i = 0; i < questionList.size(); i++) {
                        Question question = questionList.get(i);
                        String studentAnswer = studentanswer.get(i);

                        ps.setString(1, testid);
                        ps.setString(2, matno);
                        ps.setString(3, name);
                        ps.setString(4, question.getNumber());
                        ps.setString(5, question.getQuestion());
                        ps.setString(6, question.getOptionA());
                        ps.setString(7, question.getOptionB());
                        ps.setString(8, question.getOptionC());
                        ps.setString(9, question.getOptionD());
                        ps.setString(10, question.getAnswer());
                        ps.setString(11, studentAnswer);

                        ps.addBatch(); // Add the current set of parameters to the batch
                    }

                    // Execute the batch
                    ps.executeBatch();
                    JOptionPane.showMessageDialog(rootPane, "Response submitted");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception appropriately (logging, showing a message to the user, etc.)
            }
        }
    }

    //Scores Submission
    public class scoresSubmission implements Runnable {

        @Override
        public void run() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/responsebank", "root", "Ilovesql123%");

                String sql = "INSERT INTO scores (testid, matno, name, coursename, coursecode, score) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, testid);
                    ps.setString(2, matno);
                    ps.setString(3, name);
                    ps.setString(4, coursename);
                    ps.setString(5, coursecode);
                    ps.setInt(6, score);

                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(rootPane, "Scores uploaded");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception appropriately (logging, showing a message to the user, etc.)
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 245, 204));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("jLabel1");

        jLabel2.setText("jLabel2");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("jRadioButton1");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("jRadioButton2");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("jRadioButton3");

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("jRadioButton4");

        jButton1.setText("<- Previous");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Next ->");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Submit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Calculator");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel3.setText("No.");

        jTextField1.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(94, 94, 94))
            .addComponent(jSeparator1)
            .addComponent(jScrollPane1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(jButton1)
                .addGap(108, 108, 108)
                .addComponent(jButton2)
                .addGap(111, 111, 111)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(72, 72, 72))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButton4)
                            .addComponent(jRadioButton3)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton1)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1)
                        .addGap(30, 30, 30))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jRadioButton1)
                .addGap(27, 27, 27)
                .addComponent(jRadioButton2)
                .addGap(27, 27, 27)
                .addComponent(jRadioButton3)
                .addGap(28, 28, 28)
                .addComponent(jRadioButton4)
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap(29, Short.MAX_VALUE))
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

        // Previous button
        if (currentQuestionIndex > 0) {
            // Get the selected answer and add it to the list
            String studentOption = getSelectedOption();
            studentanswer.set(currentQuestionIndex, studentOption);

            --currentQuestionIndex;

            buttonGroup1.clearSelection();

            Question question = questionList.get(currentQuestionIndex);

            displayQuestion(question);

            String answerForCurrentQuestion = studentanswer.get(currentQuestionIndex);

            // Select the appropriate radio button based on the stored answer
            selectRadioButton(answerForCurrentQuestion);
        } else {
            JOptionPane.showMessageDialog(rootPane, "This is the first Question");
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        // Next button
        if (currentQuestionIndex < noofquestions - 1) {
            // Get the selected answer and add it to the list
            String studentOption = getSelectedOption();
            studentanswer.set(currentQuestionIndex, studentOption);

            ++currentQuestionIndex;

            // Check if the index is within the bounds of the question list
            if (currentQuestionIndex < questionList.size()) {
                Question question = questionList.get(currentQuestionIndex);

                displayQuestion(question);

                buttonGroup1.clearSelection();

                // Retrieve the answer for the current question if it was answered
                if (currentQuestionIndex < studentanswer.size()) {
                    String answerForCurrentQuestion = studentanswer.get(currentQuestionIndex);
                    // Select the appropriate radio button based on the stored answer
                    selectRadioButton(answerForCurrentQuestion);

                    // Check if the current question has been answered before
                    if (!studentOption.isEmpty()) {
                        // Check if the answer is correct
                        if (studentOption.equals(useAnswer())) {
                            score = score + 5;
                        }
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "This is the last Question");
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Submit button

        try {
            // Get the selected answer and add it to the list
            String studentOption = getSelectedOption();
            studentanswer.add(studentOption);

            var choice = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to submit?");
            if (choice == JOptionPane.YES_OPTION) {
                if (studentoption.equals(useAnswer())) {
                    score += 5;
                }
                String str_score = Integer.toString(score);
                jTextArea1.setText("Your total score is " + str_score);

                mail m = new mail();
                Thread m1 = new Thread(m);
                m1.start();

                //Submit script
                scriptSubmission s = new scriptSubmission();
                Thread s1 = new Thread(s);
                s1.start();

                //Scores Script
                scoresSubmission c = new scoresSubmission();
                Thread c1 = new Thread(c);
                c1.start();
 
                //Switch to script frame
                dispose();
                new script(name,matno,testid,email).setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Calculator button

        try {
            Runtime.getRuntime().exec("calc");
        } catch (Exception e) {

        }
    }//GEN-LAST:event_jButton4ActionPerformed

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
            java.util.logging.Logger.getLogger(test.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(test.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(test.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(test.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new test("", "", "", "").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
