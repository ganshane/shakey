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
  def EMA(input: Array[Double], period: Int) = {
    val multiplier = BigDecimal(2.0) / (period + 1)
    val initialSMA = input.take(period).map(BigDecimal(_)).sum / period

    //从周期起始位置开始计算EMA
    val inputStream = input.toStream.drop(period)

    lazy val stream: Stream[BigDecimal] = initialSMA #:: inputStream.zip(stream).map {
      case (value, last) =>
        (value - last) * multiplier + last
    }

    stream
  }

  def EMAWithDoubleStream(input: Stream[(BigDecimal, BigDecimal)], period: Int) = {
    val multiplier = BigDecimal(2.0) / (period + 1)

    val (sum1, sum2) = input.take(period).fold((BigDecimal(0), BigDecimal(0))) {
      case ((total1, total2), (first, second)) =>
        (total1 + first, total2 + second)
    }

    val initialSMA = (sum1 / period, sum2 / period)
    //从周期起始位置开始计算EMA
    val inputStream = input.drop(period)

    lazy val stream: Stream[(BigDecimal, BigDecimal)] = initialSMA #:: inputStream.zip(stream).map {
      case ((value1, value2), (last1, last2)) =>
        ((value1 - last1) * multiplier + last1, (value2 - last2) * multiplier + last2)
    }

    stream
  }

  def EMA(input: Stream[BigDecimal], period: Int) = {
    val multiplier = BigDecimal(2.0) / (period + 1)

    val initialSMA = input.take(period).sum / period

    //从周期起始位置开始计算EMA
    val inputStream = input.drop(period)

    lazy val stream: Stream[BigDecimal] = initialSMA #:: inputStream.zip(stream).map {
      case (value, last) =>
        (value - last) * multiplier + last
    }

    stream
  }

  def EMA1(input: Array[Double], period: Int) = {
    val returnValues = new ListBuffer[BigDecimal]()
    val multiplier = BigDecimal(2.0) / (period + 1)
    val initialSMA = input.take(period).map(BigDecimal(_)).sum / period

    returnValues += initialSMA

    input.toStream.drop(period).foreach { case value =>
      returnValues += (value - returnValues.last) * multiplier + returnValues.last
    }

    returnValues.toStream
  }
}
