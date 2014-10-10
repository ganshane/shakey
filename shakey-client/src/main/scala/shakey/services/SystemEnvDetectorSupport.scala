// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.services

import org.slf4j.Logger
import java.util;
import java.net.{UnknownHostException, InetAddress}
import java.lang.management.ManagementFactory

/**
 * detect system information
 */
trait SystemEnvDetectorSupport {
  protected def detectAndPrintSystemEnv(logger:Logger){
    val l = new util.ArrayList[Entry]();
    try {
      put(l,"host.name",InetAddress.getLocalHost.getCanonicalHostName)
    } catch {
      case e:UnknownHostException =>
        put(l,"host.name", "<NA>")
    }
    put(l, "java.version",
      System.getProperty("java.version", "<NA>"));
    put(l, "java.vendor",
      System.getProperty("java.vendor", "<NA>"));
    put(l, "java.home",
      System.getProperty("java.home", "<NA>"));
    put(l, "java.class.path",
      System.getProperty("java.class.path", "<NA>"));
    put(l, "java.library.path",
      System.getProperty("java.library.path", "<NA>"));
    put(l, "java.io.tmpdir",
      System.getProperty("java.io.tmpdir", "<NA>"));
    put(l, "java.compiler",
      System.getProperty("java.compiler", "<NA>"));
    put(l, "os.name",
      System.getProperty("os.name", "<NA>"));
    put(l, "os.arch",
      System.getProperty("os.arch", "<NA>"));
    put(l, "os.version",
      System.getProperty("os.version", "<NA>"));
    put(l, "user.name",
      System.getProperty("user.name", "<NA>"));
    put(l, "user.home",
      System.getProperty("user.home", "<NA>"));
    put(l, "user.dir",
      System.getProperty("user.dir", "<NA>"));
    val it = l.iterator()
    while(it.hasNext){
      val entry = it.next()
      logger.info("{}={}",entry.k,entry.v)
    }
    try {
      val runtimemxBean = ManagementFactory.getRuntimeMXBean
      val arguments = runtimemxBean.getInputArguments.toArray.mkString(" ")
      logger.info("args={}",arguments)
    }catch{
      case e:Throwable =>
    }
  }
  private def put(l:util.ArrayList[Entry],k:String,v:String){
    l.add(Entry(k,v))
  }
  case class Entry(k:String,v:String)
}
