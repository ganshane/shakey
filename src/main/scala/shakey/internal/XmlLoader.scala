package shakey.internal

import javax.xml.bind.{Marshaller, JAXBContext}
import io.Source
import javax.xml.bind.util.ValidationEventCollector
import javax.xml.validation.SchemaFactory
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import java.io._
import org.w3c.dom.ls.LSResourceResolver
import shakey.ShakeyConstants
import shakey.services.{ShakeyErrorCode, ShakeyException}
import org.apache.tapestry5.ioc.internal.services.{MapSymbolProvider, SystemEnvSymbolProvider, SystemPropertiesSymbolProvider, SymbolSourceImpl}

/**
 * 使用Jaxb方式解析XML配置文件
 *
 * @author jcai
 * @version 0.1
 */
object XmlLoader {
  /** 加载某一个配置 **/
  def loadConfig[T <: Object](filePath:String,
                              symbols:Map[String,String]=Map[String, String](),
                              encoding:String=ShakeyConstants.UTF8_ENCODING)(implicit m: Manifest[T]):T={
    val content = Source.fromFile(filePath,encoding).mkString
    parseXML(content,symbols,encoding)
  }
  def loadConfig[T <: Object](clazz:Class[T],filePath:String):T={
    loadConfig[T](filePath)(Manifest.classType(clazz))
  }
  def parseXML[T <: Object](content:String,
                            symbols:Map[String,String]=Map[String, String](),
                            encoding:String=ShakeyConstants.UTF8_ENCODING,
                            xsd:Option[InputStream]=None)(implicit m: Manifest[T]):T={
    parseXML(new ByteArrayInputStream(SymbolExpander.expand(content,symbols).getBytes(encoding)),xsd)
  }
  def parseXML[T <: Object](is:InputStream,xsd:Option[InputStream])(implicit m: Manifest[T]):T={
    val vec = new ValidationEventCollector()
    try{
      //obtain type parameter
      val clazz = m.runtimeClass.asInstanceOf[Class[T]]
      //create io reader
      val reader = new InputStreamReader(is, ShakeyConstants.UTF8_ENCODING)
      val context = JAXBContext.newInstance(clazz)
      //unmarshal xml
      val unmarshaller = context.createUnmarshaller()
      if (xsd.isDefined){
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val schemaSource = new StreamSource(xsd.get,"xml")
        val schema = sf.newSchema(schemaSource)
        unmarshaller.setSchema(schema)
        unmarshaller.setEventHandler(vec)
      }
      unmarshaller.unmarshal(reader).asInstanceOf[T]
    }catch{
      case e:Throwable =>
        throw ShakeyException.wrap(e,ShakeyErrorCode.FAIL_PARSE_XML)
    }finally {
      close(is)
      if (xsd.isDefined)
        close(xsd.get)
      if (vec.hasEvents){
        val veOption = vec.getEvents.headOption
        if (veOption.isDefined){
          val ve = veOption.get
          val vel = ve.getLocator
          throw new ShakeyException(
            "line %s column %s :%s".format(vel.getLineNumber,vel.getColumnNumber,ve.getMessage),
            ShakeyErrorCode.FAIL_PARSE_XML)
        }
      }
    }
  }
  private def close(io:Closeable){try{io.close()}catch{case e: Throwable =>}}

  /**
   * 把对象转化为XML文件
   */
  def toXml[T](obj:T,encoding:String=ShakeyConstants.UTF8_ENCODING):String={
    val context = JAXBContext.newInstance(obj.getClass)
    val marshaller = context.createMarshaller()
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    val out = new ByteArrayOutputStream
    marshaller.marshal(obj,out)
    new String(out.toByteArray,encoding)
  }
}
object SymbolExpander {
  import collection.JavaConversions._
  /**
   * 解析字符串
   */
  def expand(input:String,params:Map[String,String]=Map[String,String]())={
    val symbolSource = new SymbolSourceImpl(List(
      new SystemPropertiesSymbolProvider,
      new SystemEnvSymbolProvider,
      new MapSymbolProvider(params)
    ))
    symbolSource.expandSymbols(input)
  }
}