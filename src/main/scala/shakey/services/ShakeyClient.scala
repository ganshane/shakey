package shakey.services

import shakey.config.ShakeyConfig
import com.ib.controller.ApiController.IConnectionHandler
import com.ib.controller.{ApiController, ApiConnection}

/**
 * shakey client
 */
object ShakeyClient extends LoggerSupport{
  def start(config:ShakeyConfig):ApiController={
    val controller = new ApiController(new ShakeyConnectionHandler,
      new ApiLogger("in"),
      new ApiLogger("out"))
    controller.connect(config.ibApiHost,config.ibApiPort,1024)
    controller
  }
  class ApiLogger(val prefix:String) extends ApiConnection.ILogger{
    def log(str: String): Unit = {
      //logger.debug(prefix+":{}",str)
    }
  }
  class ShakeyConnectionHandler extends IConnectionHandler{
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
      logger.info("ID:"+id+" CODE:{},msg:{}",errorCode,new String(errorMsg.getBytes("GBK"),"UTF8"))
    }

    def show(string: String): Unit = {
      logger.info("show:{}",string)
    }
  }
}
