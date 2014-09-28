package shakey.internal

import com.ib.controller.{Bar, Types, NewContract, ApiController}
import com.ib.controller.Types.SecType
import com.ib.controller.ApiController.IRealTimeBarHandler
import shakey.services.{Stock, StockDatabase, LoggerSupport}

/**
 * 实时股票信息的抓取
 * 1> 先从sina抓取均量 TODO 考虑放入另外线程处理
 * 2> 启动ib的实时数据抓取 TODO 考虑放入另外的线程专门处理数据
 * 3> 启动报表 TODO 考虑启动线程进行比较控制
 */
class RealtimeMktDataFetcher(controller: ApiController,
                             database: StockDatabase) extends LoggerSupport {

  private var countMonitor = 0

  def startMonitor() {
    database updateStockList monitor
  }

  private def monitor(stock: Stock) {
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

  private class ShakeyRealTimeBarHandler(stock: Stock) extends IRealTimeBarHandler {
    override def realtimeBar(bar: Bar): Unit = {
      //logger.debug("bar time:{} vol:{}",bar.formattedTime(),bar.volume())
      stock.meter.mark(bar.volume()) //IB API每次返回的单位是100
    }
  }
}
