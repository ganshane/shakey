package shakey.internal

import java.io.{IOException, Closeable}
import org.apache.http.impl.client.{HttpClientBuilder, CloseableHttpClient}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, CloseableHttpResponse}
import shakey.ShakeyConstants
import org.apache.http.client.utils.URIBuilder
import scala.io.Source

/**
 * Created by jcai on 14-9-26.
 */
object RestClient {
  def close(e: Closeable) {
    try {
      e.close
    }
    catch {
      case t: Throwable => {
      }
    }
  }

  def createHttpClient: CloseableHttpClient = {
    val defaultConfig: RequestConfig = RequestConfig.custom.setConnectTimeout(10 * 1000).setSocketTimeout(30 * 1000).build
    return HttpClientBuilder.create.setDefaultRequestConfig(defaultConfig).setUserAgent("nirvana/1.0").build
  }

  def get(url:String,params:Option[Map[String,String]]=None,headers:Option[Map[String,String]]=None,encoding:String=ShakeyConstants.UTF8_ENCODING):String={
    val httpClient: CloseableHttpClient = createHttpClient
    try {
      val builder = new URIBuilder(url)
      if(params.isDefined){
        params.get.foreach{case(k,v)=>
          builder.addParameter(k,v)
        }
      }

      val get = new HttpGet(builder.build())
      val response: CloseableHttpResponse = httpClient.execute(get)
      try {
        if (response.getStatusLine.getStatusCode == 200) {
          return Source.fromInputStream(response.getEntity.getContent,encoding).mkString
        }
        else throw new RuntimeException(response.getStatusLine.toString)
      }
      finally {
        close(response)
      }
    }
    catch {
      case e: IOException => {
        throw new RuntimeException(e)
      }
    }
    finally {
      close(httpClient)
    }
  }
}

