package shakey.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SplashScreen extends JWindow {

    static boolean isRegistered;
    private static JProgressBar progressBar = new JProgressBar();
    private static SplashScreen execute;
    private static int count;
    private static Timer timer1;
    private final JLabel message;

    public SplashScreen() {

        Container container = getContentPane();
        container.setLayout(null);

        JPanel panel = new JPanel();
        panel.setBorder(new javax.swing.border.EtchedBorder());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBounds(10, 10, 348, 150);
        panel.setLayout(null);
        container.add(panel);

        JLabel label = new JLabel("天量报警工具");
        label.setFont(new Font("Verdana", Font.BOLD, 16));
        label.setBounds(125, 25, 280, 30);
        panel.add(label);

        label = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;感谢 心灵捕手、华美、<br/> &lt;&lt;美股技术讨论群&gt;&gt;的兄弟姐妹们！</html>");
        label.setFont(new Font("Verdana", Font.BOLD, 12));
        label.setBounds(60, 55, 280, 60);
        panel.add(label);
        message = new JLabel("loading ...");
        message.setFont(new Font("Verdana", Font.BOLD, 10));
        message.setBounds(11, 105, 340, 30);
        panel.add(message);

        progressBar.setMaximum(50);
        progressBar.setBounds(55, 180, 250, 15);
        container.add(progressBar);
        loadProgressBar();
        setSize(370, 215);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadProgressBar() {
        ActionListener al = new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                count++;

                progressBar.setValue(count);
                message.setText(count + "");

                System.out.println(count);

                if (count == 300) {

                    createFrame();

                    execute.setVisible(false);//swapped this around with timer1.stop()

                    timer1.stop();
                }

            }

            private void createFrame() throws HeadlessException {
                JFrame frame = new JFrame();
                frame.setSize(500, 500);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        };
        timer1 = new Timer(50, al);
        timer1.start();
    }

    public static void main(String[] args) {
        execute = new SplashScreen();
    }
};
