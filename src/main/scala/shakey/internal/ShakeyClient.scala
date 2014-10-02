package shakey.internal

import shakey.config.ShakeyConfig
import com.ib.controller.ApiController.IConnectionHandler
import com.ib.controller.{ApiController, ApiConnection}
import shakey.services.LoggerSupport

/**
 * shakey client
 */
object ShakeyClient extends LoggerSupport{
  def start(config: ShakeyConfig, screen: ShakeySplashScreen): ApiController = {
    val controller = new ApiController(new ShakeyConnectionHandler(screen),
      new ApiLogger("in"),
      new ApiLogger("out"))
    controller.connect(config.ibApiHost,config.ibApiPort,1024)
    controller.disconnect()
    controller
  }
  class ApiLogger(val prefix:String) extends ApiConnection.ILogger{
    def log(str: String): Unit = {
      //logger.debug(prefix+":{}",str)
    }
  }

  class ShakeyConnectionHandler(screen: ShakeySplashScreen) extends IConnectionHandler {
    def connected(): Unit = {
      logger.info("connected")
    }

    def disconnected(): Unit = {
      logger.info("disconnected")
    }

    def accountList(list: java.util.ArrayList[String]): Unit = ()

    def error(e: Exception): Unit = {
      logger.error(e.getMessage,e)
    }

    def message(id: Int, errorCode: Int, errorMsg: String): Unit = {
      screen.setErrorMessage("code:" + errorCode + new String(errorMsg.getBytes("GBK"), "UTF8"))
      logger.error("ID:"+id+" CODE:{},msg:{}",errorCode,new String(errorMsg.getBytes("GBK"),"UTF8"))
    }

    def show(string: String): Unit = {
      logger.info("show:{}",string)
    }
  }
}
