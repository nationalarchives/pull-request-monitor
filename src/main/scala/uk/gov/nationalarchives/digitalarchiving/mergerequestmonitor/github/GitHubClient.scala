package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github

import java.time.ZonedDateTime
import dispatch.Defaults.executor
import dispatch.url
import io.circe.generic.auto._
import io.circe.parser.decode
import org.asynchttpclient.Response
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.config.GitHubAppConfig
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.http.CustomHttp

import scala.concurrent.Future

class GitHubClient(appConfig: GitHubAppConfig) {

  def reposByTeam(teamId: String): Future[Seq[Repo]] = {
    val path = s"/teams/$teamId/repos"
    paginateRepos(s"${appConfig.gitHubBaseUrl}/orgs/${appConfig.organisationName}$path")
      .map(_.filter(r => !appConfig.excludeGithubRepositories.contains(r.name) && (r.permissions.admin || r.permissions.push)))
  }

  private def filterPullRequests(pullRequests: Seq[PullRequest], pullRequestRef: String): Seq[PullRequest] = {
    if (appConfig.ignoreBotUpdates) {
      pullRequests.filterNot(pr => appConfig.botUsers.contains(pr.user.login))
    } else {
      if (pullRequestRef.nonEmpty) {
        pullRequests.filter(pr => pr.title.contains(pullRequestRef) || pr.user.login == pullRequestRef)
      } else {
        pullRequests
      }
    }
  }

  def repoPullRequests(repoName: String, pullRequestRef: String): Future[Seq[PullRequestWithComments]] = {
    val repoPrsEndpoint = s"/repos/${appConfig.organisationName}/$repoName/pulls"
    val prsResponseBody = get(repoPrsEndpoint)
    prsResponseBody.flatMap { body =>
      decode[Seq[PullRequest]](body) match {
        case Left(err) => Future.failed(err)
        case Right(prs) =>
          Future.sequence {
            val filteredPrs = filterPullRequests(prs, pullRequestRef)
            filteredPrs.map { pr =>
              val prEndpoint = s"$repoPrsEndpoint/${pr.number}"
              for {
                reviewPr <- get(s"$prEndpoint/reviews").map { reviewBody =>
                  val approvalStatus = decode[Seq[PullRequestReview]](reviewBody) match {
                    case Left(err)        => throw err
                    case Right(prReviews) => prReviews.lastOption.map(prr => if (prr.state == "APPROVED") " - Approved" else "")
                  }
                  pr.copy(reviewStatus = approvalStatus)
                }
                prWithComments <- get(s"$prEndpoint/comments").map { reviewBody =>
                  decode[List[PullRequestComments]](reviewBody) match {
                    case Left(err)       => throw err
                    case Right(comments) => PullRequestWithComments(reviewPr, comments.map(_.user.login))
                  }
                }
              } yield prWithComments
            }
          }
      }
    }
  }

  private def paginateRepos(currentPageUrl: String): Future[Seq[Repo]] =
    getByUrl(currentPageUrl).flatMap { response =>
      val body = response.getResponseBody
      val repos = Future.fromTry(decode[Seq[Repo]](body).toTry)

      val linkHeader = Option(response.getHeader("Link"))
      val nextPageUrl = linkHeader.flatMap(header => HeaderParser.parsePageLinks(header).next)
      nextPageUrl match {
        case Some(url) => repos.flatMap(currentPageRepos => paginateRepos(url).map(laterRepos => currentPageRepos ++ laterRepos))
        case None      => repos
      }
    }

  private def get(path: String): Future[String] =
    getByUrl(s"${appConfig.gitHubBaseUrl}$path").map(response => response.getResponseBody)

  private def getByUrl(gitHubUrl: String): Future[Response] = {
    val request = url(gitHubUrl)
      .as_!(appConfig.gitHubUserName, appConfig.gitHubApiToken)
      .GET
    val responseFuture: Future[Response] = CustomHttp.proxied(request)

    responseFuture.flatMap { response =>
      if (response.getStatusCode == 200) {
        Future.successful(response)
      } else {
        Future.failed(new RuntimeException(s"Request to $gitHubUrl returned status ${response.getStatusCode}"))
      }
    }
  }
}

case class Repo(name: String, permissions: Permissions)

case class Permissions(admin: Boolean, push: Boolean)

case class PullRequest(
    title: String,
    number: Integer,
    user: GitHubUser,
    html_url: String,
    updated_at: ZonedDateTime,
    draft: Boolean,
    state: String,
    reviewStatus: Option[String],
    labels: List[Label] = Nil
)

case class PullRequestWithComments(pullRequest: PullRequest, commentUsers: List[String] = Nil)

case class PullRequestReview(state: String)

case class PullRequestComments(user: GitHubUser)

case class GitHubUser(login: String)

case class Label(name: String)
