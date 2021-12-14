package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.TimeSource
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.{PullRequest, Repo}

import java.time.temporal.ChronoUnit

class GitHubRepoSlackPresenter(repo: Repo, pullRequests: Seq[PullRequest], timeSource: TimeSource) extends ProjectSlackPresenter {
  override def name: String = repo.name

  override def mergeRequests: Seq[MergeRequestSlackPresenter] = pullRequests.map(pullRequest => new GitHubPullRequestSlackPresenter(pullRequest, timeSource))
}

class GitHubPullRequestSlackPresenter(pullRequest: PullRequest, timeSource: TimeSource) extends MergeRequestSlackPresenter {
  override def title: String = pullRequest.title

  override def authorName: String = pullRequest.user.login

  override def url: String = pullRequest.html_url

  override def daysSinceLastUpdate: Long = ChronoUnit.DAYS.between(pullRequest.updated_at, timeSource.now)

  override def draft: String = if(pullRequest.draft) {
    " Draft"
  } else {
    ""
  }

  override def reviewStatus: String = pullRequest.reviewStatus.getOrElse("")
}
