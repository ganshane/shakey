package shakey.internal

import com.ib.controller.{Bar, Types, NewContract, ApiController}
import org.apache.tapestry5.ioc.annotations.PostInjection
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IRealTimeBarHandler
import shakey.services.{Stock, StockDatabase, LoggerSupport}
import org.apache.tapestry5.ioc.services.cron.{CronSchedule, PeriodicExecutor}
import org.apache.tapestry5.json.JSONArray
import shakey.ShakeyConstants
import shakey.config.ShakeyConfig
import java.util.concurrent.Executors

/**
 * 实时股票信息的抓取
 * 1> 先从sina抓取均量 TODO 考虑放入另外线程处理
 * 2> 启动ib的实时数据抓取 TODO 考虑放入另外的线程专门处理数据
 * 3> 启动报表 TODO 考虑启动线程进行比较控制
 */
class RealtimeMktDataFetcher(config: ShakeyConfig,
                             controller: ApiController,
                             perodicExecutor:PeriodicExecutor,
                             database: StockDatabase,
                             notifier: MessageNotifierService) extends LoggerSupport {
  private val TRADE_SECONDS_IN_ONE_DAY:Double = 6.5 * 60 * 60
  private val executor = Executors.newFixedThreadPool(2)

  @PostInjection
  def start{
    logger.debug("fetch realtime data...")
    //初始化速率
    executor.submit(new Runnable {
      override def run(): Unit = {
        database updateStockList fetchStockRate
        database updateStockList startMonitor
      }
    })
    startReporter
  }
  def startReporter{
    perodicExecutor.addJob(new CronSchedule("0 * * * * ? *"),"job",new Runnable {
      override def run(): Unit = {
        database updateStockList {stock=>
        //logger.debug("{} 1m: {}", stock.symbol, (stock.meter.getOneMinuteRate * 60).asInstanceOf[Int])
        //logger.debug("{} 5m: {}", stock.symbol, (stock.meter.getFiveMinuteRate * 5 * 60).asInstanceOf[Int])
        //logger.debug("{} 15m: {}", stock.symbol, (stock.meter.getFifteenMinuteRate * 15 * 60).asInstanceOf[Int])
        //TODO 大于多少倍算天量？,算法支撑
          if (stock.rateOneSec > 0 && stock.rateOneSec * config.rateOverflow < stock.meter.getFiveMinuteRate) {
            logger.error("=====================> {} rate:" + stock.rateOneSec * 60 * 5 + " now:" + stock.meter.getFiveMinuteRate * 5 * 60, stock.symbol)
            notifier.notify(stock)
          }
        }
      }
    })
  }
  def fetchStockRate(stock:Stock){
    val content = RestClient.get(ShakeyConstants.HISTORY_API_URL_FORMATTER.format(stock.symbol))
    val jsonArray = new JSONArray(content)
    val len = jsonArray.length()
    var size = ShakeyConstants.HISTORY_SIZE
    var begin = len - size
    if(begin <0)
      begin = 0
    size = len - begin
    var volCount = 0
    begin until jsonArray.length() foreach{case i=>
      val obj = jsonArray.getJSONObject(i)
      volCount += obj.getInt("v")
    }
    //TODO 速率的计算是否合理，算法支撑？
    val rate:Double = volCount/1.0/size/TRADE_SECONDS_IN_ONE_DAY
    logger.debug("symbol:{} rate:{}",stock.symbol,rate)
    stock.rateOneSec = rate;
  }

  private var countMonitor = 0

  def startMonitor(stock:Stock){
    Thread.sleep(11 * 1000)
    countMonitor += 1
    logger.debug("monitor:{} symbol:{}", countMonitor, stock.symbol)
    val contract= new NewContract();
    contract.symbol(stock.symbol);
    contract.secType(SecType.STK)
    contract.currency("USD")
    contract.exchange("SMART")
    controller.reqRealTimeBars(contract,Types.WhatToShow.TRADES,false,new ShakeyRealTimeBarHandler(stock))
  }
  class ShakeyRealTimeBarHandler(stock:Stock) extends IRealTimeBarHandler{
    override def realtimeBar(bar: Bar): Unit = {
      //logger.debug("bar time:{} vol:{}",bar.formattedTime(),bar.volume())
      stock.meter.mark(bar.volume() * 100)//IB API每次返回的单位是100
    }
  }
}
