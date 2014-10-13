package shakey.server.services

import org.apache.tapestry5.json.JSONArray
import java.util


/**
 * 抽象的股票分析类
 *
 * @author jcai
 */
trait StockAnalyzer {
  /**
   * 处理股票一年的日数据
   *
   * @param symbol 股票代码
   * @param data   股票一年的日数据,数据格式为：[{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"},{d:"2012-11-23",o:"11.50",h:"11.76",l:"11.20",c:"11.32",v:"236917"}]
   *               参见：http://stock.finance.sina.com.cn/usstock/api/json.php/US_MinKService.getDailyK?symbol=yy&___qn=
   */
  def processStockDataInOneYear(symbol: String, data: JSONArray)

  /**
   * 分析完成之后，把分析的数据添加到模板引擎
   *
   * @param model 模板数据
   */
  def addDataToTemplateAfterAnalysis(model: util.HashMap[AnyRef, AnyRef])

  /**
   * 模板的路径
   *
   * @return 模板所在路径
   */
  def getTemplatePath: String
}
