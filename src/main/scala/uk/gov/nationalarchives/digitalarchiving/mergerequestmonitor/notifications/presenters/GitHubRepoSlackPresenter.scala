package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github.{PullRequest, Repo}

class GitHubRepoSlackPresenter(repo: Repo, pullRequests: Seq[PullRequest]) extends ProjectSlackPresenter {
  override def name: String = repo.name

  override def mergeRequests: Seq[MergeRequestSlackPresenter] = pullRequests.map(pullRequest => new GitHubPullRequestSlackPresenter(pullRequest))
}

class GitHubPullRequestSlackPresenter(pullRequest: PullRequest) extends MergeRequestSlackPresenter {
  override def title: String = pullRequest.title

  override def authorName: String = pullRequest.user.login

  override def url: String = pullRequest.html_url
}