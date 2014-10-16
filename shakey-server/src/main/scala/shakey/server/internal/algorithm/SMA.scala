package shakey.server.internal.algorithm


/**
 * Calculates Simple Moving Average (SMA) indicator
 */
trait SMA {
  /**
   * Calculates Simple Moving Average (SMA) indicator
   * @param input Input signal
   * @param periods number of periods
   * @param returnImmatureValues Determines whether immature values should be taken int account
   * @return sma result
   */
  def SMA(input: Array[Double], periods: Int, returnImmatureValues: Boolean = false): Stream[BigDecimal] = {
    if (returnImmatureValues) {
      Range(0, input.length).toStream.map {
        case i =>
          val from = if (i + 1 > periods) i + 1 - periods else 0
          val size = if (i >= periods) periods else i + 1
          input.toStream.slice(from, from + size).map(BigDecimal(_)).sum / periods
      }
    } else {
      Range(0, input.length - periods + 1).toStream.map {
        case i =>
          input.toStream.slice(i, i + periods).map(BigDecimal(_)).sum / periods
      }
    }
  }

  def SMAWithStream(input: Stream[BigDecimal], periods: Int, returnImmatureValues: Boolean = false): Stream[BigDecimal] = {
    if (returnImmatureValues) {
      Range(0, input.length).toStream.map {
        case i =>
          val from = if (i + 1 > periods) i + 1 - periods else 0
          val size = if (i >= periods) periods else i + 1
          input.slice(from, from + size).sum / periods
      }
    } else {
      Range(0, input.length - periods + 1).toStream.map {
        case i =>
          input.slice(i, i + periods).sum / periods
      }
    }
  }
}
