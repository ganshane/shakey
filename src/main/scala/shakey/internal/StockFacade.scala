package shakey.internal

import java.util.concurrent.{TimeUnit, Executors}
import org.apache.tapestry5.ioc.annotations.PostInjection
import org.apache.tapestry5.ioc.services.cron.{PeriodicExecutor, CronSchedule}
import shakey.services.{LoggerSupport, StockDatabase}
import org.apache.tapestry5.ioc.services.RegistryShutdownHub
import com.ib.controller.ApiController

/**
 * stock facade
 */
class StockFacade(periodicExecutor: PeriodicExecutor,
                  historicalDataFetcher: HistoricalDataFetcher,
                  realtimeMktDataFetcher: RealtimeMktDataFetcher,
                  notifier: MessageNotifierService,
                  database: StockDatabase) extends LoggerSupport {
  private val executor = Executors.newFixedThreadPool(2)

  @PostInjection
  def start(shutdownHub: RegistryShutdownHub, apiController: ApiController) {
    executor.submit(new Runnable {
      override def run(): Unit = {
        //启动历史数据的速率查询
        historicalDataFetcher.startFetchBiggerVolume()
        //实时数据的监控
        realtimeMktDataFetcher.startMonitor()
      }
    })
    executor.submit(new Runnable {
      override def run(): Unit = {
        startReporter()
      }
    })

    shutdownHub.addRegistryShutdownListener(new Runnable {
      override def run(): Unit = {
        logger.info("closing api controller...")
        apiController.disconnect()
        logger.info("closing stock facade background executor service....")
        executor.shutdown
        try {
          if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            executor.shutdownNow
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
              logger.warn("executor {} not terminated", "stock facade")
            }
          }
        }
        catch {
          case ie: InterruptedException => {
            executor.shutdownNow
            Thread.currentThread.interrupt
          }
        }
      }
    })
  }

  def startReporter() {
    periodicExecutor.addJob(new CronSchedule("0 * * * * ? *"), "job", new Runnable {
      override def run(): Unit = {
        database updateStockList {
          stock =>
          //logger.debug("{} 1m: {}", stock.symbol, (stock.meter.getOneMinuteRate * 60).asInstanceOf[Int])
          //logger.debug("{} 5m: {}", stock.symbol, (stock.meter.getFiveMinuteRate * 5 * 60).asInstanceOf[Int])
          //logger.debug("{} 15m: {}", stock.symbol, (stock.meter.getFifteenMinuteRate * 15 * 60).asInstanceOf[Int])
          //TODO 大于多少倍算天量？,算法支撑
            val fiveRate = stock.meter.getFiveMinuteRate
            notifier.notify(stock)
            if (stock.rateOneSec > 0 && stock.rateOneSec < fiveRate) {
              logger.error("=====================> {} rate:" + stock.rateOneSec * 60 * 5 + " now:" + stock.meter.getFiveMinuteRate * 5 * 60, stock.symbol)
              stock.nowScale = fiveRate / stock.rateOneSec
              notifier.notify(stock)
            }
        }
      }
    })
  }
}
