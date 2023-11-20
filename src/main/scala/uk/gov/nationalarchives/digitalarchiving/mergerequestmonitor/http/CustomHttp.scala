package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.http

import dispatch.Http
import org.asynchttpclient.proxy.{ProxyServer, ProxyServerSelector}
import org.asynchttpclient.uri.Uri

import scala.collection.JavaConverters._

object CustomHttp {
  val proxied: Http = Http.withConfiguration(builder => builder.setProxyServerSelector(proxyServerSelector))

  private def proxyServerSelector: ProxyServerSelector = {
    val proxyHost = Option(System.getProperty("http.proxyHost"))
    val proxyPort = Option(System.getProperty("http.proxyPort")).map(port => port.toInt).getOrElse(80)
    val nonProxyHosts = Option(System.getProperty("http.nonProxyHosts"))
      .map(hosts => hosts.stripPrefix("\"").stripSuffix("\"").split("\\|").toList)
      .getOrElse(Nil)
      .asJava

    _: Uri => {
      proxyHost
        .map(host =>
          new ProxyServer.Builder(host, proxyPort)
            .setNonProxyHosts(nonProxyHosts)
            .build()
        )
        .orNull
    }
  }

}
