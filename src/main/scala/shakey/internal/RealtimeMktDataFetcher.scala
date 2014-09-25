package shakey.internal

import com.ib.controller.{Bar, Types, NewContract, ApiController}
import org.apache.tapestry5.ioc.annotations.PostInjection
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IRealTimeBarHandler
import shakey.services.LoggerSupport
import com.codahale.metrics.Meter
import org.apache.tapestry5.ioc.services.cron.{CronSchedule, PeriodicExecutor}

/**
 * Created by jcai on 14-9-25.
 */
class RealtimeMktDataFetcher(controller:ApiController,perodicExecutor:PeriodicExecutor) extends LoggerSupport{
  @PostInjection
  def start{
    logger.debug("fetch realtime data...")
    val m_contract= new NewContract();
    m_contract.symbol("YY");
    m_contract.secType(SecType.STK)
    m_contract.currency("USD")
    m_contract.exchange("SMART")
    val meter = new Meter()
    perodicExecutor.addJob(new CronSchedule("0 * * * * ? *"),"job",new Runnable {
      override def run(): Unit = {
        logger.debug("1m: {}",(meter.getOneMinuteRate * 60 * 100).asInstanceOf[Int])
        logger.debug("5m: {}",(meter.getFiveMinuteRate * 5 * 60 * 100).asInstanceOf[Int])
        logger.debug("15m: {}",(meter.getFifteenMinuteRate * 15 * 60 * 100).asInstanceOf[Int])
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
