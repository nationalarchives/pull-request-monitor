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

  val organisationName = "some-organisation"
  val teamName = "some-team-name"
  val gitHubUser = "some-username"
  val gitHubApiToken = "some-api-token"

  "the pull request monitor" should {
    "post an open merge request to Slack" in { wiremockServers: WiremockServers =>

      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl,
        List("tdr-prototype-mvc"),
        List("tna-da-bot", "dependabot[bot]"),
        ignoreBotUpdates = false,
        ""
      )
      val mergeRequestMonitor = initialisePullRequestMonitor(wiremockServers, appConfig)
      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      verifySlackRequest(wiremockServers, responseResult, "slack-messages/expected-github-slack-request.json")
    }

    "post an open merge request to Slack but ignore pull requests raised by bots" in { wiremockServers: WiremockServers =>

      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl,
        List("tdr-prototype-mvc"),
        List("tna-da-bot", "dependabot[bot]"),
        ignoreBotUpdates = true,
        ""
      )
      val mergeRequestMonitor = initialisePullRequestMonitor(wiremockServers, appConfig)
      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      verifySlackRequest(wiremockServers, responseResult, "slack-messages/expected-github-slack-request2.json")
    }

    "post an open merge request to Slack with PRs matching to the given pull request reference - dependabot[bot]" in { wiremockServers: WiremockServers =>

      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl,
        List("tdr-prototype-mvc"),
        List("tna-da-bot", "dependabot[bot]"),
        ignoreBotUpdates = false,
        "dependabot[bot]"
      )
      val mergeRequestMonitor = initialisePullRequestMonitor(wiremockServers, appConfig)
      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      verifySlackRequest(wiremockServers, responseResult, "slack-messages/expected-github-slack-request3.json")
    }

    "post an open merge request to Slack with PRs matching to the given pull request reference - Scala Steward Updates" in { wiremockServers: WiremockServers =>

      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl,
        List("tdr-prototype-mvc"),
        List("tna-da-bot", "dependabot[bot]"),
        ignoreBotUpdates = false,
        "Scala Steward Updates"
      )
      val mergeRequestMonitor = initialisePullRequestMonitor(wiremockServers, appConfig)
      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      verifySlackRequest(wiremockServers, responseResult, "slack-messages/expected-github-slack-request4.json")
    }

    "post an open merge request to Slack with PRs matching to the given pull request reference - scala-steward-dependencies" in { wiremockServers: WiremockServers =>

      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        wiremockServers.repoHost.baseUrl,
        organisationName,
        teamName,
        gitHubUser,
        gitHubApiToken,
        wiremockServers.slack.baseUrl,
        List("tdr-prototype-mvc"),
        List("tna-da-bot", "dependabot[bot]"),
        ignoreBotUpdates = false,
        "scala-steward-dependencies"
      )
      val mergeRequestMonitor = initialisePullRequestMonitor(wiremockServers, appConfig)
      // When
      val result = mergeRequestMonitor.notifyOpenPullRequests()

      // Then
      val responseResult = Await.result(result, 10.second)

      verifySlackRequest(wiremockServers, responseResult, "slack-messages/expected-github-slack-request5.json")
    }
  }

  def initialisePullRequestMonitor(wiremockServers: WiremockServers, appConfig: TestGitHubAppConfig): GitHubPullRequestMonitor = {
    val reposPath = "/orgs/some-organisation/teams/some-team-name/repos"
    val repo1Path = "/repos/some-organisation/tdr-dev-documentation/pulls"
    val repo2Path = "/repos/some-organisation/tdr-prototype-mvc/pulls"
    val repo1ReviewPath = "/repos/some-organisation/tdr-dev-documentation/pulls/1/reviews"
    val repo2ReviewPath1 = "/repos/some-organisation/tdr-prototype-mvc/pulls/33/reviews"
    val repo2ReviewPath2 = "/repos/some-organisation/tdr-prototype-mvc/pulls/25/reviews"
    val repo3ReviewPath1 = "/repos/some-organisation/prototype-server/pulls/33/reviews"
    val repo3ReviewPath2 = "/repos/some-organisation/prototype-server/pulls/25/reviews"
    val repo1CommentPath = "/repos/some-organisation/tdr-dev-documentation/pulls/1/comments"
    val repo2CommentPath1 = "/repos/some-organisation/tdr-prototype-mvc/pulls/33/comments"
    val repo2CommentPath2 = "/repos/some-organisation/tdr-prototype-mvc/pulls/25/comments"
    val repo3CommentPath1 = "/repos/some-organisation/prototype-server/pulls/33/comments"
    val repo3CommentPath2 = "/repos/some-organisation/prototype-server/pulls/25/comments"
    val repo3Path = "/repos/some-organisation/prototype-server/pulls"
    val repo4Path = "/repos/some-organisation/tdr-transfer-frontend/pulls"
    val repo4ReviewPath1 = "/repos/some-organisation/tdr-transfer-frontend/pulls/580/reviews"
    val repo4ReviewPath2 = "/repos/some-organisation/tdr-transfer-frontend/pulls/4320/reviews"
    val repo4ReviewPath3 = "/repos/some-organisation/tdr-transfer-frontend/pulls/4311/reviews"
    val repo4CommentPath1 = "/repos/some-organisation/tdr-transfer-frontend/pulls/580/comments"
    val repo4CommentPath2 = "/repos/some-organisation/tdr-transfer-frontend/pulls/4320/comments"
    val repo4CommentPath3 = "/repos/some-organisation/tdr-transfer-frontend/pulls/4311/comments"
    val userData = "[{\"user\" : {\"login\": \"testlogin\"}}]"

    wiremockServers.repoHost.stubFor(
      get(s"$reposPath")
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(
          aResponse()
            .withBodyFile("github/repos-page1.json")
            .withHeader(
              "Link",
              s"""<${wiremockServers.repoHost.baseUrl}$reposPath?page=2>; rel="next", <${wiremockServers.repoHost.baseUrl}$reposPath?page=2>; rel="last""""
            )
        )
    )
    wiremockServers.repoHost.stubFor(
      get(s"$reposPath?page=2")
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repos-page2.json"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo1Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-1-prs.json"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo2Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-2-prs.json"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo3Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-3-prs.json"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo4Path)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBodyFile("github/repo-4-prs.json"))
    )
    wiremockServers.slack.stubFor(post(appConfig.slackWebhookPath).willReturn(aResponse()))
    wiremockServers.repoHost.stubFor(
      get(repo1ReviewPath)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[{\"state\": \"APPROVED\"}]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo2ReviewPath1)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[{\"state\": \"CHANGES_REQUESTED\"}]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo2ReviewPath2)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[{\"state\": \"CHANGES_REQUESTED\"}]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo3ReviewPath1)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[{\"state\": \"CHANGES_REQUESTED\"}]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo3ReviewPath2)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[{\"state\": \"CHANGES_REQUESTED\"}]"))
    )
    List(repo4ReviewPath1, repo4ReviewPath2, repo4ReviewPath3).foreach(reviewPath =>
      wiremockServers.repoHost.stubFor(
        get(reviewPath)
          .withBasicAuth(gitHubUser, gitHubApiToken)
          .willReturn(aResponse().withBody("[{\"state\": \"CHANGES_REQUESTED\"}]"))
      )
    )
    wiremockServers.repoHost.stubFor(
      get(repo1CommentPath)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo2CommentPath1)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo2CommentPath2)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody(userData))
    )
    wiremockServers.repoHost.stubFor(
      get(repo3CommentPath1)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody("[]"))
    )
    wiremockServers.repoHost.stubFor(
      get(repo3CommentPath2)
        .withBasicAuth(gitHubUser, gitHubApiToken)
        .willReturn(aResponse().withBody(userData))
    )
    List(repo4CommentPath1, repo4CommentPath2, repo4CommentPath3).foreach(commentPath =>
      wiremockServers.repoHost.stubFor(
        get(commentPath)
          .withBasicAuth(gitHubUser, gitHubApiToken)
          .willReturn(aResponse().withBody("[]"))
      )
    )
    val slackClient = new SlackClient(appConfig)
    val gitHubClient = new GitHubClient(appConfig)
    new GitHubPullRequestMonitor(gitHubClient, slackClient, appConfig, new FixedTimeSource())

  }

  private def verifySlackRequest(wiremockServers: WiremockServers, responseResult: Unit, expectedSlackRequest: String) = {
    val expectedJson: String = Source
      .fromResource(expectedSlackRequest)
      .getLines()
      .mkString("\n")

    wiremockServers.slack.verify(
      postRequestedFor(urlEqualTo("/slack/webhook"))
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withRequestBody(equalToJson(expectedJson))
    )

    responseResult must beEqualTo[Unit]()
  }
}
