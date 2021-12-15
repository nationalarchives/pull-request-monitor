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
  implicit class EitherUtils[U](e: Either[Exception, U]) {
    def toFuture: Future[U] = e match {
      case Left(err) => Future.failed(err)
      case Right(value) => Future(value)
    }
  }

  def reposByTeam(teamId: String): Future[Seq[Repo]] = {
    val path = s"/teams/$teamId/repos"
    paginateRepos(s"${appConfig.gitHubBaseUrl}/orgs/${appConfig.organisationName}$path")
      .map(_.filter(r => r.permissions.admin || r.permissions.push))
  }

  def repoPullRequests(repoName: String): Future[Seq[PullRequestWithComments]] = {
    val responseBody = get(s"/repos/${appConfig.organisationName}/$repoName/pulls")
    responseBody.flatMap(body => {
      decode[Seq[PullRequest]](body) match {
        case Left(err) => Future.failed(err)
        case Right(value) => Future.sequence {
          value.map(pr => {
            for {
              reviewPr <- get(s"/repos/${appConfig.organisationName}/$repoName/pulls/${pr.number}/reviews").map(reviewBody => {
                val approved = decode[Seq[PullRequestReview]](reviewBody) match {
                  case Left(err) => throw err
                  case Right(value) => value.lastOption.map(prr => if(prr.state == "APPROVED") "Approved" else "")
                }
                pr.copy(reviewStatus = approved)
              })
              commentsPr <- get(s"/repos/${appConfig.organisationName}/$repoName/pulls/${pr.number}/comments").map(reviewBody => {
                decode[List[PullRequestComments]](reviewBody) match {
                  case Left(err) => throw err
                  case Right(value) => PullRequestWithComments(reviewPr, value.map(_.user.login), value.length)
                }
              })
            } yield commentsPr
          })
        }
      }
    })
  }

  private def paginateRepos(currentPageUrl: String): Future[Seq[Repo]] = {
    getByUrl(currentPageUrl).flatMap(response => {
      val body = response.getResponseBody
      val repos = Future.fromTry(decode[Seq[Repo]](body).toTry)

      val linkHeader = Option(response.getHeader("Link"))
      val nextPageUrl = linkHeader.flatMap(header => HeaderParser.parsePageLinks(header).next)
      nextPageUrl match {
        case Some(url) => repos.flatMap(currentPageRepos => paginateRepos(url).map(laterRepos => currentPageRepos ++ laterRepos))
        case None => repos
      }
    })
  }

  private def get(path: String): Future[String] =
    getByUrl(s"${appConfig.gitHubBaseUrl}$path").map(response => response.getResponseBody)

  private def getByUrl(gitHubUrl: String): Future[Response] = {
    val request = url(gitHubUrl)
      .as_!(appConfig.gitHubUserName, appConfig.gitHubApiToken)
      .GET
    val responseFuture: Future[Response] = CustomHttp.proxied(request)

    responseFuture.flatMap(response => {
      if (response.getStatusCode == 200) {
        Future.successful(response)
      } else {
        Future.failed(new RuntimeException(s"Request to $gitHubUrl returned status ${response.getStatusCode}"))
      }
    })
  }
}

case class Repo(name: String, permissions: Permissions)

case class Permissions(admin: Boolean, push: Boolean)

case class PullRequest(title: String, number: Integer, user: GitHubUser, html_url: String, updated_at: ZonedDateTime, draft: Boolean, state: String, reviewStatus: Option[String])

case class PullRequestWithComments(pullRequest: PullRequest, commentUsers: List[String] = Nil, commentsCount: Int = 0)

case class PullRequestReview(state: String)

case class PullRequestComments(user: GitHubUser)

case class GitHubUser(login: String)
