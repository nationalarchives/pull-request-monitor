package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.GitHubClient
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.SlackClient

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.reflect.io.File

class GitHubPullRequestMonitorIntegrationTest extends org.specs2.mutable.Specification with WiremockContext {

  private def getPrBody(fileNumber: Int): String =
    Source.fromResource(s"__files/github/repo-$fileNumber-prs.json")
      .getLines().mkString
      .replaceAll("\\{updated_date}", ZonedDateTime.now(ZoneId.of("UTC"))
        .minus(2, ChronoUnit.DAYS)
        .withHour(0)
        .withMinute(0)
        .withNano(0).format(DateTimeFormatter.ISO_INSTANT))

  sequential

  "the pull request monitor" should {
    "post an open merge request to Slack" in { wiremockServers: WiremockServers =>
      // Given
      val organisationName = "some-organisation"
      val teamName = "some-team-name"
      val gitHubUser = "some-username"
      val gitHubApiToken = "some-api-token"
      val reposPath = "/orgs/some-organisation/teams/some-team-name/repos"
      val repo1Path = "/repos/some-organisation/tdr-dev-documentation/pulls"
      val repo2Path = "/repos/some-organisation/tdr-prototype-mvc/pulls"
      val repo3Path = "/repos/some-organisation/prototype-server/pulls"
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl)

      wiremockServers.repoHost.stubFor(get(s"$reposPath")
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse()
          .withBodyFile("github/repos-page1.json")
          .withHeader("Link", s"""<${wiremockServers.repoHost.baseUrl}$reposPath?page=2>; rel="next", <${wiremockServers.repoHost.baseUrl}$reposPath?page=2>; rel="last"""")
        ))
      val prBodyOne = getPrBody(1)
      val prBodyTwo = getPrBody(2)
      val prBodyThree = getPrBody(3)
      wiremockServers.repoHost.stubFor(get(s"$reposPath?page=2")
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repos-page2.json")))
      wiremockServers.repoHost.stubFor(get(repo1Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody(prBodyOne)))
      wiremockServers.repoHost.stubFor(get(repo2Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody(prBodyTwo)))
      wiremockServers.repoHost.stubFor(get(repo3Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody(prBodyThree)))
      wiremockServers.slack.stubFor(post(appConfig.slackWebhookPath).willReturn(aResponse()))

      val slackClient = new SlackClient(appConfig)
      val gitHubClient = new GitHubClient(appConfig)
      val mergeRequestMonitor = new GitHubPullRequestMonitor(gitHubClient, slackClient, appConfig)

      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      val expectedJson: String = Source.fromResource("slack-messages/expected-github-slack-request.json")
        .getLines()
        .mkString("\n")

      wiremockServers.slack.verify(postRequestedFor(
        urlEqualTo(appConfig.slackWebhookPath))
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withRequestBody(equalToJson(expectedJson))
      )

      responseResult must beEqualTo[Unit]()
    }
  }
}
