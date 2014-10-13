package shakey.server.internal.stat

import shakey.services.LoggerSupport
import org.junit.Test

/**
 * Created by jcai on 14-10-7.
 */
class StockAlgorithmTest extends LoggerSupport {
  @Test
  def test_line {
    val xx = Array[Int](1, 4)
    val yy = Array[Double](1, 1)
    val rate = StockAlgorithm.lineSimulate(xx, yy)
    logger.debug("rate:{}", rate)
  }
}
