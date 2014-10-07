package shakey.internal

import org.apache.tapestry5.json.{JSONArray, JSONObject}
import shakey.services.{Stock, LoggerSupport}
import scala.collection.mutable.ArrayBuffer
import util.control.Breaks._
import shakey.ShakeyConstants


/**
 * Created by jcai on 14-9-26.
 */
object StockSymbolFetcher extends LoggerSupport {

  def main(args: Array[String]) {
    analyzeStrongStockByDayVolume("YY")
  }

  def fetchAllStock {
    1 to 200 foreach {
      case page =>
        val content = RestClient.get(ShakeyConstants.US_STOCK_FORMATTER.format(page), encoding = "GBK")
        val jsonObject = new JSONObject(content.substring(3, content.length - 3))
        val count = jsonObject.getInt("count")
        val data = jsonObject.getJSONArray("data")

        0 until data.length() foreach {
          case j =>
            //{count:"8318",data:[{name:"Goldman Sachs Group Inc.",cname:"高盛集团",category:"",symbol:"GS",price:"184.09",diff:"-3.72",chg:"-1.98",preclose:"187.81",open:"187.46",high:"187.80",low:"183.46",amplitude:"2.31%",volume:"2999670",mktcap:"84400008279",pe:"12.12714049",market:"NYSE",category_id:"695"},
            println("\"" + data.getJSONObject(j).getString("symbol") + "\",")
        }
    }
  }


  def fetchChinaStock = {
    val buffer = new ArrayBuffer[String]()
    var count = 0
    breakable {
      1 to 4 foreach {
        case page =>
          val content = RestClient.get(ShakeyConstants.CN_STOCK_FORMATTER.format(page), encoding = "GBK")
          val jsonStr = content.substring(58, content.length - 1)
          val data = new JSONArray(jsonStr)
          0 until data.length() foreach {
            case j =>
              //{count:"8318",data:[{name:"Goldman Sachs Group Inc.",cname:"高盛集团",category:"",symbol:"GS",price:"184.09",diff:"-3.72",chg:"-1.98",preclose:"187.81",open:"187.46",high:"187.80",low:"183.46",amplitude:"2.31%",volume:"2999670",mktcap:"84400008279",pe:"12.12714049",market:"NYSE",category_id:"695"},
              val obj = data.getJSONObject(j)
              if (obj.getInt("volume") > 200000 && obj.getDouble("open") > 5.0) {
                buffer += obj.getString("symbol")
                count += 1
              }
              if (count >= 100) {
                //IB的API处理只能处理100个stock
                break;
              }
          }
      }
    }

    buffer.toArray
  }

  def fetchStockRateByDayVolume(stock: Stock, rateOverflow: Double) {
    val content = RestClient.get(ShakeyConstants.HISTORY_API_URL_FORMATTER.format(stock.symbol))
    val jsonArray = new JSONArray(content)
    val len = jsonArray.length()
    var size = ShakeyConstants.HISTORY_SIZE
    var begin = len - size
    if (begin < 0)
      begin = 0
    size = len - begin
    var volCount = 0
    begin until jsonArray.length() foreach {
      case i =>
        val obj = jsonArray.getJSONObject(i)
        volCount += obj.getInt("v")
    }
    val rate: Double = (volCount / 1.0 / size / ShakeyConstants.TRADE_SECONDS_IN_ONE_DAY / 100) * rateOverflow
    logger.debug("symbol:{} rate:{}", stock.symbol, rate)
    stock.rateOneSec = rate;
  }

  def analyzeStrongStockByDayVolume(symbol: String): Double = {
    val content = RestClient.get(ShakeyConstants.HISTORY_API_URL_FORMATTER.format(symbol))
    val jsonArray = new JSONArray(content)
    val len = jsonArray.length()
    var size = ShakeyConstants.ONE_MONTH_HISTORY_SIZE
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
        yy(i - begin) = l + (h - l) / 2
        //yy(i-begin) = math.log(obj.getDouble("c"))
        logger.debug("obj:{}", obj)
    }

    val rate = StockAlgorithm.LineSimulate(xx, yy)
    logger.debug("rate:{}", rate)

    rate
  }
}
