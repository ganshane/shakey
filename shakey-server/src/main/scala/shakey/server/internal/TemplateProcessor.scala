package shakey.server.internal

import freemarker.template.{DefaultObjectWrapper, Configuration}
import freemarker.cache.ClassTemplateLoader
import java.io.{Writer, OutputStreamWriter}
import shakey.ShakeyConstants
import org.joda.time.DateTime
import scala.collection.mutable
import shakey.server.internal.Stockanalyzer.StrongStock


/**
 * Created by jcai on 14-10-11.
 */
object TemplateProcessor {
  val configuration = new Configuration() {
    setObjectWrapper(new DefaultObjectWrapper());
    setDefaultEncoding(ShakeyConstants.UTF8_ENCODING)
    setTemplateLoader(new ClassTemplateLoader(TemplateProcessor.getClass(), "/"))
  }

  def main(args: Array[String]) {
    val model = new java.util.HashMap[Any, Any]
    val queue = new mutable.PriorityQueue[StrongStock]()
    queue += new StrongStock("xx", 1, 2, 3)
    queue += new StrongStock("yy", 2, 3, 4)
    queue += new StrongStock("yy", 0, 1, 2)
    model.put("stocks", queue.dequeueAll.toArray)

    val writer = new OutputStreamWriter(System.out)
    processTemplate("/strong-stock.ftl", model, writer)
  }

  def processTemplate(templateLocation: String, model: java.util.HashMap[Any, Any], writer: Writer) {
    val template = configuration.getTemplate(templateLocation)
    model.put("date", DateTime.now.toString("YYYY-MM-dd HH:mm:ss"))
    model.put("date_title", DateTime.now.toString("YYYYMMdd"))
    template.process(model, writer)
  }
}
