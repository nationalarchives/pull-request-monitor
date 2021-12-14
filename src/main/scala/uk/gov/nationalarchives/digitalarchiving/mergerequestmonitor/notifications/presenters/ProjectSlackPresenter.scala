package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications.presenters

trait ProjectSlackPresenter {
  def name: String
  def mergeRequests: Seq[MergeRequestSlackPresenter]
}

trait MergeRequestSlackPresenter {
  def title: String
  def authorName: String
  def url: String
  def daysSinceLastUpdate: Long
  def draft: String
  def reviewStatus: String
}
