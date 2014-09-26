package shakey.internal

import org.apache.tapestry5.json.JSONObject
import shakey.services.LoggerSupport

/**
 * Created by jcai on 14-9-26.
 */
object StockSymbolFetcher extends LoggerSupport {
  private val url_formatter = "http://stock.finance.sina.com.cn/usstock/api/jsonp.php/x/US_CategoryService.getList?page=%s&num=60&sort=price&asc=0&market=&id="

  def main(args: Array[String]) {
    1 to 200 foreach {
      case page =>
        val content = RestClient.get(url_formatter.format(page))
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
}
