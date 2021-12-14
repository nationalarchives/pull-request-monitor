package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters.{MergeRequestSlackPresenter, ProjectSlackPresenter}

case class TestProjectPresenter(name: String, mergeRequests: Seq[MergeRequestSlackPresenter]) extends ProjectSlackPresenter

case class TestMergeRequestPresenter(authorName: String, title: String, url: String, daysSinceLastUpdate: Long, draft: String = "", reviewStatus: String = "") extends MergeRequestSlackPresenter
