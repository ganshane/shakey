package shakey.server.internal.algorithm

import org.junit.{Assert, Test}

/**
 * macd test
 */
class MACDTest {
  @Test
  def test_macd(): Unit = {
    val inputValues = Array[Double](
      3, 5, 4, 6, 5, 7, 5, 4, 2, 4, 8, 5, 4, 8, 9, 5, 2, 6, 8, 9,
      3, 5, 4, 6, 5, 7, 5, 4, 2, 4, 8, 5, 4, 8, 9, 5, 2, 6, 8, 9)


    val fastPeriod = 12;
    val slowPeriod = 26;
    val signalPeriod = 9;

    val expectedSignalCount = 7;
    val result = StockAlgorithm.MACD(inputValues, fastPeriod, slowPeriod, signalPeriod);

    val diff = result.toList
    println(diff)

    Assert.assertEquals(expectedSignalCount, diff.length)

  }
}
