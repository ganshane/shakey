package shakey.internal

import com.ib.controller.{Bar, Types, NewContract, ApiController}
import org.apache.tapestry5.ioc.annotations.PostInjection
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IRealTimeBarHandler
import shakey.services.LoggerSupport
import com.codahale.metrics.Meter
import org.apache.tapestry5.ioc.services.cron.{CronSchedule, PeriodicExecutor}
import org.apache.tapestry5.json.JSONArray
import shakey.ShakeyConstants

/**
 * Created by jcai on 14-9-25.
 */
class RealtimeMktDataFetcher(controller:ApiController,perodicExecutor:PeriodicExecutor) extends LoggerSupport{
  @PostInjection
  def start{
    logger.debug("fetch realtime data...")
    fetchStockRate("yy")
    /*
    startStock("YY")
    startStock("JD")
    startStock("BABA")
    startStock("CMCM")
    startStock("DANG")
    startStock("VIPS")
    startStock("JMEI")
    startStock("WUBA")
    startStock("TOUR")
    */
  }
  def fetchStockRate(stock:String){
    val content = RestClient.get(ShakeyConstants.HISTORY_API_URL_FORMATTER.format(stock))
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
    logger.debug("rate:{}",volCount/size)
  }
  def startStock(stock:String){
    val m_contract= new NewContract();
    m_contract.symbol(stock);
    m_contract.secType(SecType.STK)
    m_contract.currency("USD")
    m_contract.exchange("SMART")
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
