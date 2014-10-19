package shakey.server.internal.analyzer

import java.util

import org.apache.tapestry5.json.JSONArray
import shakey.server.internal.algorithm.StockAlgorithm
import shakey.server.services.StockAnalyzer

import scala.collection.mutable.ListBuffer

/**
 * 针对回抽股票进行分析
 */
class PullbackAnalyzer extends StockAnalyzer {
  private val list = new ListBuffer[PullbackStock]()

  /**
   * 处理股票一年的日数据
   *
   * @param symbol 股票代码
   * @param data   股票一年的日数据,数据格式为：[{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"},{d:"2012-11-23",o:"11.50",h:"11.76",l:"11.20",c:"11.32",v:"236917"}]
   */
  override def processStockDataInOneYear(symbol: String, data: JSONArray): Unit = {
    var size = 80
    val len = data.length()
    if (len < size)
      size = len
    val begin = len - size
    //计算中间值的股价列表
    val priceStream = Range(begin, len).map { case i =>
      val stock = data.getJSONObject(i)
      val midPoint = (BigDecimal(stock.getDouble("l")) + BigDecimal(stock.getDouble("h"))) / 2
      //(BigDecimal(stock.getDouble("l")), BigDecimal(stock.getDouble("h")), stock.getString("d"))
      (midPoint, midPoint, stock.getString("d"))
    }.toStream
    //先查找支撑位
    var sma = StockAlgorithm.EMA(priceStream.map(_._1), 5)
    sma = StockAlgorithm.EMA(sma, 5)

    val (upResistanceIndex, downSupportIndex) = findSupportAndResistance(sma)
    //策略1 下降部分不能太长时间
    if ((upResistanceIndex) * 2.618 >= downSupportIndex) {
      return
    }

    var downSupport = 0.0
    if (downSupportIndex > 0 && downSupportIndex < (size - 1)) {
      downSupport = priceStream.slice(size - downSupportIndex - 3, size - downSupportIndex + 1).map(_._1).min.doubleValue()
    }

    //寻找阻力位
    var upResistance = 0.0
    if (upResistanceIndex > 0 && upResistanceIndex < (size - 3)) {
      upResistance = priceStream.slice(size - upResistanceIndex - 3, size - upResistanceIndex + 1).map(_._2).max.doubleValue()
    }

    val currentObj = data.getJSONObject(len - 1)
    val current = (currentObj.getDouble("l"), currentObj.getDouble("h"), currentObj.getDouble("c"))

    //策略2
    val changeRate = (upResistance - downSupport) / current._3
    if (changeRate < 0.20) {
      //波动小
      return
    }

    val rate = (upResistance - current._3) / (upResistance - downSupport)
    if (rate > 0.4 && rate < 1.0) {
      //40~100的回抽
      list += new PullbackStock(symbol, rate, upResistance, downSupport)
    }
  }

  def findSupportAndResistance(sma: Stream[BigDecimal]): (Int, Int) = {
    var first: Double = 0
    var upResistanceIndex, downSupportIndex = 0
    var upping = 0 //正在变大的个数
    var downing = 0 //正在变小的个数
    var last: Double = 0.0
    sma.reverse.zipWithIndex.foreach {
      case (bigV, i) =>
        val v = bigV.doubleValue()
        if (first == 0.0) {
          //第一个数据
          first = v
          last = first
        } else {
          //println("i:"+i," v:"+v)
          if (v >= last) {
            //变大
            upping += 1
            if (downing > 2) {
              //之前在变小,现在变大，说明是支撑区
              if (v >= first) {
                //如果到达上方支撑区，则是不需要的
                return (0, 0)
              } else {
                downSupportIndex = i - 1
                if (upResistanceIndex > 0)
                  return (upResistanceIndex, downSupportIndex)
              }
            }
            downing = 0
          } else {
            // 变小
            downing += 1
            if (upping > 2) {
              //之前是在变大，说明是阻力位
              if (v >= first) {
                if (upResistanceIndex > 0) {
                  //说明已经发现过上方阻力区
                  return (0, 0)
                } else {
                  upResistanceIndex = i - 1
                }
              } else {
                //下方阻力区
                return (0, 0)
              }
            }
            upping = 0
          }
          last = v
        }
    }
    return (0, 0)
  }

  /**
   * 模板的路径
   *
   * @return 模板所在路径
   */
  override def getTemplatePath: String = "/pullback.ftl"

  /**
   * 分析完成之后，把分析的数据添加到模板引擎
   *
   * @param model 模板数据
   */
  override def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]): Unit = {
    model.put("stocks", list.sorted.toArray)
  }

  class PullbackStock(val symbol: String,
                      val rate: Double,
                      val upResistance: Double,
                      val downSupport: Double) extends Comparable[PullbackStock] {
    override def compareTo(o: PullbackStock): Int = {
      o.rate.compareTo(rate)
    }
  }

}
