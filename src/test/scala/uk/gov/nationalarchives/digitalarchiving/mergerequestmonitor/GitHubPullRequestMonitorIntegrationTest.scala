package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.GitHubClient
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.SlackClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

class GitHubPullRequestMonitorIntegrationTest extends org.specs2.mutable.Specification with WiremockContext {
  sequential

  "the pull request monitor" should {
    "post an open merge request to Slack" in { wiremockServers: WiremockServers =>
      // Given
      val organisationName = "some-organisation"
      val teamName = "some-team-name"
      val gitHubUser = "some-username"
      val gitHubApiToken = "some-api-token"
      val reposPath = "/teams/some-team-name/repos"
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
      wiremockServers.repoHost.stubFor(get(s"$reposPath?page=2")
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repos-page2.json")))
      wiremockServers.repoHost.stubFor(get(repo1Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-1-prs.json")))
      wiremockServers.repoHost.stubFor(get(repo2Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-2-prs.json")))
      wiremockServers.repoHost.stubFor(get(repo3Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-3-prs.json")))
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
