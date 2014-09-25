package shakey.config

import org.junit.{Assert, Test}
import scala.io.Source
import shakey.ShakeyConstants
import shakey.internal.XmlLoader

/**
 */
class ShakeyConfigTest {
  @Test
  def test_config{
    val io = getClass.getResourceAsStream("/test_shakey.xml")
    val content = Source.fromInputStream(io, ShakeyConstants.UTF8_ENCODING).mkString
    val config = XmlLoader.parseXML[ShakeyConfig](content, xsd = Some(getClass.getResourceAsStream("/shakey/shakey.xsd")))

    Assert.assertEquals("xxx",config.ibAccount)
  }
}
