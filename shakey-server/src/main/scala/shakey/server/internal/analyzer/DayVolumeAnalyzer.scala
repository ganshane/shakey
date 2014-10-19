package shakey.server.internal.analyzer

import java.util

import org.apache.tapestry5.json.{JSONArray, JSONObject}
import shakey.server.internal.algorithm.StockAlgorithm
import shakey.server.services.StockAnalyzer

import scala.collection.mutable.ListBuffer

/**
 * 出现天量股票分析.
 * 分析依据，当日的成交量，是之前10天的1.618倍
 *
 * @author jcai
 */
class DayVolumeAnalyzer extends StockAnalyzer {
  private val list = new ListBuffer[VolumeStock]()

  def processStockDataInOneYear(symbol: String, data: JSONArray) {
    val pos: Int = data.length - 1
    val current: JSONObject = data.getJSONObject(pos)
    val size: Int = 10
    val untilPos: Int = pos - 1 - size

    val av = Range(pos - 1, untilPos, -1).
      map(data.getJSONObject(_).getInt("v")).
      sum / size

    val currentVolume: Int = current.getInt("v")
    if (currentVolume > av * 2) {
      //求20天的价格强度
      val arr = Range(pos - 1, pos - 1 - 20, -1).map(data.getJSONObject(_).getDouble("c")).toArray
      val strongRate = StockAlgorithm.calStrongRate(arr, arr.length)
      if (math.abs(strongRate) > 0.01)
        list += new VolumeStock(symbol, strongRate, currentVolume * 1.0 / av)
    }
  }

  def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]) {
    val arr = list.sorted.toArray
    model.put("stocks", arr)
  }

  def getTemplatePath: String = {
    return "day-volume.ftl"
  }

  class VolumeStock(val symbol: String, val strongRate: Double, val rate: Double) extends Comparable[VolumeStock] {
    def compareTo(o: VolumeStock): Int = {
      return math.abs(o.strongRate).compareTo(math.abs(strongRate))
    }
  }

}
