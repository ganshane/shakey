package shakey.server.internal.algorithm

import org.apache.tapestry5.json.{JSONArray, JSONObject}


/**
 * 股票相关算法
 *
 * @author jcai
 */
object StockAlgorithm {
  /**
   * 利用直线拟合求出一段时间内股价的趋势
   *
   * @param xx 时间序列
   * @param yy 股价序列,长度要和xx保持一致
   * @return 斜率, 斜率为负值, 则表明股价可能下挫，可以卖空，为正值，则股价上升，可以买入
   */
  def lineSimulate(xx: Array[Int], yy: Array[Double]): Double = {
    var yysum: Double = 0
    var xxsum: Double = 0
    var xxyy: Double = 0
    var xxxx: Double = 0

    0.until(xx.length).foreach {
      case i =>
        xxsum += xx(i)
        yysum += yy(i)
        xxyy += xx(i) * yy(i)
        xxxx += xx(i) * xx(i)
    }
    val D: Double = (xxsum * xxsum - xx.length * xxxx)
    val a: Double = (yysum * xxsum - xx.length * xxyy) / D
    //val b = (xxsum * xxyy - yysum * xxxx) / D
    return a
  }

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
    return StockAlgorithm.lineSimulate(xx, yy)
  }

  def average(data: Array[Double], from: Int, until: Int): Double = {
    val subArray = data.slice(from, until)
    subArray.sum / subArray.length
  }
}

