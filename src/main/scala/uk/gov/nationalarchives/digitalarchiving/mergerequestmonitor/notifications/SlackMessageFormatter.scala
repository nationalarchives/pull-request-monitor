package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters.{MergeRequestSlackPresenter, ProjectSlackPresenter}

object SlackMessageFormatter {
  def format(projects: Seq[ProjectSlackPresenter], mergeRequestsTerm: MergeRequestsTerm): String = {
    val projectsWithOpenMrs = projects.filter(project => project.mergeRequests.nonEmpty)

    if (projectsWithOpenMrs.isEmpty) {
      s"No open ${mergeRequestsTerm.term} to review! :tada:"
    } else {
      val projectMessages = projectsWithOpenMrs.map(project => {
        val mergeRequestMessages = project.mergeRequests.map(mergeRequest => formatMergeRequest(mergeRequest, project)).mkString("\n")
        s"*${project.name}*\n$mergeRequestMessages"
      }).mkString("\n")

      s"Hello team!\nHere are the ${mergeRequestsTerm.term} to review today:\n${projectMessages}"
    }
  }

  private def formatMergeRequest(mergeRequest: MergeRequestSlackPresenter, project: ProjectSlackPresenter): String = {
    s"${mergeRequest.authorName}: <${mergeRequest.url}|${mergeRequest.title}>"
  }
}

sealed trait MergeRequestsTerm { def term: String }
case object PULL_REQUEST extends MergeRequestsTerm { val term = "pull requests" }
