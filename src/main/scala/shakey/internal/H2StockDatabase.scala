package shakey.internal

import shakey.services.{LoggerSupport, Stock, StockDatabase}
import scala.slick.driver.H2Driver.simple._
import java.util.concurrent.ConcurrentHashMap
import scala.slick.jdbc.meta.MTable

/**
 * Created by jcai on 14-9-28.
 */
class H2StockDatabase(database: Database) extends StockDatabase with LoggerSupport {
  private val db = new ConcurrentHashMap[String, Stock]()
  private val stocks: TableQuery[Stocks] = TableQuery[Stocks]
  private val data = StockSymbolFetcher fetchChinaStock;
  if (data.length > 0) {
    //如果有数据，则使用此数据
    //填充数据库
    database.withSession {
      implicit session =>
        if (MTable.getTables("STOCKS").list.size > 0) {
          stocks.ddl.drop
        }
        stocks.ddl.create
        StockSymbolFetcher.fetchChinaStock.foreach {
          case symbol =>
            stocks +=(symbol, 0, System.currentTimeMillis())
        }
    }
  }
  database.withSession {
    implicit session =>
      if (MTable.getTables("STOCKS").list.size > 0) {
        stocks.map(_.symbol).foreach {
          case symbol =>
            db.put(symbol, new Stock(symbol))
        }
      } else {
        logger.error("no symbol found in database")
      }
  }

  override def findStockBySymbol(symbol: String): Option[Stock] = {
    val value = db.get(symbol)
    if (value == null) None else Some(value)
  }

  override def updateStockList(fun: (Stock) => Unit): Unit = {
    val it = db.values().iterator()
    while (it.hasNext)
      fun(it.next())
  }
}
