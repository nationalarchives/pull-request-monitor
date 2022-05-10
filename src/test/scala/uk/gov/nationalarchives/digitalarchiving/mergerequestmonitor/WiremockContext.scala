package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.specs2.execute.{AsResult, Result}
import org.specs2.specification.ForEach

case class WiremockServers(repoHost: WireMockServer, slack: WireMockServer)

trait WiremockContext extends ForEach[WiremockServers] {
  override def foreach[R: AsResult](f: WiremockServers => R): Result = {
    val repoHost = new WireMockServer(WireMockConfiguration.options().port(8086))
    val slack = new WireMockServer(WireMockConfiguration.options().port(8085))
    val wiremockServers = WiremockServers(repoHost, slack)

    try {
      repoHost.start()
      slack.start()

      AsResult(f(wiremockServers))
    } finally {
      repoHost.stop()
      slack.stop()
    }
  }
}

