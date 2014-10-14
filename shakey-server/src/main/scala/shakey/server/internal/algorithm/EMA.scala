package shakey.server.internal.algorithm

import scala.collection.mutable.ListBuffer

/**
 * Calculates Exponential Moving Average (EMA) indicator
 * Created by jcai on 14-10-14.
 */
trait EMA {
  /**
   *
   * Calculates Exponential Moving Average (EMA) indicator
   * @param input Input signal
   * @param period Number of periods
   * @return Object containing operation results
   */
  def EMA(input:Array[Double], period:Int)=
  {
    val multiplier = BigDecimal(2.0) / (period + 1);
    val initialSMA = input.take(period).map(BigDecimal(_)).sum / period

    val inputStream  = input.toStream.drop(period)
    lazy val stream:Stream[BigDecimal] = initialSMA #:: stream.zip(inputStream).map{case (last,value) =>
       (value - last) * multiplier + last
    }

    stream
  }
  def EMA1(input:Array[Double], period:Int)=
  {
    val returnValues = new ListBuffer[BigDecimal]()
    val multiplier = BigDecimal(2.0) / (period + 1);
    val initialSMA = input.take(period).map(BigDecimal(_)).sum / period

    returnValues += initialSMA

    input.toStream.drop(period).foreach{case value=>
        returnValues += (value - returnValues.last) * multiplier + returnValues.last
    }

    returnValues.toStream
  }
}
