A development day project for Suzanne to refresh her Scala knowledge.

# Run GitLab monitor

You will need:

- A GitLab [private token][token]
- A Slack [webhook URL][webhook]

[token]: https://dri-dev-scm1.web.local/profile/account
[webhook]: https://api.slack.com/incoming-webhooks

Run, replacing the environment variable values:

```
MERGE_REQUEST_MONITOR_GITLAB_URL=https://gitlab-api-host/api/v3 \
MERGE_REQUEST_MONITOR_GITLAB_TOKEN=your_api_token \
MERGE_REQUEST_MONITOR_SLACK_URL=https://hooks.slack.com/your-webhook-url \
sbt "runMain uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.GitLabMergeRequestMonitor"
```

By default, this will start a dry run which just prints open merge requests to the console. To actually send messages to
Slack, add the environment variable `MERGE_REQUEST_MONITOR_DRY_RUN=false`.

# Run GitHub monitor

Run:

```
sbt "runMain uk.gov.nationalarchives.digitalarchiving.mergerequestmonitor.GitHubPullRequestMonitor"
```