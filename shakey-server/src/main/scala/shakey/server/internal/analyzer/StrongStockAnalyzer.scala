package shakey.server.internal.analyzer

import java.util

import org.apache.tapestry5.json.JSONArray
import shakey.server.internal.algorithm.StockAlgorithm
import shakey.server.services.StockAnalyzer

import scala.collection.mutable

/**
 * Created by jcai on 14-10-12.
 */
class StrongStockAnalyzer extends StockAnalyzer {

  private val queue = new mutable.PriorityQueue[StrongStock]()

  override def getTemplatePath: String = {
    "/strong-stock.ftl"
  }

  override def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]): Unit = {
    model.put("stocks", queue.dequeueAll.toArray)
  }

  override def processStockDataInOneYear(symbol: String, dayData: JSONArray): Unit = {
    val obj = dayData.getJSONObject(dayData.length() - 1)
    if (obj.getInt("v") > 500000 && obj.getDouble("c") > 5.0) {
      val r = (StockAlgorithm.calStrongRate(dayData, 25),
        StockAlgorithm.calStrongRate(dayData, 8),
        StockAlgorithm.calStrongRate(dayData, 3))
      //logger.debug("seq:" + sequence + " symbol:{} rate:{}", event.symbol, r)
      queue += new StrongStock(symbol, r._1, r._2, r._3)
    }
  }

  class StrongStock(val symbol: String, val rate1: Double, val rate2: Double, val rate3: Double) extends Comparable[StrongStock] {
    override def compareTo(o: StrongStock): Int = {
      rate1.compareTo(o.rate1)
    }

    override def toString: String = {
      "%s,%s".format(symbol, rate1)
    }

    def getColor(): String = {
      if (isRed())
        "red"
      else if (isBlue())
        "blue"
      else if (isGray())
        "gray"
      else
        "white"
    }

    def isRed() = {
      rate1 > 0 && rate2 < 0 && rate3 > 0
    }

    def isBlue() = {
      rate1 > 0 && rate2 < 0
    }

    def isGray() = {
      rate1 > 0 && rate3 < 0
    }
  }
}

