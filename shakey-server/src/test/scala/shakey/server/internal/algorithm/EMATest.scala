package shakey.server.internal.algorithm

import org.junit.{Assert, Test}

import scala.math.BigDecimal.RoundingMode

/**
 * Created by jcai on 14-10-14.
 */
class EMATest {
  @Test
  def test_ema2 {
    val testValues = Array[Double](
      26.63, 26.03, 25.80, 24.92, 25.10, 26.51, 26.45, 26.81
    );
    var sma = StockAlgorithm.EMA(testValues, 5)
    println(sma.toList)
    sma = StockAlgorithm.EMA(sma, 5)
    println(sma.toList)
  }

  @Test
  def test_ema {
    val testValues = Array[Double](
      22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24,
      22.29, 22.15, 22.39, 22.38, 22.61, 22.36, 24.05, 23.75, 23.83, 23.95,
      23.63, 23.82, 23.87, 23.65, 23.19, 23.10, 23.33, 22.68, 23.10, 22.40, 22.17
    );

    /*
    val expectedResults = Array[Double](
      22.22, 22.21, 22.24, 22.27, 22.33, 22.52, 22.80, 22.97, 23.13, 23.28, 23.34, 23.43, 23.51, 23.54,
      23.47, 23.40, 23.39, 23.26, 23.23, 23.08, 22.92
    );
    */
    val expectedResults = Array[Double](22.22, 22.21, 22.24, 22.27, 22.33, 22.33, 22.65, 22.85, 23.03, 23.19, 23.27, 23.37, 23.46, 23.5, 23.44, 23.38, 23.37, 23.24, 23.22, 23.07, 22.91)

    val result = StockAlgorithm.EMA(testValues, 10).map(_.setScale(2, RoundingMode.HALF_UP).doubleValue()).toArray

    Assert.assertTrue(expectedResults.length == result.length)
    println(expectedResults.toList)
    println(result.toList)

    0 until result.length foreach { case i =>
      Assert.assertEquals(expectedResults(i), result(i), 0.01)
    }
  }
}
