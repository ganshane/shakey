package shakey

import scala.slick.driver.H2Driver.simple._
import shakey.config.ShakeyConfig
import scala.io.Source
import org.apache.tapestry5.ioc.annotations.{EagerLoad, Contribute, Symbol}
import org.apache.tapestry5.ioc.services.{FactoryDefaults, SymbolProvider}
import org.apache.tapestry5.ioc.{ServiceBinder, MappedConfiguration}
import shakey.internal._
import com.ib.controller.ApiController
import shakey.services.{StockDatabase, ShakeyClient}
import scala.Some
import java.io.File

/**
 * Created by jcai on 14-9-25.
 */
object ShakeyModule {
  def bind(binder:ServiceBinder){
    binder.bind(classOf[RealtimeMktDataFetcher]).eagerLoad()
    binder.bind(classOf[StockDatabase], classOf[H2StockDatabase])
    binder.bind(classOf[MessageNotifierService]).eagerLoad()
  }
  def buildApiController(config:ShakeyConfig):ApiController={
    ShakeyClient.start(config)
  }

  @EagerLoad
  def buildDatabase(@Symbol(ShakeyConstants.SERVER_HOME) serverHome: String): Database = {
    val stocks: TableQuery[Stocks] = TableQuery[Stocks]
    val dbPath = new File(serverHome + "/data/shakey").getAbsolutePath
    val db = Database.forURL("jdbc:h2:file:" + dbPath, driver = "org.h2.Driver")
    db
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
