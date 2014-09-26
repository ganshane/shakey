package shakey.services

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
  var averageVol:Double = _ //平均的量
}
