// Copyright 2014 Jun Tsai. All rights reserved.
// site: http://www.ganshane.com
package shakey.app

import org.slf4j.LoggerFactory
import shakey.services.{BootstrapTextSupport, GlobalLoggerConfigurationSupport, SystemEnvDetectorSupport, TapestryIocContainerSupport}
import shakey.{ShakeyConstants, ShakeyModule}

/**
 * shakey application
 */
object ShakeyApp
  extends TapestryIocContainerSupport
  with GlobalLoggerConfigurationSupport
  with SystemEnvDetectorSupport
  with BootstrapTextSupport {
  def main(args: Array[String]) {
    val serverHome = System.getProperty(ShakeyConstants.SERVER_HOME, "support")
    System.setProperty(ShakeyConstants.SERVER_HOME, serverHome)
    val config = ShakeyModule.buildShakeConfig(serverHome)
    configLogger(config.logFile, "SHAKEY")

    val logger = LoggerFactory getLogger getClass
    logger.info("Starting shakey server ....")
    val classes = List[Class[_]](
      Class.forName("shakey.ShakeyModule")
    )
    startUpContainer(classes: _*)
    printText("shakey",
      "META-INF/maven/com.ganshane/shakey/version.properties", logger);
    logger.info("shakey server started")
    join()
  }
}
