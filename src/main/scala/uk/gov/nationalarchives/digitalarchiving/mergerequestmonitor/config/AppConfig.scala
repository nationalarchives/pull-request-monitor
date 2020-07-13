package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config

trait AppConfig {
  def slackUrl: String
  def dryRun: Boolean
}
