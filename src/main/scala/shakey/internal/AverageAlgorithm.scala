package shakey.internal

object AverageAlgorithm {
  /**
   * 利用直线拟合求出一段时间内股价的趋势
   * @param xx 时间序列
   * @param yy 股价序列,长度要和xx保持一致
   * @return 斜率,斜率为负值,则表明股价可能下挫，可以卖空，为正值，则股价上升，可以买入
   */
  def LineSimulate(xx:Array[Int],yy:Array[Double]):Double=
  {
    var xxsum:Int=0
    var yysum:Double = 0
    var xxyy:Double = 0
    var xxxx:Double = 0

    0 until xx.length foreach{i=>
      xxsum += xx(i)
      yysum += yy(i)
      xxyy += xx(i)*yy(i)
      xxxx += xx(i)*xx(i)
    }

    val D = xxxx-xxsum*xxsum;
    if ( D==0 ) return(-1);
    val a=(xxyy-xxsum*yysum)/D; // 斜率
    val b=(-xxsum*xxyy+xxxx*yysum)/D; //截距
    return a
  }

}
