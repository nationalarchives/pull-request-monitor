package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import dispatch.Defaults.executor
import dispatch.{as, url}
import io.circe.generic.auto._
import io.circe.syntax._
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.AppConfig
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.http.CustomHttp

import scala.concurrent.Future

class SlackClient(appConfig: AppConfig) {
  def send(message: String): Future[Unit] = {
    val notificationBody = SlackNotification(message).asJson.toString

    val slackRequest = url(appConfig.slackUrl).POST
      .addHeader("Content-type", "application/json")
      .setBody(notificationBody)

    CustomHttp.proxied(slackRequest OK as.String).map(_ => ())
  }
}

case class SlackNotification(text: String)
