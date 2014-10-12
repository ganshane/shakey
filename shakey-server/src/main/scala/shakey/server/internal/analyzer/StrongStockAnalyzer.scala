package shakey.server.internal.analyzer

import scala.collection.mutable
import org.apache.tapestry5.json.JSONArray
import shakey.internal.StockAlgorithm
import shakey.server.services.StockAnalyzer
import java.util

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


  private def cal(jsonArray: JSONArray, day_len: Int): Double = {
    val len = jsonArray.length()
    var size = day_len
    var begin = len - size
    if (begin < 0)
      begin = 0
    size = len - begin

    val xx = 0 until size toArray;

    val yy = new Array[Double](size)
    begin until jsonArray.length() foreach {
      case i =>
        val obj = jsonArray.getJSONObject(i)
        //{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"}
        val h = obj.getDouble("h")
        val l = obj.getDouble("l")
        yy(i - begin) = math.log(l + (h - l) / 2)
    }

    StockAlgorithm.LineSimulate(xx, yy)
  }

  override def processStockDataInOneYear(symbol: String, dayData: JSONArray): Unit = {
    val obj = dayData.getJSONObject(dayData.length() - 1)
    if (obj.getInt("v") > 500000 && obj.getDouble("c") > 5.0) {
      val r = (cal(dayData, 25), cal(dayData, 8), cal(dayData, 3))
      //logger.debug("seq:" + sequence + " symbol:{} rate:{}", event.symbol, r)
      queue += new StrongStock(symbol, r._1, r._2, r._3)
    }
  }
}

