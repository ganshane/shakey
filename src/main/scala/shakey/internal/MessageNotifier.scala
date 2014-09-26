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
  setVisible(false);
  val codebase = getClass.getResource("/ding.wav")
  val ding = Applet.newAudioClip(codebase);

  def play {
    ding.play()
  }
}

object MessageNotifier {
  private var INSTANCE: MessageNotifier = null

  def notify(stock: Stock) {
    if (INSTANCE == null) {
      SwingUtilities.invokeLater(new Runnable {
        override def run(): Unit = {
          INSTANCE = new MessageNotifier(stock.symbol)
          INSTANCE.play
        }
      })
    } else {
      SwingUtilities.invokeLater(new Runnable {
        override def run(): Unit = {
          INSTANCE.play
        }
      })
    }
  }

  def main(args: Array[String]) {
    val stock = new Stock("YY")
    notify(stock)
  }
}
