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
        TestProjectPresenter("some-project", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/1", 1)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n    *some-project*\n         Some Author: <http://example.com/1|Some title>\n")
    }

    "list pull requests from multiple projects" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Author A", "title A", "http://example.com/A", 1))),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Author B", "title B", "http://example.com/B", 1))),
        TestProjectPresenter("project-C", List(TestMergeRequestPresenter("Author C", "title C", "http://example.com/C", 1)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n    *project-A*\n         Author A: <http://example.com/A|title A>\n    *project-B*\n         Author B: <http://example.com/B|title B>\n    *project-C*\n         Author C: <http://example.com/C|title C>\n")
    }

    "groups pull requests by project" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Some Author", "title A1", "http://example.com/A1", 1), TestMergeRequestPresenter("Other Author", "title A2", "http://example.com/A2", 1))),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Some Author", "title B1", "http://example.com/B1", 1)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n    *project-A*\n         Some Author: <http://example.com/A1|title A1>\n         Other Author: <http://example.com/A2|title A2>\n    *project-B*\n         Some Author: <http://example.com/B1|title B1>\n")
    }

    "skip projects with no pull requests" in {
      val projects = List(
        TestProjectPresenter("project-A", List()),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/A", 1)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n    *project-B*\n         Some Author: <http://example.com/A|Some title>\n")
    }

    "lists pull requests which are over two days old" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/A", 3)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("\nHello team!\nThese pull requests have had no activity for more than two days:\n    *project-A*\n         Some Author: <http://example.com/A|Some title> *Updated 3 days ago*\n\n")
    }

    "lists a mix of old and new pull requests" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Old Author A", "Old title A", "http://example.com/A", 3))),
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("New Author A", "New title A", "http://example.com/A", 1))),
        TestProjectPresenter("project-B", List(TestMergeRequestPresenter("New Author B", "New title B", "http://example.com/B", 1)))
      )

      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("\nHello team!\nThese pull requests have had no activity for more than two days:\n    *project-A*\n         Old Author A: <http://example.com/A|Old title A> *Updated 3 days ago*\n\nHere are the pull requests to review today:\n    *project-A*\n         New Author A: <http://example.com/A|New title A>\n    *project-B*\n         New Author B: <http://example.com/B|New title B>\n")
    }

    "display Draft if the pull request is a draft" in {
      val projects = List(
        TestProjectPresenter("project-A", List(TestMergeRequestPresenter("Some Author", "Some title", "http://example.com/A", 1, " Draft"))),
      )
      val message = SlackMessageFormatter.format(projects, PULL_REQUEST)

      message must beEqualTo("Hello team!\nHere are the pull requests to review today:\n    *project-A*\n         Some Author: <http://example.com/A|Some title> Draft\n")
    }
  }
}
