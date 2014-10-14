package shakey.server.internal.algorithm

import org.apache.tapestry5.json.{JSONArray, JSONObject}
import shakey.services.LoggerSupport


/**
 * 股票相关算法
 *
 * @author jcai
 */
object StockAlgorithm
  extends LoggerSupport
  with SMA
  with EMA
  with MACD
  with Trend {
  /**
   * 计算day_len天数内股价的强度系数
   *
   * @param jsonArray 一串的股票数据, 数据格式如下:
   *                  {d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"}
   * @param day_len   计算的天数
   * @return 股价的强度系数
   */
  def calStrongRate(jsonArray: JSONArray, day_len: Int): Double = {
    val len: Int = jsonArray.length
    var size: Int = day_len
    var begin: Int = len - size
    if (begin < 0) begin = 0
    size = len - begin
    val xx = 0.until(size).toArray

    val yy = Range(0, size).map {
      case i =>
        val obj: JSONObject = jsonArray.getJSONObject(i + begin)
        val h: Double = obj.getDouble("h")
        val l: Double = obj.getDouble("l")
        Math.log(l + (h - l) / 2)
    }.toArray
    
    trend(xx, yy)
  }


  def average(data: Array[Double], from: Int, until: Int): Double = {
    val subArray = data.slice(from, until)
    subArray.sum / subArray.length
  }
}

