package shakey.server.internal.analyzer

import shakey.server.services.StockAnalyzer
import org.apache.tapestry5.json.{JSONArray, JSONObject}
import java.util
import scala.collection.mutable.ListBuffer

/**
 * 出现天量股票分析.
 * 分析依据，当日的成交量，是之前10天的1.618倍
 *
 * @author jcai
 */
class DayVolumeAnalyzer extends StockAnalyzer {
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
      list += new VolumeStock(symbol, currentVolume * 1.0 / av)
    }
  }

  def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]) {
    val arr = list.sorted.toArray
    model.put("stocks", arr)
  }

  def getTemplatePath: String = {
    return "day-volume.ftl"
  }

  private val list = new ListBuffer[VolumeStock]()

  class VolumeStock(val symbol: String, val rate: Double) extends Comparable[VolumeStock] {
    def compareTo(o: VolumeStock): Int = {
      return o.rate.compareTo(rate)
    }
  }

}
