package shakey

/**
 * Created by jcai on 14-9-25.
 */
object ShakeyConstants {
  final val UTF8_ENCODING="utf-8"
  final val SERVER_HOME="server.home"
  final val HISTORY_API_URL_FORMATTER= "http://stock.finance.sina.com.cn/usstock/api/json.php/US_MinKService.getDailyK?symbol=%s&___qn="
  final val HISTORY_SIZE=100
  final val TRADE_SECONDS_IN_ONE_DAY: Double = 6.5 * 60 * 60
}
