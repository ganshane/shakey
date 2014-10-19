package shakey.server.internal

import java.io.{File, FileWriter, OutputStreamWriter}

import com.lmax.disruptor.EventHandler
import org.joda.time.DateTime
import shakey.server.internal.StockAnalyzerApp.StockDayEvent
import shakey.server.internal.analyzer._
import shakey.server.services.StockAnalyzer

/**
 * 股票分析器的注册
 */
object StockAnalyzerRegistry {
  final val analyzers = List[StockAnalyzer](
    new ConsecutiveDownAnalyzer,
    new StrongStockAnalyzer,
    new R20Analyzer,
    new SupportResistanceAnalyzer,
    new PullbackAnalyzer,
    new DayVolumeAnalyzer
  )

  def buildHandler(postDir: Option[String], countDown: CountDowner): List[EventHandler[StockDayEvent]] = {
    postDir match {
      case Some(dir) => //输出到目录
        val filePrefix = "/" + DateTime.now().toString("YYYY-MM-dd") + "-"
        analyzers map {
          case analyzer =>
            val index1 = analyzer.getTemplatePath.lastIndexOf("/")
            val index2 = analyzer.getTemplatePath.lastIndexOf(".")
            val fileName = dir + filePrefix + analyzer.getTemplatePath.substring(index1 + 1, index2) + ".md"
            val writer = new FileWriter(new File(fileName))
            new AutoBuildStockAnalyzerHandler(analyzer, writer, countDown)
        }
      case None =>
        analyzers map {
          case analyzer =>
            val writer = new OutputStreamWriter(System.out)
            new AutoBuildStockAnalyzerHandler(analyzer, writer, countDown)
        }
    }
  }
}
