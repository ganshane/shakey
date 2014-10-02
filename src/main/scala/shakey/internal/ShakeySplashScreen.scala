package shakey.internal

import javax.swing._
import java.awt.{Font, Color, Container}
import java.awt.event.{WindowEvent, ActionEvent, ActionListener}
import javax.swing.border.EtchedBorder
import shakey.config.ShakeyConfig
import shakey.app.ShakeyApp

/**
 * Created by jcai on 14-9-29.
 */
class ShakeySplashScreen(config: ShakeyConfig, notifier: MessageNotifierService) extends JFrame("天量检测工具") {
  private val progressBar: JProgressBar = new JProgressBar
  private var timer1: Timer = null
  private var messageLabel: JLabel = null
  private var messageLabel2: JLabel = null
  private var sysMessageLabel: JLabel = null
  private var count: Int = 0
  private var total: Int = 0
  private var message: String = "loading ....";
  private var errorMessage: String = null;
  private var sysMessage: String = null;
  {
    val container: Container = getContentPane
    container.setLayout(null)
    val panel: JPanel = new JPanel
    panel.setBorder(new EtchedBorder)
    panel.setBackground(new Color(255, 255, 255))
    panel.setBounds(10, 10, 348, 140)
    panel.setLayout(null)
    container.add(panel)

    var label: JLabel = new JLabel("天量报警工具")
    label.setFont(new Font("Song", Font.BOLD, 16))
    label.setBounds(125, 5, 280, 30)
    panel.add(label)

    label = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;感谢 心灵捕手、华美、<br/> &lt;&lt;美股技术讨论群&gt;&gt;的兄弟姐妹们！</html>")
    label.setFont(new Font("Song", Font.PLAIN, 12))
    label.setBounds(60, 35, 280, 60)
    panel.add(label)

    messageLabel = new JLabel("loading ...")
    messageLabel.setFont(new Font("Song", Font.PLAIN, 10))
    messageLabel.setBounds(11, 75, 340, 30)
    panel.add(messageLabel)
    messageLabel2 = new JLabel("")
    messageLabel2.setFont(new Font("Song", Font.PLAIN, 10))
    messageLabel2.setBounds(11, 85, 340, 30)
    panel.add(messageLabel2)


    sysMessageLabel = new JLabel("")
    sysMessageLabel.setFont(new Font("Song", Font.PLAIN, 10))
    sysMessageLabel.setForeground(Color.RED)
    sysMessageLabel.setBounds(11, 140, 340, 30)
    container.add(sysMessageLabel)

    total = config.stocks.split(",").length * 2
    progressBar.setBounds(5, 170, 360, 15)
    progressBar.setMaximum(total)
    loadProgressBar
    container.add(progressBar)

    setSize(370, 215)
    setLocationRelativeTo(null)
    setAlwaysOnTop(true)
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    setResizable(false);
    setVisible(true)
  }

  override def processWindowEvent(e: WindowEvent): Unit = {
    e.getID match {
      case WindowEvent.WINDOW_CLOSING =>
        val result = JOptionPane.showConfirmDialog(this, "确认要关闭海量检测工具吗？", "关闭", JOptionPane.YES_NO_OPTION)
        if (result == JOptionPane.YES_OPTION) {
          timer1.stop()
          ShakeyApp.stopServer()
          super.processWindowEvent(e)
        }
      case _ =>
        super.processWindowEvent(e)

    }
  }

  def incCountAndMessage(message: String) {
    this.count += 1
    this.message = message;
    this.errorMessage = null;
  }

  def setErrorMessage(message: String) {
    this.errorMessage = message;
  }

  def setSysMessage(message: String) {
    this.sysMessage = message;
  }

  private def loadProgressBar {
    val al: ActionListener = new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        progressBar.setValue(count)
        messageLabel.setText(message)
        messageLabel2.setText(errorMessage)
        sysMessageLabel.setText(sysMessage)
        sysMessageLabel.setToolTipText(sysMessage)

        if (count == total) {
          if (notifier != null)
            notifier.start
          dispose()
          timer1.stop
        }
      }
    }
    timer1 = new Timer(50, al)
    timer1.start
  }
}

