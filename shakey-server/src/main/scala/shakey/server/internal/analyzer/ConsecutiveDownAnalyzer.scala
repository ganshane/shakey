package shakey.server.internal.analyzer

import org.apache.tapestry5.json.{JSONObject, JSONArray}
import scala.annotation.tailrec
import scala.collection.mutable
import shakey.server.services.StockAnalyzer
import java.util

/**
 * 抓取连续下跌股票，并且出现下影线
 */
class ConsecutiveDownAnalyzer extends StockAnalyzer {

  override def getTemplatePath: String = {
    "/rb.ftl"
  }

  override def addDataToTemplateAfterFinishAnaysis(model: util.HashMap[AnyRef, AnyRef]): Unit = {
    model.put("stocks", queue.dequeueAll.toArray)
  }

  override def processStockDataInOneYear(symbol: String, data: JSONArray): Unit = {
    val last_index = data.length() - 1
    val last = data.getJSONObject(last_index)
    val (isRb, rbRate) = calRb(last)
    if (isRb) {
      //反转线
      if (backIsDown(data, last_index - 1, 3)) {
        //前面三个交易日都是下跌
        queue += new RbStock(symbol, rbRate)
      }
    }
  }

  private def calRb(json: JSONObject): (Boolean, Double) = {
    val o = json.getDouble("o")
    val c = json.getDouble("c")
    val l = json.getDouble("l")

    //(o-l) > 2 * (c-o) //反转线
    val isRb = c > o && (o - l) > 2 * (c - o) //反转线
    var rate: Double = 0
    if (isRb) {
      rate = (o - l) / (c - o)
    }
    (isRb, rate)
  }

  @tailrec
  private def backIsDown(data: JSONArray, fromIndex: Int, loop: Int): Boolean = {
    if (loop == 0)
      return true

    val current = data.getJSONObject(fromIndex)
    if (current.getDouble("c") > current.getDouble("o")) {
      //当日为上涨
      return false;
    }

    val up = data.getJSONObject(fromIndex - 1) //上个交易日
    if (up.getDouble("c") <= current.getDouble("o")) //上个交易日的收盘价小于当前日的开盘价
      return false

    return backIsDown(data, fromIndex - 1, loop - 1)
  }

  class RbStock(val symbol: String, val rate: Double) extends Comparable[RbStock] {
    override def compareTo(o: RbStock): Int = {
      rate.compareTo(o.rate)
    }

    override def toString: String = {
      "%s,%s".format(symbol, rate)
    }
  }

  private val queue = new mutable.PriorityQueue[RbStock]()
}
