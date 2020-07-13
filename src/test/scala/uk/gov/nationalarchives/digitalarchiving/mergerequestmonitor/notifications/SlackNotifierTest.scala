package uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.notifications

import org.specs2.mock.Mockito
import uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.TestGitHubAppConfig

class SlackNotifierTest extends org.specs2.mutable.Specification with Mockito {
  "the Slack notifier" should {
    "send a message to Slack" in {
      // Given
      val appConfig = TestGitHubAppConfig(
        false,
        "url",
        "org",
        "team",
        "gitHubUser",
        "gitHubApiToken",
        "url")
      val slackClient = mock[SlackClient]
      val slackNotifier = new SlackNotifier(slackClient, appConfig)

      val projects = List(
        TestProjectPresenter("some-project-name", List(
          TestMergeRequestPresenter("Some Author", "some title", "http://example.com/MR1"),
          TestMergeRequestPresenter("Other Author", "other title", "http://example.com/MR2")))
      )

      // When
      slackNotifier.sendNotification(projects, PULL_REQUEST)

      // Then
      there was one(slackClient).send(contain("some-project-name"))
    }

    "not send a message if this is a dry run" in {
      // Given
      val appConfig = TestGitHubAppConfig(
        true,
        "url",
        "org",
        "team",
        "gitHubUser",
        "gitHubApiToken",
        "url")
      val slackClient = mock[SlackClient]
      val slackNotifier = new SlackNotifier(slackClient, appConfig)

      val projects = List(
        TestProjectPresenter("some-project-name", List(
          TestMergeRequestPresenter("Some Author", "some title", "http://example.com/MR2"),
          TestMergeRequestPresenter("Other Author", "other title", "http://example.com/MR2")))
      )

      // When
      slackNotifier.sendNotification(projects, PULL_REQUEST)

      // Then
      there were no(slackClient).send(anyString)
    }
  }
}
