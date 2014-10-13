package shakey.server.internal

import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor._
import java.util.concurrent.{ExecutorService, CountDownLatch, ThreadFactory, Executors}
import java.util.concurrent.atomic.AtomicInteger
import org.apache.tapestry5.json.JSONArray
import shakey.services.{ShakeyException, LoggerSupport}
import shakey.internal.StockSymbolFetcher
import shakey.server.internal.StockAnalyzerApp.StockDayEvent
import scala.io.Source

/**
 * 针对股票的分析程序
 */
object StockAnalyzerApp {

  class StockDayEvent {
    var symbol: String = null
    var dayData: JSONArray = null
    var complete = false
  }


  def main(args: Array[String]) {
    var dirOpt: Option[String] = None
    if (args.length > 0) {
      dirOpt = Some(args(0))
    }
    var api = "sina"
    if (args.length > 1) {
      api = args(1)
    }
    var countOpt: Option[Int] = None
    if (args.length > 2) {
      countOpt = Some(args(2).toInt)
    }
    val analyzer = new StockAnalyzerApp(dirOpt, api, countOpt)
    analyzer.start
    analyzer.shutdown
  }
}

trait CountDowner {
  def countDown();
}

class StockAnalyzerApp(dirOpt: Option[String], api: String, countOpt: Option[Int]) extends LoggerSupport with CountDowner {
  private var countDownLatch: CountDownLatch = _
  private val buffer = 1 << 8
  private val fetchWorkerNum = 2
  private var disruptor: Disruptor[StockDayEvent] = null
  private val EVENT_FACTORY = new EventFactory[StockDayEvent] {
    def newInstance() = new StockDayEvent()
  }
  private var executors: ExecutorService = _

  override def countDown(): Unit = {
    countDownLatch.countDown()
  }

  def start {
    val handlers = StockAnalyzerRegistry.buildHandler(dirOpt, this)
    countDownLatch = new CountDownLatch(handlers.size)
    executors = Executors.newFixedThreadPool(fetchWorkerNum + handlers.size, new ThreadFactory {
      private val seq = new AtomicInteger(0)

      def newThread(p1: Runnable) = {
        val t = new Thread(p1)
        t.setName("processor-%s".format(seq.incrementAndGet()))
        t
      }
    })

    startDisruptor(handlers)

    var i = 0
    var lines = Source.fromInputStream(getClass.getResourceAsStream("/stocks")).getLines();
    if (countOpt.isDefined)
      lines = lines.take(countOpt.get)
    lines.take(100).foreach {
      case symbol =>
        //StockSymbolFetcher.fetchChinaStock.foreach{case symbol=>
        disruptor.publishEvent(new EventTranslator[StockDayEvent] {
          override def translateTo(event: StockDayEvent, sequence: Long): Unit = {
            event.symbol = symbol
            event.dayData = null
            event.complete = false
          }
        })
        i += 1
        if ((i % 100) == 0)
          logger.info("process {} stock is {}", i, symbol)
    }
    logger.info("finish process {} stocks ", i)

    disruptor.publishEvent(new EventTranslator[StockDayEvent] {
      override def translateTo(event: StockDayEvent, sequence: Long): Unit = {
        event.complete = true
      }
    })
    countDownLatch.await()
  }

  protected def startDisruptor(handlers: List[EventHandler[StockDayEvent]]) {
    disruptor = new Disruptor[StockDayEvent](EVENT_FACTORY, buffer, executors)
    disruptor.handleExceptionsWith(new LogExceptionHandler)

    val workHandlers = 0 until fetchWorkerNum map {
      case i =>
        new FetchStockDayDataWorker
    }
    disruptor.handleEventsWithWorkerPool(workHandlers: _*).`then`(handlers: _ *)
    disruptor.start()
  }

  protected def shutdown {
    logger.info("shutdown disruptor...")
    disruptor.shutdown()
    executors.shutdown()
    logger.info("disruptor closed")
  }

  //抓取每天的数据
  class FetchStockDayDataWorker extends WorkHandler[StockDayEvent] {
    override def onEvent(event: StockDayEvent): Unit = {
      if (event.complete)
        return
      api match {
        case "sina" =>
          event.dayData = StockSymbolFetcher.fetchStockDayVolume(event.symbol)
        case "yahoo" =>
          event.dayData = StockSymbolFetcher.fetchStockDayVolumeByYahoo(event.symbol)
      }
    }
  }


  class LogExceptionHandler extends ExceptionHandler with LoggerSupport {

    def handleEventException(ex: Throwable, sequence: Long, event: Any) {
      ex match {
        case e: ShakeyException =>
          logger.error(e.toString)
        case e: InterruptedException =>
        //do nothing
        case _ =>
          logger.error(ex.getMessage, ex)
      }
    }

    def handleOnStartException(ex: Throwable) {
      logger.error(ex.getMessage, ex)
    }

    def handleOnShutdownException(ex: Throwable) {
      logger.error(ex.getMessage, ex)
    }
  }

}

