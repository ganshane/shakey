package shakey.internal

import javax.swing._
import java.awt.{Font, Color, Container}
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.border.EtchedBorder
import shakey.config.ShakeyConfig

/**
 * Created by jcai on 14-9-29.
 */
class ShakeySplashScreen(config: ShakeyConfig, notifier: MessageNotifierService) extends JWindow {
  private val progressBar: JProgressBar = new JProgressBar
  private var timer1: Timer = null
  private var messageLabel: JLabel = null
  private var count: Int = 0
  private var total: Int = 0
  private var message: String = "loading ....";
  {
    val container: Container = getContentPane
    container.setLayout(null)
    val panel: JPanel = new JPanel
    panel.setBorder(new EtchedBorder)
    panel.setBackground(new Color(255, 255, 255))
    panel.setBounds(10, 10, 348, 150)
    panel.setLayout(null)
    container.add(panel)

    var label: JLabel = new JLabel("天量报警工具")
    label.setFont(new Font("Song", Font.BOLD, 16))
    label.setBounds(125, 25, 280, 30)
    panel.add(label)

    label = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;感谢 心灵捕手、华美、<br/> &lt;&lt;美股技术讨论群&gt;&gt;的兄弟姐妹们！</html>")
    label.setFont(new Font("Song", Font.PLAIN, 12))
    label.setBounds(60, 55, 280, 60)
    panel.add(label)

    messageLabel = new JLabel("loading ...")
    messageLabel.setFont(new Font("Song", Font.PLAIN, 10))
    messageLabel.setBounds(11, 105, 340, 30)
    panel.add(messageLabel)

    total = config.stocks.split(",").length * 2
    progressBar.setBounds(5, 180, 360, 15)
    progressBar.setMaximum(total)
    loadProgressBar
    container.add(progressBar)
    setSize(370, 215)
    setLocationRelativeTo(null)
    setVisible(true)
    this.toFront()
  }

  def incCountAndMessage(message: String) {
    this.count += 1
    this.message = message;
  }

  private def loadProgressBar {
    val al: ActionListener = new ActionListener {
      def actionPerformed(evt: ActionEvent) {
        progressBar.setValue(count)
        messageLabel.setText(message)
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

