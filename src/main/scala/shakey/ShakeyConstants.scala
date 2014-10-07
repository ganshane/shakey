package shakey

/**
 * Created by jcai on 14-9-25.
 */
object ShakeyConstants {
  final val UTF8_ENCODING="utf-8"
  final val SERVER_HOME="server.home"
  final val HISTORY_API_URL_FORMATTER= "http://stock.finance.sina.com.cn/usstock/api/json.php/US_MinKService.getDailyK?symbol=%s&___qn="
  final val CN_STOCK_FORMATTER = "http://money.finance.sina.com.cn/q/api/jsonp_v2.php/x/US_ChinaStockService.getData?page=%s&num=60&sort=volume&asc=0&market=&concept=0";
  final val US_STOCK_FORMATTER = "http://stock.finance.sina.com.cn/usstock/api/jsonp.php/x/US_CategoryService.getList?page=%s&num=60&sort=price&asc=0&market=&id="
  final val HISTORY_SIZE=100
  final val TRADE_SECONDS_IN_ONE_DAY: Double = 6.5 * 60 * 60
}
