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
    paginateRepos(s"${appConfig.gitHubBaseUrl}$path")
  }

  def repoPullRequests(repoName: String): Future[Seq[PullRequest]] = {
    val responseBody = get(s"/repos/${appConfig.organisationName}/$repoName/pulls")
    responseBody.flatMap(body => Future.fromTry(decode[Seq[PullRequest]](body).toTry))
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

case class Repo(name: String, `private`: Boolean)

case class PullRequest(title: String, user: GitHubUser, html_url: String, updated_at: ZonedDateTime)

case class GitHubUser(login: String)
