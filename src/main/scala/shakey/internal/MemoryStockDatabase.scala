package shakey.internal

import shakey.services.{Stock, StockDatabase, LoggerSupport}
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jcai on 14-9-26.
 */
class MemoryStockDatabase extends StockDatabase with LoggerSupport{
  private val db = new ConcurrentHashMap[String,Stock]()
  private val tmpStock = Array("YY","BABA","BITA")
  tmpStock.foreach(x=>db.put(x,new Stock(x)))

  override def findStockBySymbol(symbol: String): Option[Stock] = {
    val value = db.get(symbol)
    if(value == null) None else Some(value)
  }
  override def updateStockList(fun: (Stock) => Unit): Unit = {
    val it = db.values().iterator()
    while(it.hasNext)
      fun(it.next())
  }
}
