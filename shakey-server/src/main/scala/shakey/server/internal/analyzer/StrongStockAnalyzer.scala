package shakey.server.internal.analyzer

import scala.collection.mutable
import org.apache.tapestry5.json.JSONArray
import shakey.server.services.StockAnalyzer
import java.util
import shakey.server.internal.stat.StockAlgorithm

/**
 * Created by jcai on 14-10-12.
 */
class StrongStockAnalyzer extends StockAnalyzer {

  class StrongStock(val symbol: String, val rate1: Double, val rate2: Double, val rate3: Double) extends Comparable[StrongStock] {
    override def compareTo(o: StrongStock): Int = {
      rate1.compareTo(o.rate1)
    }

    override def toString: String = {
      "%s,%s".format(symbol, rate1)
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
  }

  private val queue = new mutable.PriorityQueue[StrongStock]()


  override def getTemplatePath: String = {
    "/strong-stock.ftl"
  }

  override def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef]): Unit = {
    model.put("stocks", queue.dequeueAll.toArray)
  }


  override def processStockDataInOneYear(symbol: String, dayData: JSONArray): Unit = {
    val obj = dayData.getJSONObject(dayData.length() - 1)
    val cal = StockAlgorithm.calStrongRate _
    if (obj.getInt("v") > 500000 && obj.getDouble("c") > 5.0) {
      val r = (cal(dayData, 25), cal(dayData, 8), cal(dayData, 3))
      //logger.debug("seq:" + sequence + " symbol:{} rate:{}", event.symbol, r)
      queue += new StrongStock(symbol, r._1, r._2, r._3)
    }
  }
}

