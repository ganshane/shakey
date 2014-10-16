package shakey.server.internal.analyzer

import java.util

import org.apache.tapestry5.json.JSONArray
import shakey.server.internal.algorithm.StockAlgorithm
import shakey.server.services.StockAnalyzer

import scala.collection.mutable.ListBuffer

/**
 * 针对股票阻力位和支撑位的分析
 */
class SupportResistanceAnalyzer extends StockAnalyzer {
  private val list = new ListBuffer[SupportResistanceStock]()

  /**
   * 处理股票一年的日数据
   *
   * @param symbol 股票代码
   * @param data   股票一年的日数据,数据格式为：[{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"},{d:"2012-11-23",o:"11.50",h:"11.76",l:"11.20",c:"11.32",v:"236917"}]
   */
  override def processStockDataInOneYear(symbol: String, data: JSONArray): Unit = {
    var size = 200
    val len = data.length()
    if (len < size)
      size = len
    val begin = len - size
    //计算中间值的股价列表
    val priceStream = Range(begin, len).map { case i =>
      val stock = data.getJSONObject(i)
      (BigDecimal(stock.getDouble("l")), BigDecimal(stock.getDouble("h")), stock.getString("d"))
    }.toArray
    //先查找支撑位
    var sma = StockAlgorithm.EMA(priceStream.toStream.map(_._1), 5)
    sma = StockAlgorithm.EMA(sma, 5)

    val (upSupportIndex, _, downSupportIndex, _) = findSupportAndResistance(sma)
    var upSupport = 0.0
    if (upSupportIndex > 0 && upSupportIndex < (size - 3)) {
      upSupport = priceStream.slice(size - upSupportIndex - 3, size - upSupportIndex + 1).map(_._1).min.doubleValue()
    }
    var downSupport = 0.0
    if (downSupportIndex > 0 && downSupportIndex < (size - 1)) {
      downSupport = priceStream.slice(size - downSupportIndex - 3, size - downSupportIndex + 1).map(_._1).min.doubleValue()
    }

    //寻找阻力位
    sma = StockAlgorithm.EMA(priceStream.toStream.map(_._2), 5)
    sma = StockAlgorithm.EMA(sma, 5)
    val (_, upResistanceIndex, _, downResistanceIndex) = findSupportAndResistance(sma)
    var upResistance = 0.0
    if (upResistanceIndex > 0 && upResistanceIndex < (size - 3)) {
      upResistance = priceStream.slice(size - upResistanceIndex - 3, size - upResistanceIndex + 1).map(_._2).max.doubleValue()
    }
    var downResistance = 0.0
    if (downResistanceIndex > 0 && downResistanceIndex < (size - 3)) {
      downResistance = priceStream.slice(size - downResistanceIndex - 3, size - downResistanceIndex + 1).map(_._2).max.doubleValue()
    }

    list += new SupportResistanceStock(symbol, upSupport, upResistance, downSupport, downResistance)
  }

  def findSupportAndResistance(sma: Stream[BigDecimal]) = {
    var first, upSupport, upResistance, downSupport, downResistance = 0.0
    upResistance = 10000000
    upSupport = 10000000
    downResistance = 0
    downSupport = 0
    var upResistanceIndex, upSupportIndex, downSupportIndex, downResistanceIndex = 0
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
              if (v >= first && upSupport > v) {
                upSupport = last
                upSupportIndex = i - 1
              } else if (v < first && downSupport < v) {
                downSupport = last
                downSupportIndex = i - 1
              }
            }
            downing = 0
          } else {
            // 变小
            downing += 1
            if (upping > 2) {
              //之前是在变大，说明是阻力位
              if (v >= first && upResistance > v) {
                upResistance = last
                upResistanceIndex = i - 1
              } else if (v < first && downResistance < v) {
                downResistance = last
                downResistanceIndex = i - 1
              }
            }
            upping = 0
          }
          last = v
        }
    }
    (upSupportIndex, upResistanceIndex, downSupportIndex, downResistanceIndex)
  }

  /**
   * 模板的路径
   *
   * @return 模板所在路径
   */
  override def getTemplatePath: String = "/support-resistance.ftl"

  /**
   * 分析完成之后，把分析的数据添加到模板引擎
   *
   * @param model 模板数据
   */
  override def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]): Unit = {
    model.put("stocks", list.toArray)
  }

  class SupportResistanceStock(val symbol: String, val upSupport: Double,
                               val upResistance: Double,
                               val downSupport: Double,
                               val downResistance: Double) {
  }

}
