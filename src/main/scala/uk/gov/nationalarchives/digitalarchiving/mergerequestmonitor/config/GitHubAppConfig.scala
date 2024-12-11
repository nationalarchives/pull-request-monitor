package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config

import com.typesafe.config.ConfigFactory

trait GitHubAppConfig extends AppConfig {
  def gitHubBaseUrl: String
  def organisationName: String
  def teamId: String
  def gitHubUserName: String
  def gitHubApiToken: String
  def excludeGithubRepositories: List[String]
  def pullRequestRef: String
  def ignoreBotUpdates: Boolean
  def botUsers: List[String]
}

object GitHubAppConfig extends GitHubAppConfig {
  val config = ConfigFactory.load
  override val dryRun: Boolean = config.getBoolean("dryRun")
  override val gitHubBaseUrl: String = config.getString("githubBaseUrl")
  override val organisationName: String = config.getString("organisationName")
  override val teamId: String = config.getString("team")
  override val slackUrl: String = config.getString("slackUrl")
  override val gitHubUserName: String = config.getString("githubUserName")
  override val gitHubApiToken: String = config.getString("githubApiToken")
  override val excludeGithubRepositories: List[String] = config.getString("excludeGithubRepositories").split(",").toList
  override val pullRequestRef: String = config.getString("pullRequestRef")
  override def ignoreBotUpdates: Boolean = config.getBoolean("ignoreBotUpdates")
  override def botUsers: List[String] = config.getString("botUsers").split(",").toList
}
