package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.GitHubAppConfig

case class TestGitHubAppConfig(
    dryRun: Boolean,
    gitHubBaseUrl: String,
    organisationName: String,
    teamId: String,
    gitHubUserName: String,
    gitHubApiToken: String,
    slackBaseUrl: String,
    excludeGithubRepositories: List[String],
    botUsers: List[String],
    ignoreBotUpdates: Boolean,
    pullRequestRef: String
) extends GitHubAppConfig {

  val slackWebhookPath = "/slack/webhook"

  override val slackUrl: String = slackBaseUrl + slackWebhookPath

}
