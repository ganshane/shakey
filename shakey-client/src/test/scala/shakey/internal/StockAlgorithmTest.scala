package shakey.internal

import org.junit.Test
import shakey.services.LoggerSupport

/**
 * Created by jcai on 14-10-7.
 */
class StockAlgorithmTest extends LoggerSupport {
  @Test
  def test_line {
    val xx = Array[Int](1, 4)
    val yy = Array[Double](1, 1)
    val rate = StockAlgorithm.LineSimulate(xx, yy)
    logger.debug("rate:{}", rate)
  }
}
