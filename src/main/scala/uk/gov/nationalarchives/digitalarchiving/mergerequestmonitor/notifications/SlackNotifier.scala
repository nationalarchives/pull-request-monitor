package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.AppConfig
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters.ProjectSlackPresenter

import scala.concurrent.Future

class SlackNotifier(slackClient: SlackClient, appConfig: AppConfig) {
  def sendNotification(projects: Seq[ProjectSlackPresenter], mergeRequestsTerm: MergeRequestsTerm): Future[Unit] = {
    val message = SlackMessageFormatter.format(projects, mergeRequestsTerm)

    if (appConfig.dryRun) {
      println(s"Dry run, so not sending a request to Slack. Would have sent: $message")
      Future.successful()
    } else {
      slackClient.send(message)
    }
  }
}
