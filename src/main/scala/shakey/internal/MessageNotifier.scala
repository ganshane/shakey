package shakey.internal

import javax.swing.{SwingUtilities, JFrame}
import java.applet.Applet
import shakey.services.Stock
import org.apache.tapestry5.ioc.annotations.PostInjection

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

class MessageNotifierService {
  private var INSTANCE: MessageNotifier = null

  @PostInjection
  def start {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run(): Unit = {
        INSTANCE = new MessageNotifier("jcai")
        INSTANCE.play
      }
    })
  }

  def notify(stock: Stock) {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        INSTANCE.play
      }
    })
  }
}

