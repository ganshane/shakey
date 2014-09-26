package shakey.internal

import javax.swing.{SwingUtilities, JFrame}
import java.applet.Applet
import shakey.services.Stock

/**
 * 消息通知
 */
class MessageNotifier(name: String) extends JFrame(name) {
  setSize(200, 200);
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  setVisible(true);
  val codebase = getClass.getResource("/ding.wav")
  val ding = Applet.newAudioClip(codebase);
  ding.play()
  this.dispose()
}

object MessageNotifier {
  def notify(stock: Stock) {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        new MessageNotifier(stock.symbol)
      }
    })
  }

  def main(args: Array[String]) {
    val stock = new Stock("YY")
    notify(stock)
  }
}
