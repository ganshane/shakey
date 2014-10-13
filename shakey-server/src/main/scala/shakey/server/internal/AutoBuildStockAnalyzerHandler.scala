package shakey.server.internal

import com.lmax.disruptor.EventHandler
import shakey.server.services.StockAnalyzer
import java.io.{FileWriter, Writer}
import shakey.server.internal.StockAnalyzerApp.StockDayEvent
import shakey.services.LoggerSupport

/**
 * 自动加载分析的handler
 */
class AutoBuildStockAnalyzerHandler(analyzer: StockAnalyzer, writer: Writer, countDownLatch: CountDowner)
  extends EventHandler[StockDayEvent]
  with LoggerSupport {
  logger.info("{} started", analyzer.getClass.getSimpleName)

  override def onEvent(event: StockDayEvent, sequence: Long, endOfBatch: Boolean): Unit = {
    if (event.complete) {
      val model = new java.util.HashMap[AnyRef, AnyRef]
      analyzer.addDataToTemplateAfterAnalysis(model)
      TemplateProcessor.processTemplate(analyzer.getTemplatePath, model, writer)
      if (writer.isInstanceOf[FileWriter])
        writer.close()
      countDownLatch.countDown()
      logger.info("{} finished", analyzer.getClass.getSimpleName)
      return
    }
    val data = event.dayData
    if (data.length() <= 20)
      return
    analyzer.processStockDataInOneYear(event.symbol, data)
  }
}
