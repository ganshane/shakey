package shakey.server.internal.algorithm

import shakey.services.LoggerSupport
import org.junit.{Assert, Test}
import scala.math.BigDecimal.RoundingMode

/**
 * Created by jcai on 14-10-7.
 */
class SMATest extends LoggerSupport {
  @Test
  def test_SMA {
    val inputValues = Array[Double](
      22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83,
      23.95, 23.63, 23.82, 23.87, 23.65, 23.19, 23.10, 23.33, 22.68, 23.10, 22.40, 22.17
    );

    val expectedMatureValues = Array[Double](
      22.22, 22.21, 22.23, 22.26, 22.3, 22.42, 22.61, 22.77, 22.91, 23.08, 23.21, 23.38, 23.53, 23.65, 23.71, 23.68, 23.61, 23.51, 23.43, 23.28, 23.13
    );

    val periods = 10;
    val expectedResultCount = 21;

    val result = StockAlgorithm.SMA(inputValues, periods).map(_.setScale(2, RoundingMode.HALF_UP).doubleValue())
    Assert.assertEquals(expectedResultCount, result.size)

    0 until expectedResultCount foreach {
      case i =>
        Assert.assertEquals(expectedMatureValues(i), result(i), 0)
    }

    //Assert.assertArrayEquals(result.toArray,expectedMatureValues)
  }
}
