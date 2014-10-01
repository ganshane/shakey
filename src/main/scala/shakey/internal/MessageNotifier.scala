package shakey.internal

import javax.swing._
import java.applet.Applet
import shakey.services.{LoggerSupport, Stock}
import java.awt._
import java.awt.event._
import java.awt.image.BufferedImage
import shakey.app.ShakeyApp
import java.awt.TrayIcon.MessageType
import java.util.concurrent.{TimeUnit, PriorityBlockingQueue}
import shakey.internal.MessageNotifier.StockWithTime
import org.joda.time.DateTime

/**
 * 消息通知
 */
object MessageNotifier {
  val queue = new PriorityBlockingQueue[StockWithTime](15)

  class StockWithTime(val stock: Stock, val time: Long) extends Comparable[StockWithTime] {
    override def compareTo(o: StockWithTime): Int = {
      var result = o.time.compareTo(time)
      if (result == 0) {
        result = o.stock.nowScale.compareTo(stock.nowScale)
      }

      result
    }

    override def equals(obj: scala.Any): Boolean = {
      obj.asInstanceOf[StockWithTime].stock.symbol == stock.symbol
    }

    override def toString: String = {
      new DateTime(time).toString("HH:mm") + " " + stock.symbol + " " + "%.2f".format(stock.nowScale)
    }
  }

}

class MessageNotifier(name: String) extends LoggerSupport {
  private val codebase = getClass.getResource("/ding.wav")
  private val ding = Applet.newAudioClip(codebase);
  private var trayIcon: TrayIcon = _
  //private val image:Image= loadTrayIcon("/ico.png","Shakey").getImage
  //private val image2:Image= loadTrayIcon("/ico2.png","Shakey").getImage
  private val image: Image = iconToImage(new BevelArrowIcon(BevelArrowIcon.UP, false, false))
  private val image2: Image = iconToImage(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false))
  private var timer: Timer = _
  private val tray = SystemTray.getSystemTray
  @volatile
  private var runFlag = false

  initTray

  def play {
    ding.play()
    runFlag = true
  }

  private def showStockInfo {
    //TODO 打开一个panel能够展示提醒的股票信息
    val builder = new StringBuilder
    val queue = MessageNotifier.queue
    0 until queue.size() foreach {
      case i =>
        val stock = queue.poll(1, TimeUnit.MILLISECONDS)
        if (stock != null)
          builder.append(stock).append("\n")
    }
    trayIcon.displayMessage("Shakey", builder.toString(), MessageType.INFO)
    runFlag = false
  }

  def initTray {
    val mouseListener = new MouseAdapter() {
      override def mouseClicked(e: MouseEvent): Unit = {
        //双击图标不再闪动
        if (e.getClickCount == 2) {
          showStockInfo
        }
      }
    };

    val exitListener = new ActionListener() {
      override def actionPerformed(e: ActionEvent): Unit = {
        tray.remove(trayIcon)
        logger.debug("exiting ...")
        ShakeyApp.stopServer()
        System.exit(0);
      }
    };
    val aboutListener = new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        trayIcon.displayMessage("Shakey", "powered by you and me!", MessageType.INFO)
      }
    }
    val showListener = new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        showStockInfo
      }
    }

    val popup = new PopupMenu();

    val showItem = new MenuItem("Show");
    showItem.addActionListener(showListener);
    popup.add(showItem);
    val aboutItem = new MenuItem("About");
    aboutItem.addActionListener(aboutListener);
    popup.add(aboutItem);

    val defaultItem = new MenuItem("Exit");
    defaultItem.addActionListener(exitListener);
    popup.add(defaultItem);

    trayIcon = new TrayIcon(image, "Shakey", popup);
    trayIcon.setImageAutoSize(true);
    trayIcon.addMouseListener(mouseListener);

    tray.add(trayIcon);

    start
  }

  private def start {
    //加载定时器能够闪动图标
    timer = new Timer(125, updateCol)
    timer.start
  }

  private def updateCol: Action = {
    return new AbstractAction("Icon load action") {
      def actionPerformed(e: ActionEvent) {
        val doRun: Runnable = new Runnable {
          def run {
            val img: Image = trayIcon.getImage
            if (runFlag) {
              //闪动模式
              if (img == image) {
                trayIcon.setImage(image2)
              } else {
                trayIcon.setImage(image)
              }
            } else {
              //正常模式
              if (img != image)
                trayIcon.setImage(image)
            }
          }
        }
        SwingUtilities.invokeLater(doRun)
      }
    }
  }

  private def loadTrayIcon(path: String, desc: String): ImageIcon = {
    val imageURL = getClass.getResource(path);

    if (imageURL == null) {
      logger.error("Resource not found: {}", path);
      return null;
    } else {
      return new ImageIcon(imageURL, desc)
    }
  }

  private def iconToImage(icon: Icon): Image = {
    if (icon.isInstanceOf[ImageIcon]) {
      return (icon.asInstanceOf[ImageIcon]).getImage
    }
    else {
      val w: Int = icon.getIconWidth
      val h: Int = icon.getIconHeight
      val ge: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
      val gd: GraphicsDevice = ge.getDefaultScreenDevice
      val gc: GraphicsConfiguration = gd.getDefaultConfiguration
      val image: BufferedImage = gc.createCompatibleImage(w, h)
      val g: Graphics2D = image.createGraphics
      icon.paintIcon(null, g, 0, 0)
      g.dispose
      return image
    }
  }

  object BevelArrowIcon {
    final val UP: Int = 0
    final val DOWN: Int = 1
    private final val DEFAULT_SIZE: Int = 16
  }

  class BevelArrowIcon extends Icon {

    import BevelArrowIcon._

    def this(direction: Int, isRaisedView: Boolean, isPressedView: Boolean) {
      this()
      if (isRaisedView) {
        if (isPressedView) {
          init(UIManager.getColor("controlLtHighlight"), UIManager.getColor("controlDkShadow"), UIManager.getColor("controlShadow"), DEFAULT_SIZE, direction)
        }
        else {
          init(UIManager.getColor("controlHighlight"), UIManager.getColor("controlShadow"), UIManager.getColor("control"), DEFAULT_SIZE, direction)
        }
      }
      else {
        if (isPressedView) {
          init(UIManager.getColor("controlDkShadow"), UIManager.getColor("controlLtHighlight"), UIManager.getColor("controlShadow"), DEFAULT_SIZE, direction)
        }
        else {
          init(UIManager.getColor("controlShadow"), UIManager.getColor("controlHighlight"), UIManager.getColor("control"), DEFAULT_SIZE, direction)
        }
      }
    }

    def this(edge1: Color, edge2: Color, fill: Color, size: Int, direction: Int) {
      this()
      init(edge1, edge2, fill, size, direction)
    }

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
      direction match {
        case BevelArrowIcon.DOWN =>
          drawDownArrow(g, x, y)
        case BevelArrowIcon.UP =>
          drawUpArrow(g, x, y)
      }
    }

    def getIconWidth: Int = {
      return size
    }

    def getIconHeight: Int = {
      return size
    }

    private def init(edge1: Color, edge2: Color, fill: Color, size: Int, direction: Int) {
      this.edge1 = edge1
      this.edge2 = edge2
      this.fill = fill
      this.size = size
      this.direction = direction
    }

    private def drawDownArrow(g: Graphics, xo: Int, yo: Int) {
      g.setColor(edge1)
      g.drawLine(xo, yo, xo + size - 1, yo)
      g.drawLine(xo, yo + 1, xo + size - 3, yo + 1)
      g.setColor(edge2)
      g.drawLine(xo + size - 2, yo + 1, xo + size - 1, yo + 1)
      var x: Int = xo + 1
      var y: Int = yo + 2
      var dx: Int = size - 6
      while (y + 1 < yo + size) {
        g.setColor(edge1)
        g.drawLine(x, y, x + 1, y)
        g.drawLine(x, y + 1, x + 1, y + 1)
        if (0 < dx) {
          g.setColor(fill)
          g.drawLine(x + 2, y, x + 1 + dx, y)
          g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1)
        }
        g.setColor(edge2)
        g.drawLine(x + dx + 2, y, x + dx + 3, y)
        g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1)
        x += 1
        y += 2
        dx -= 2
      }
      g.setColor(edge1)
      g.drawLine(xo + (size / 2), yo + size - 1, xo + (size / 2), yo + size - 1)
    }

    private def drawUpArrow(g: Graphics, xo: Int, yo: Int) {
      g.setColor(edge1)
      var x: Int = xo + (size / 2)
      g.drawLine(x, yo, x, yo)
      x -= 1
      var y: Int = yo + 1
      var dx: Int = 0
      while (y + 3 < yo + size) {
        g.setColor(edge1)
        g.drawLine(x, y, x + 1, y)
        g.drawLine(x, y + 1, x + 1, y + 1)
        if (0 < dx) {
          g.setColor(fill)
          g.drawLine(x + 2, y, x + 1 + dx, y)
          g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1)
        }
        g.setColor(edge2)
        g.drawLine(x + dx + 2, y, x + dx + 3, y)
        g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1)
        x -= 1
        y += 2
        dx += 2
      }
      g.setColor(edge1)
      g.drawLine(xo, yo + size - 3, xo + 1, yo + size - 3)
      g.setColor(edge2)
      g.drawLine(xo + 2, yo + size - 2, xo + size - 1, yo + size - 2)
      g.drawLine(xo, yo + size - 1, xo + size, yo + size - 1)
    }

    private var edge1: Color = null
    private var edge2: Color = null
    private var fill: Color = null
    private var size: Int = 0
    private var direction: Int = 0
  }

}

class MessageNotifierService {
  private var INSTANCE: MessageNotifier = null

  def start {
    val runnable = new Runnable {
      override def run(): Unit = {
        INSTANCE = new MessageNotifier("jcai")
      }
    }
    if (SwingUtilities.isEventDispatchThread) {
      runnable.run()
    } else {
      SwingUtilities.invokeAndWait(runnable)
    }
  }

  def notify(stock: Stock) {
    val stockWithTime = new StockWithTime(stock, System.currentTimeMillis())
    MessageNotifier.queue.remove(stockWithTime)

    val queue = MessageNotifier.queue
    val size = queue.size() - 10
    if (size >= 0) {
      0 until size foreach {
        case i =>
          queue.poll()
      }
    }
    queue.offer(stockWithTime)

    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        if (INSTANCE != null)
          INSTANCE.play
      }
    })
  }
}

