package shakey

import shakey.config.ShakeyConfig
import scala.io.Source
import org.apache.tapestry5.ioc.annotations.{Contribute,Symbol}
import org.apache.tapestry5.ioc.services.{FactoryDefaults, SymbolProvider}
import org.apache.tapestry5.ioc.{ServiceBinder, MappedConfiguration}
import shakey.internal._
import com.ib.controller.ApiController
import shakey.services.{StockDatabase, ShakeyClient}
import org.apache.commons.io.FileUtils
import scala.Some
import java.io.File

/**
 * Created by jcai on 14-9-25.
 */
object ShakeyModule {
  def bind(binder:ServiceBinder){
    binder.bind(classOf[RealtimeMktDataFetcher])
    binder.bind(classOf[StockDatabase],classOf[MemoryStockDatabase])
    binder.bind(classOf[MessageNotifierService]).eagerLoad()
    binder.bind(classOf[HistoricalDataFetcher])
    binder.bind(classOf[StockFacade]).eagerLoad()
  }
  def buildApiController(config:ShakeyConfig):ApiController={
    ShakeyClient.start(config)
  }

  def buildLocalSimpleStore(@Symbol(ShakeyConstants.SERVER_HOME) serverHome: String): LocalSimpleStore = {
    val filePath = serverHome + "/data"
    FileUtils.forceMkdir(new File(filePath))

    new LocalSimpleStore(filePath)
  }
  def buildShakeConfig(@Symbol(ShakeyConstants.SERVER_HOME) serverHome:String):ShakeyConfig={
    val filePath = serverHome + "/config/shakey.xml"
    val content = Source.fromFile(filePath, ShakeyConstants.UTF8_ENCODING).mkString
    val config = XmlLoader.parseXML[ShakeyConfig](content, xsd = Some(getClass.getResourceAsStream("/shakey/shakey.xsd")))
    config
  }
  @Contribute(classOf[SymbolProvider])
  @FactoryDefaults
  def provideFactoryDefaults(configuration: MappedConfiguration[String, AnyRef]) {
    configuration.add(ShakeyConstants.SERVER_HOME, "support")
  }
}
