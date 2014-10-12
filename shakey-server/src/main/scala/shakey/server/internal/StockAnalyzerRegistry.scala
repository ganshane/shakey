package shakey.server.internal

import shakey.server.services.StockAnalyzer
import com.lmax.disruptor.EventHandler
import org.joda.time.DateTime
import java.io.{File, FileWriter, OutputStreamWriter}
import shakey.server.internal.StockAnalyzerApp.StockDayEvent
import shakey.server.internal.analyzer.{DayVolumeAnalyzer, ConsecutiveDownAnalyzer, StrongStockAnalyzer}

/**
 * Created by jcai on 14-10-12.
 */
object StockAnalyzerRegistry {
  final val analyzers = List[StockAnalyzer](
    new ConsecutiveDownAnalyzer,
    new StrongStockAnalyzer,
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
