package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

class SlackMessageFormatterTest extends org.specs2.mutable.Specification {
  "a Slack message" should {
    "say that there are no pull requests to review if there are no projects" in {
      val message = SlackMessageFormatter.format(Seq.empty, PULL_REQUEST)
      message must beEqualTo("No open pull requests to review! :tada:")
    }


    "refer to pull requests using the supplied term" in {
      val projects = List(
        TestProjectPresenter("some-project", List()),
        TestProjectPresenter("another-project", List())
      )

      val message = SlackMessageFormatter.format(Seq.empty, PULL_REQUEST)

      message must beEqualTo("No open pull requests to review! :tada:")
    }

    "list a single merge request" in {
      val mergeRequestId = 5
      val projects = List(
        TestProjectPresenter("some-project", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/1")))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n*some-project*\nSome Author: <http://example.com/1|Some title>")
    }

    "list pull requests from multiple projects" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Author A", "title A", "http://example.com/A"))),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Author B", "title B", "http://example.com/B"))),
        TestProjectPresenter("project-C", List(TestMergeRequestPresenter("Author C", "title C", "http://example.com/C")))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n*project-A*\nAuthor A: <http://example.com/A|title A>\n*project-B*\nAuthor B: <http://example.com/B|title B>\n*project-C*\nAuthor C: <http://example.com/C|title C>")
    }

    "groups pull requests by project" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Some Author", "title A1", "http://example.com/A1"), TestMergeRequestPresenter("Other Author", "title A2", "http://example.com/A2"))),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Some Author", "title B1", "http://example.com/B1")))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n*project-A*\nSome Author: <http://example.com/A1|title A1>\nOther Author: <http://example.com/A2|title A2>\n*project-B*\nSome Author: <http://example.com/B1|title B1>")
    }

    "skip projects with no pull requests" in {
      val projects = List(
        TestProjectPresenter("project-A", List()),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/A")))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n*project-B*\nSome Author: <http://example.com/A|Some title>")
    }
  }
}
