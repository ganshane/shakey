// Copyright 2012,2013,2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services

import java.util.Properties
import java.io.{IOException, BufferedInputStream}
import org.slf4j.Logger

/**
 * 输出版本
 * @author jcai
 */
trait BootstrapTextSupport {
  protected[shakey] def printText(text:String,
                                           versionPath:String,
                                           logger:Logger){
    val version = readVersionNumber(versionPath)
    var str="""
   ______ _____   __ ________  __
  / __/ // / _ | / //_/ __/\ \/ /
 _\ \/ _  / __ |/ ,< / _/   \  /
/___/_//_/_/ |_/_/|_/___/   /_/  version:%s
            """
    try{
      val className = "org.fusesource.jansi.Ansi"
      val clazz= Thread.currentThread().getContextClassLoader.loadClass(className)
      //ansi()
      val obj = clazz.getMethod("ansi").invoke(null)
      //ansi().render
      str =clazz.
        getMethod("render",classOf[String]).
        invoke(obj,"@|green "+str.
        format("|@ @|yellow "+version+"|@ ")).toString
    }catch{
      case e: Throwable =>
        logger.debug(e.getMessage,e)
        str=str.format(text,version)
    }
    logger.info(str)
  }
  def readClientVersionNumber(resourcePath:String):String={
    var result = "UNKNOWN"

    var stream = Thread.currentThread().getContextClassLoader.getResourceAsStream(
      resourcePath)


    if (stream != null)
    {
      val properties = new Properties()


      try
      {
        stream = new BufferedInputStream(stream)

        properties.load(stream)
      }
      catch{
        case ex:IOException=>
        // Just ignore it.
      }

      val version = properties.getProperty("version")
      val buildNumber = properties.getProperty("buildNumber")
      val buildId= properties.getProperty("buildId")

      if (version != null) result = version
      if (buildNumber!=null && !buildNumber.contains("$")) result += "_"+buildNumber
      if (buildId !=null && !buildId.contains("$")) result += "_"+buildId
    }

    result

  }
  def readVersionNumber(resourcePath:String):String=
  {
    var result = "UNKNOWN"

    var stream = Thread.currentThread().getContextClassLoader.getResourceAsStream(
      resourcePath)


    if (stream != null)
    {
      val properties = new Properties()


      try
      {
        stream = new BufferedInputStream(stream)

        properties.load(stream)
      }
      catch{
        case ex:IOException=>
        // Just ignore it.
      }

      val version = properties.getProperty("version")
      val buildNumber = properties.getProperty("buildNumber")
      val buildId= properties.getProperty("buildId")

      if (version != null) result = version
      if (buildNumber!=null) result += "#"+buildNumber
      if (buildId !=null) result += "@"+buildId

    }

    result
  }
}
