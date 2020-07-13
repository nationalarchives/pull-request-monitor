package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.github

object HeaderParser {
  private val LinkPattern = "(<([^>]+)>; rel=\\\"\\w+\\\")+".r
  private val SingleLinkPattern = "<([^>]+)>; rel=\\\"next\\\"".r

  def parsePageLinks(linkHeader: String): PageLinks = {
    require(LinkPattern.findFirstIn(linkHeader).nonEmpty, s"No valid links found in header '$linkHeader'")

    val firstMatch = SingleLinkPattern.findFirstMatchIn(linkHeader)
    PageLinks(firstMatch.map(_.group(1)))
  }
}

case class PageLinks(next: Option[String])
