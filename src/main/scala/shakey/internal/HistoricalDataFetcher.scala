package shakey.internal

import com.ib.controller.{Bar, NewContract, ApiController, Types}
import shakey.services.{StockDatabase, LoggerSupport, Stock}
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IHistoricalDataHandler
import java.util.{Comparator, PriorityQueue}
import org.joda.time.DateTime

/**
 * 历史数据的抓取
 */
class HistoricalDataFetcher(controller: ApiController, localStore: LocalSimpleStore, database: StockDatabase) {
  private val last_fetch_historic_data = "last_fetch_historic_data"

  def startFetchBiggerVolume() {
    val timeOpt = localStore.get[Long](last_fetch_historic_data)
    if (timeOpt.isDefined) {
      val nowDay = DateTime.now().getDayOfYear
      val lastDay = new DateTime(timeOpt.get).getDayOfYear
      if (lastDay + 2 >= nowDay) {
        //从数据库获取
        database updateStockList {
          case stock =>
            val rateOpt = localStore.get[Double](stock.symbol)
            rateOpt match {
              case Some(rate) =>
                stock.rateOneSec = rate
              case None =>
                fetchHistoricalData(stock)
            }
        }
        return
      }
    }
    database updateStockList fetchHistoricalData
    localStore.put(last_fetch_historic_data, DateTime.now.getMillis)
  }

  def fetchHistoricalData(stock: Stock) {
    Thread.sleep(11 * 1000)
    val contract = new NewContract();
    contract.symbol(stock.symbol);
    contract.secType(SecType.STK)
    contract.currency("USD")
    contract.exchange("SMART")
    val endDateTime = DateTime.now().minusDays(1).toString("YYYYMMdd HH:mm:ss")

    controller.reqHistoricalData(contract, endDateTime, 1,
      Types.DurationUnit.WEEK, Types.BarSize._5_mins,
      Types.WhatToShow.TRADES, true, new ShakeyHistoricalDataHandler(stock))

  }

  class ShakeyHistoricalDataHandler(stock: Stock) extends IHistoricalDataHandler with LoggerSupport {
    private val rate = 0.2
    private val size = (6.5 * 12 * 5 * rate).asInstanceOf[Int]
    private val queue = new PriorityQueue[Bar](size, new Comparator[Bar] {
      override def compare(o1: Bar, o2: Bar): Int = {
        o2.volume() compare (o1.volume())
      }
    })
    private var allSize = 0
    logger.info("fetch historical data for {}", stock.symbol)

    override def historicalData(bar: Bar, hasGaps: Boolean): Unit = {
      allSize += 1
      queue.add(bar)
    }

    override def historicalDataEnd(): Unit = {
      var size = (allSize * rate).asInstanceOf[Int]

      var bar: Bar = null

      while (size > 0) {
        size -= 1
        bar = queue.poll()
      }
      if (bar != null) {
        stock.rateOneSec = bar.volume() / (5.0 * 60)
        localStore.put(stock.symbol, stock.rateOneSec)
      }
      logger.info("finish fetch historical data,symbol:" + stock.symbol + " vol:{} rate:{}", bar.volume(), stock.rateOneSec)
    }
  }

}
