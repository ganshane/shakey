package shakey.services

import com.codahale.metrics.Meter

/**
 * Created by jcai on 14-9-26.
 */
trait StockDatabase {
  def updateStockList(fun: (Stock)=>Unit)
  def findStockBySymbol(symbol:String):Option[Stock]
}
class Stock{
  def this(s:String){
    this()
    this.symbol = s;
  }
  var symbol:String = _ //代码
  var rateOneSec:Double = _ //平均的量
  val meter:Meter = new Meter()
}
