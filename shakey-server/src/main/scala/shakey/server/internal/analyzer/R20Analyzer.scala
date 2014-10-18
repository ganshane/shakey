package shakey.server.internal.analyzer

import java.util

import org.apache.tapestry5.json.{JSONArray, JSONObject}
import shakey.server.internal.algorithm.StockAlgorithm
import shakey.server.services.StockAnalyzer

import scala.collection.mutable.ListBuffer

/**
 * 分析触碰到R20的股票
 */
class R20Analyzer extends StockAnalyzer {
  private val list = new ListBuffer[R20Stock]()

  def processStockDataInOneYear(symbol: String, data: JSONArray) {
    val pos: Int = data.length - 1
    val r20 = 0.until(20).
      map(x => data.getJSONObject(pos - x).
      getDouble("c")).sum / 20

    val current: JSONObject = data.getJSONObject(pos)
    val o = current.getDouble("o")
    val c = current.getDouble("c")
    val isReach = (Math.abs(o - r20) + Math.abs(r20 - c)) == Math.abs(c - o)
    if (isReach) {
      val rate = StockAlgorithm.calStrongRate(data, 25)
      if (rate > 0.003)
        list += new R20Stock(symbol, rate)
    }
  }

  def getTemplatePath: String = {
    return "/r20.ftl"
  }

  def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]) {
    val arr = list.sorted.toArray
    model.put("stocks", arr)
  }

  class R20Stock(val symbol: String, val strongRate: Double) extends Comparable[R20Stock] {
    def compareTo(o: R20Stock): Int = {
      o.strongRate.compareTo(strongRate)
    }
  }

}
