package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config

import com.typesafe.config.ConfigFactory

trait GitHubAppConfig extends AppConfig {
  def gitHubBaseUrl: String
  def organisationName: String
  def teamId: String
  def gitHubUserName: String
  def gitHubApiToken: String
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
}
