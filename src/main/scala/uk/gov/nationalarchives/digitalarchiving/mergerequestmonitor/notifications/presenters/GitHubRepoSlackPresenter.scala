package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.TimeSource
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.{PullRequest, PullRequestWithComments, Repo}

import java.time.temporal.ChronoUnit

class GitHubRepoSlackPresenter(repo: Repo, pullRequests: Seq[PullRequestWithComments], timeSource: TimeSource) extends ProjectSlackPresenter {
  override def name: String = repo.name

  override def mergeRequests: Seq[MergeRequestSlackPresenter] = pullRequests.map(pullRequest => new GitHubPullRequestSlackPresenter(pullRequest, timeSource))
}

class GitHubPullRequestSlackPresenter(pullRequestWithComments: PullRequestWithComments, timeSource: TimeSource) extends MergeRequestSlackPresenter {
  val pullRequest: PullRequest = pullRequestWithComments.pullRequest
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

  override def commentUsers: String = if(pullRequestWithComments.commentUsers.isEmpty) {
    ""
  } else {
    pullRequestWithComments.commentUsers.distinct.mkString(",").trim
  }

  override def commentCount: String = if(pullRequestWithComments.commentsCount == 0) {
    ""
  } else {
    s"${pullRequestWithComments.commentsCount} comments"
  }
}
