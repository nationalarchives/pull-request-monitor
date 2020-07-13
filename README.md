A development day project for Suzanne to refresh her Scala knowledge.

# Run Github monitor

You will need:

- A Github API token
- A Slack [webhook URL][webhook]

[webhook]: https://api.slack.com/incoming-webhooks

Run, replacing the environment variable values:

```
GITHUB_API_TOKEN=your_api_token \
SLACK_URL=https://hooks.slack.com/your-webhook-url \
sbt run
```
