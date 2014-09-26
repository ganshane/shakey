package shakey.internal

import com.ib.controller.{Bar, Types, NewContract, ApiController}
import org.apache.tapestry5.ioc.annotations.PostInjection
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IRealTimeBarHandler
import shakey.services.LoggerSupport
import com.codahale.metrics.Meter
import org.apache.tapestry5.ioc.services.cron.{CronSchedule, PeriodicExecutor}
import org.joda.time.DateTime

/**
 * Created by jcai on 14-9-25.
 */
class RealtimeMktDataFetcher(controller:ApiController,perodicExecutor:PeriodicExecutor) extends LoggerSupport{
  @PostInjection
  def start{
    logger.debug("fetch realtime data...")
    startStock("YY")
    startStock("JD")
    startStock("BABA")
    startStock("CMCM")
    startStock("DANG")
    startStock("VIPS")
    startStock("JMEI")
    startStock("WUBA")
    startStock("TOUR")
  }
  def startStock(stock:String){
    val m_contract= new NewContract();
    m_contract.symbol(stock);
    m_contract.secType(SecType.STK)
    m_contract.currency("USD")
    m_contract.exchange("SMART")

    //查询5‘分钟的速率
    /*
    //http://stock.finance.sina.com.cn/usstock/api/json.php/US_MinKService.getDailyK?symbol=baba&___qn=3
    val dateTime = new DateTime().minusDays(1).formatted("YYYYMMDD HH:MM:SS")
    controller.reqHistoricalData(m_contract,dateTime,1,Types.DurationUnit.MONTH,Types.BarSize._1_day,Types.WhatToShow.TRADES,false,new ApiController.IHistoricalDataHandler(){
      private var count = 0L;
      private var dayCount = 0;
      override def historicalData(bar: Bar, hasGaps: Boolean): Unit = {
        count += bar.volume()
        dayCount += 1
      }
      override def historicalDataEnd(): Unit = {
        logger.debug("stock {} average:{}",stock,count/dayCount)
      }
    })
    */

    val meter = new Meter()
    perodicExecutor.addJob(new CronSchedule("0 * * * * ? *"),"job",new Runnable {
      override def run(): Unit = {
        logger.debug("{} 1m: {}",stock,(meter.getOneMinuteRate * 60 * 100).asInstanceOf[Int])
        logger.debug("{} 5m: {}",stock,(meter.getFiveMinuteRate * 5 * 60 * 100).asInstanceOf[Int])
        logger.debug("{} 15m: {}",stock,(meter.getFifteenMinuteRate * 15 * 60 * 100).asInstanceOf[Int])
      }
    })
    controller.reqRealTimeBars(m_contract,Types.WhatToShow.TRADES,false,new IRealTimeBarHandler {
      override def realtimeBar(bar: Bar): Unit = {
        //logger.debug("bar time:{} vol:{}",bar.formattedTime(),bar.volume())
        meter.mark(bar.volume())
      }
    })
  }
}
