package shakey.server.internal.algorithm

import scala.math.BigDecimal.RoundingMode

/**
 * Calculates Moving Average Convergence-Divergence (MACD) indicator
 */
trait MACD {
  this: EMA =>
  /**
   *
   * Calculates Moving Average Convergence-Divergence (MACD) indicator
   * @param input Input signal
   * @param fastPeriod Number of periods for fast moving averag
   * @param slowPeriod Number of periods for slow moving average
   * @param signalPeriod Number of periods for signal line
   * @return Object containing operation results
   */
  def MACD(input: Array[Double], fastPeriod: Int, slowPeriod: Int, signalPeriod: Int) = {

    val fastEMA = EMA(input, fastPeriod).drop(slowPeriod - fastPeriod)
    val slowEMA = EMA(input, slowPeriod)

    val MACD = fastEMA.zip(slowEMA).map {
      case (fastValue, slowValue) =>
        (fastValue - slowValue).setScale(2, RoundingMode.HALF_UP).doubleValue()
    }

    val signal = EMA(MACD, signalPeriod);

    signal
  }
}
