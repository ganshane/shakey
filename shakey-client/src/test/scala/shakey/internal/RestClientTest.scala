package shakey.internal

import java.io.{File, FileOutputStream}

import org.apache.commons.io.{FileUtils, IOUtils}
import org.junit.Test
import shakey.services.LoggerSupport

/**
 * Created by jcai on 14-10-5.
 */
class RestClientTest extends LoggerSupport {

  @Test
  def test {
    val header = Map[String, String](
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
      "Accept-Encoding" -> "gzip,deflate,sdch",
      "Accept-Language" -> "zh-CN,zh;q=0.8,en;q=0.6",
      "Cache-Control" -> "max-age=0",
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36"
    )
    FileUtils.forceMkdir(new File("target"))
    val seq = 86637
    seq until 86765 foreach { case i =>
      logger.debug("process {}", i)
      RestClient.getStream("http://www.pristine.com/newsletterimages/%s.gif".format(i), { stream =>

        logger.debug("{} has data", i)
        val file = new File("target/%s.gif".format(i))

        val outputStream = new FileOutputStream(file)
        IOUtils.copy(stream, outputStream)
        IOUtils.closeQuietly(outputStream)

        logger.debug("size:{}", file.length())
      }, headers = Some(header))
    }
  }
}
