
# Pull Request Monitor
This project supports the monitoring of pull requests in repositories that have specific GitHub team access.
It will send slack alerts to the channel associated with the SLACK_WEBHOOK
There are two slack messages that indicate:

 - These pull requests have had no activity for more than two days:
```
TDR Notifier APP  7:06 AM
   Hello team!
   These pull requests have had no activity for more than two days:
      tdr-e2e-tests
        xxx: [WIP- Experiment] Turn off fail fast for E2E tests Draft Updated 2 days ago
```
- Here are the pull requests to review today:
```
Here are the pull requests to review today:
    da-reference-generator
         tna-da-bot: Update dynamodb to 2.23.10
         tna-da-bot: Update aws-java-sdk-dynamodb to 1.12.644
    pull-request-monitor
```


## Workflows
| Workflow                                             | Summary |
|------------------------------------------------------|---------|
| [Run Pull Request Monitor](.github/workflowa/run.yml)| Runs pull request monitor for repos associated with a team/s                                                              |
| [TDR Run PR Monitor Tests](.github/workflowa/run.yml)| Runs [tdr test action](nationalarchives/tdr-github-actions/.github/workflows/tdr_test.yml) with sbt scalafmtCheckAll test |



### Run Pull Request Monitor
This workflow is run on a cron running pull request monitor jobs for different environments.
Within the ```.github/workflowa/run.yml``` file there are several jobs such as:
```
run-tdr:
    runs-on: ubuntu-latest
    environment: tdr
    steps:
      - uses: actions/checkout@v3
      - run: sbt run
        env:
          SLACK_URL: ${{ secrets.SLACK_WEBHOOK }}
          GITHUB_API_TOKEN: ${{ secrets.WORKFLOW_PAT }}
          TEAM: transfer-digital-records
          DRY_RUN: false
```
For each job a separate environment is required with:

- SLACK_WEBHOOK (webhook for a Slack channel to which messages are sent ) environment secret
- TEAM GitHub team name that has access to the repositories requiring checking

You will also need:

- GITHUB_API_TOKEN: repository secret used to create PR
- WORKFLOW_PAT: repository secret PAT used to create the pull requests

To scan repositories for different teams create a new job and environment
