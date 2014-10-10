package shakey.internal

import java.io.{IOException, Closeable}
import org.apache.http.impl.client.{HttpClientBuilder, CloseableHttpClient}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{RequestBuilder, HttpGet, CloseableHttpResponse}
import shakey.ShakeyConstants
import org.apache.http.client.utils.URIBuilder
import scala.io.Source
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, TrustStrategy, SSLContexts}
import java.security.SecureRandom
import java.security.cert.X509Certificate

/**
 * rest 客户端，请求SINA的API
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
}

