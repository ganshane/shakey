package shakey.server.internal

import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor._
import java.util.concurrent.{CountDownLatch, ThreadFactory, Executors}
import java.util.concurrent.atomic.AtomicInteger
import org.apache.tapestry5.json.JSONArray
import shakey.services.{ShakeyException, LoggerSupport}
import scala.collection.mutable
import shakey.internal.{StockAlgorithm, StockSymbolFetcher}
import shakey.server.internal.Stockanalyzer.{StrongStock, StockDayEvent}
import java.io.{Writer, File, FileWriter, OutputStreamWriter}

/**
 * 针对股票的分析程序
 */
object Stockanalyzer {

  class StrongStock(val symbol: String, val rate1: Double, val rate2: Double, val rate3: Double) extends Comparable[StrongStock] {
    override def compareTo(o: StrongStock): Int = {
      rate1.compareTo(o.rate1)
    }

    override def toString: String = {
      "%s,%s".format(symbol, rate1)
    }

    def isRed() = {
      rate1 > 0 && rate2 < 0 && rate3 > 0
    }

    def isBlue() = {
      rate1 > 0 && rate2 < 0
    }

    def isGray() = {
      rate1 > 0 && rate3 < 0
    }

    def getColor(): String = {
      if (isRed())
        "red"
      else if (isBlue())
        "blue"
      else if (isGray())
        "gray"
      else
        "white"
    }
  }

  class StockDayEvent {
    var symbol: String = null
    var dayData: JSONArray = null
    var complete = false
  }

  val countDownLatch = new CountDownLatch(2)

  def main(args: Array[String]) {
    var strongWriter = new OutputStreamWriter(System.out)
    if (args.length > 0) {
      strongWriter = new FileWriter(new File(args(0)))
    }
    var rbWriter = new OutputStreamWriter(System.out)
    if (args.length > 1) {
      rbWriter = new FileWriter(new File(args(1)))
    }
    val analyzer = new Stockanalyzer(strongWriter, rbWriter)
    analyzer.start
    countDownLatch.await()
    strongWriter.close()
    rbWriter.close()
    analyzer.shutdownDisrutpor
  }
}

class Stockanalyzer(strongWriter: Writer, rbWriter: Writer) extends LoggerSupport {
  private val buffer = 1 << 8
  private val fetchWorkerNum = 5
  private var disruptor: Disruptor[StockDayEvent] = null
  private val EVENT_FACTORY = new EventFactory[StockDayEvent] {
    def newInstance() = new StockDayEvent()
  }
  val executors = Executors.newFixedThreadPool(fetchWorkerNum + 2, new ThreadFactory {
    private val seq = new AtomicInteger(0)

    def newThread(p1: Runnable) = {
      val t = new Thread(p1)
      t.setName("processor-%s".format(seq.incrementAndGet()))
      t
    }
  })

  def start {
    startDisruptor
    StockSymbolFetcher.fetchAllStock {
      symbol =>
      //StockSymbolFetcher.fetchChinaStock.foreach{case symbol=>
        disruptor.publishEvent(new EventTranslator[StockDayEvent] {
          override def translateTo(event: StockDayEvent, sequence: Long): Unit = {
            event.symbol = symbol
            event.dayData = null
            event.complete = false
          }
        })
    }

    disruptor.publishEvent(new EventTranslator[StockDayEvent] {
      override def translateTo(event: StockDayEvent, sequence: Long): Unit = {
        event.complete = true
      }
    })
  }

  protected def startDisruptor {
    disruptor = new Disruptor[StockDayEvent](EVENT_FACTORY, buffer, executors)
    disruptor.handleExceptionsWith(new LogExceptionHandler)

    val workHandlers = 0 until fetchWorkerNum map {
      case i =>
        new FetchStockDayDataWorker
    }
    disruptor.handleEventsWithWorkerPool(workHandlers: _*).`then`(new TradeAnalysisHandler, new ConsecutiveDownAnalysisHandler(rbWriter))
    disruptor.start()
  }

  protected def shutdownDisrutpor {
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
      event.dayData = StockSymbolFetcher.fetchStockDayVolume(event.symbol)
    }
  }

  class TradeAnalysisHandler extends EventHandler[StockDayEvent] {
    private val queue = new mutable.PriorityQueue[StrongStock]()

    def output() {
      val model = new java.util.HashMap[Any, Any]
      model.put("stocks", queue.dequeueAll.toArray)
      TemplateProcessor.processTemplate("/strong-stock.ftl", model, strongWriter)
    }

    private def cal(jsonArray: JSONArray, day_len: Int): Double = {
      val len = jsonArray.length()
      var size = day_len
      var begin = len - size
      if (begin < 0)
        begin = 0
      size = len - begin

      val xx = 0 until size toArray;

      val yy = new Array[Double](size)
      begin until jsonArray.length() foreach {
        case i =>
          val obj = jsonArray.getJSONObject(i)
          //{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"}
          val h = obj.getDouble("h")
          val l = obj.getDouble("l")
          yy(i - begin) = math.log(l + (h - l) / 2)
      }

      StockAlgorithm.LineSimulate(xx, yy)
    }

    override def onEvent(event: StockDayEvent, sequence: Long, endOfBatch: Boolean): Unit = {
      if (event.complete) {
        output()
        Stockanalyzer.countDownLatch.countDown()
        return
      }
      val dayData = event.dayData
      if (dayData.length() == 0) //没有日数据
        return
      val obj = dayData.getJSONObject(dayData.length() - 1)
      if (obj.getInt("v") > 500000 && obj.getDouble("c") > 5.0) {
        val r = (cal(dayData, 25), cal(dayData, 8), cal(dayData, 3))
        //logger.debug("seq:" + sequence + " symbol:{} rate:{}", event.symbol, r)
        queue += new StrongStock(event.symbol, r._1, r._2, r._3)
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
