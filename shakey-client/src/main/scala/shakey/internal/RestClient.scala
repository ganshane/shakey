package shakey.internal

import java.io.{Closeable, IOException, InputStream}
import java.security.SecureRandom
import java.security.cert.X509Certificate

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, RequestBuilder}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContexts, TrustStrategy}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import shakey.ShakeyConstants
import shakey.services.LoggerSupport

import scala.io.Source

/**
 * rest 客户端，请求SINA的API
 */
object RestClient extends LoggerSupport {
  def post(url: String, params: Option[Map[String, String]] = None, headers: Option[Map[String, String]] = None, encoding: String = ShakeyConstants.UTF8_ENCODING): String = {
    val httpClient: CloseableHttpClient = createHttpClient
    try {
      val postBuilder = RequestBuilder.post().setUri(url)
      params.foreach(_.foreach(x => postBuilder.addParameter(x._1, x._2)))
      headers.foreach(_.foreach(x => postBuilder.addHeader(x._1, x._2)))
      val response: CloseableHttpResponse = httpClient.execute(postBuilder.build())
      try {
        if (response.getStatusLine.getStatusCode == 200) {
          return Source.fromInputStream(response.getEntity.getContent, encoding).mkString
        }
        else {
          val string = Source.fromInputStream(response.getEntity.getContent, encoding).mkString
          throw new RuntimeException(response.getStatusLine.toString + "\n" + string)
        }
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

  def get(url: String, params: Option[Map[String, String]] = None, headers: Option[Map[String, String]] = None, encoding: String = ShakeyConstants.UTF8_ENCODING): String = {
    val httpClient: CloseableHttpClient = createHttpClient
    try {
      val builder = new URIBuilder(url)
      if (params.isDefined) {
        params.get.foreach {
          case (k, v) =>
            builder.addParameter(k, v)
        }
      }

      val get = new HttpGet(builder.build())
      val response: CloseableHttpResponse = httpClient.execute(get)
      try {
        if (response.getStatusLine.getStatusCode == 200) {
          return Source.fromInputStream(response.getEntity.getContent, encoding).mkString
        }
        else {
          val string = Source.fromInputStream(response.getEntity.getContent, encoding).mkString
          throw new RuntimeException(response.getStatusLine.toString + "\n" + string)
        }
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
    val sslsf = new SSLConnectionSocketFactory(
      buildSSLContext(),
      Array[String]("TLSv1"),
      null,
      SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    val client = HttpClientBuilder.create.setSSLSocketFactory(sslsf).setDefaultRequestConfig(defaultConfig).setUserAgent("shakey/1.0").build
    client
  }

  private def buildSSLContext() = {
    SSLContexts.custom()
      .setSecureRandom(new SecureRandom())
      .loadTrustMaterial(null, new TrustStrategy() {
      override def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = {
        true
      }
    }).build()
  }

  def getStream(url: String, streamFun: (InputStream) => Unit, params: Option[Map[String, String]] = None, headers: Option[Map[String, String]] = None, encoding: String = ShakeyConstants.UTF8_ENCODING) {
    val httpClient: CloseableHttpClient = createHttpClient
    try {
      val builder = new URIBuilder(url)
      if (params.isDefined) {
        params.get.foreach {
          case (k, v) =>
            builder.addParameter(k, v)
        }
      }

      val get = new HttpGet(builder.build())
      val response: CloseableHttpResponse = httpClient.execute(get)
      try {
        val statusLine = response.getStatusLine
        if (statusLine.getStatusCode == 200) {
          val contentLength = response.getHeaders("Content-Length")
          val entity = response.getEntity
          logger.debug("contentLength:{},entityLength:{}", contentLength, entity.getContentLength)
          streamFun(entity.getContent)
        }
        else {
          logger.warn("fail to fetch url {} with code {}", url, statusLine.getStatusCode)
        }
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

