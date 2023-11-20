package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor

import com.typesafe.config.ConfigFactory
import dispatch.Defaults.executor
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.{CurrentTimeSource, GitHubAppConfig, TimeSource}
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.{GitHubClient, PullRequestSearch}
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters.GitHubRepoSlackPresenter
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.{PULL_REQUEST, SlackClient, SlackNotifier}

import scala.concurrent.Future
import scala.util.Failure

class GitHubPullRequestMonitor(gitHubClient: GitHubClient, slackClient: SlackClient, appConfig: GitHubAppConfig, timeSource: TimeSource) {
  def notifyOpenPullRequests(): Future[Unit] = {
    val pullRequestResults = new PullRequestSearch(gitHubClient, appConfig).getPullRequests
    pullRequestResults.flatMap{searchResults =>
      val slackPresenters = searchResults.map(result => new GitHubRepoSlackPresenter(result.repo, result.pullRequests, timeSource))
      new SlackNotifier(slackClient, appConfig).sendNotification(slackPresenters, PULL_REQUEST)
    }
  }
}

object GitHubPullRequestMonitor extends App {
  val gitHubClient = new GitHubClient(GitHubAppConfig)
  val slackClient = new SlackClient(GitHubAppConfig)
  val monitor = new GitHubPullRequestMonitor(gitHubClient, slackClient, GitHubAppConfig, new CurrentTimeSource())
  val result = monitor.notifyOpenPullRequests()

  result.onComplete {
    case Failure(e) =>
      println("Error in Merge Request Monitor")
      e.printStackTrace()
      System.exit(1)
    case _ =>
      println("Merge Request Monitor complete")
      System.exit(0)
  }
}
