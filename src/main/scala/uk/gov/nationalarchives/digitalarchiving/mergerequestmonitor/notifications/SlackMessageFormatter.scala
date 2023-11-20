package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters.{MergeRequestSlackPresenter, ProjectSlackPresenter}

object SlackMessageFormatter {

  def format(projects: Seq[ProjectSlackPresenter], mergeRequestsTerm: MergeRequestsTerm): String = {
    val projectsWithOpenMrs = projects.filter(_.mergeRequests.nonEmpty)

    if (projectsWithOpenMrs.isEmpty) {
      s"No open ${mergeRequestsTerm.term} to review! :tada:"
    } else {
      val newPrProjectMessages: String = projectsToString(projectsWithOpenMrs, pr => pr.daysSinceLastUpdate < 2, newPrToString)
      val oldPrProjectMessages: String = projectsToString(projectsWithOpenMrs, pr => pr.daysSinceLastUpdate >= 2, mergeRequest => s"${newPrToString(mergeRequest)} ${updatedSince(mergeRequest.daysSinceLastUpdate)}")

      val welcomeMsg = "Hello team!\n"
      val oldPrs = if (oldPrProjectMessages.nonEmpty) s"\n${welcomeMsg}These pull requests have had no activity for more than two days:\n$oldPrProjectMessages\n\n" else welcomeMsg
      if (newPrProjectMessages.nonEmpty) s"${oldPrs}Here are the pull requests to review today:\n$newPrProjectMessages\n" else oldPrs
    }
  }

  private def projectsToString(projectsWithOpenMrs: Seq[ProjectSlackPresenter], filterFn: MergeRequestSlackPresenter => Boolean, strFn: MergeRequestSlackPresenter => String) = {
    projectsWithOpenMrs.map { pr =>
      val prs = pr.mergeRequests.filter(filterFn).map(strFn)
      if (prs.nonEmpty) s"    *${pr.name}*\n${prs.mkString("\n")}" else ""
    }.filter(_.nonEmpty).mkString("\n")
  }

  private def newPrToString: MergeRequestSlackPresenter => String = mergeRequest =>
    s"         ${mergeRequest.authorName}: <${mergeRequest.url} | ${mergeRequest.title}>${mergeRequest.draft}${mergeRequest.reviewStatus}${mergeRequest.commentUsers}"

  private def updatedSince(days: Long): String = s"*Updated $days ${if (days == 1) "day" else "days"} ago*"
}

sealed trait MergeRequestsTerm {
  def term: String
}

case object PULL_REQUEST extends MergeRequestsTerm {
  val term = "pull requests"
}
