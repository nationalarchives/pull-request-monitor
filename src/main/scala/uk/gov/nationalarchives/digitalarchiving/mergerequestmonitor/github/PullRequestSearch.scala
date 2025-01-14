package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github

import dispatch.Defaults.executor
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.GitHubAppConfig

import scala.concurrent.Future

class PullRequestSearch(gitHubClient: GitHubClient, appConfig: GitHubAppConfig) {
  def getPullRequests: Future[Seq[PullRequestSearchResults]] =
    for {
      projects <- gitHubClient.reposByTeam(appConfig.teamId)
      mergeRequests <- Future.sequence(getRepoPullRequests(projects, appConfig.pullRequestRef))
    } yield mergeRequests

  private def getRepoPullRequests(repos: Seq[Repo], pullRequestRef: String): Seq[Future[PullRequestSearchResults]] = {
    repos
      .map { repo =>
        gitHubClient
          .repoPullRequests(repo.name, pullRequestRef)
          .map(pullRequests => PullRequestSearchResults(repo, pullRequests))
      }
  }
}

case class PullRequestSearchResults(repo: Repo, pullRequests: Seq[PullRequestWithComments])
