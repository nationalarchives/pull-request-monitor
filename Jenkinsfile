pipeline {
  agent none

  stages {
    stage('Test') {
        agent {
            ecs {
                inheritFrom 'transfer-frontend'
            }
        }
        steps {
            withCredentials([string(credentialsId: 'github-jenkins-api-key', variable: 'GITHUB_API_TOKEN'), string(credentialsId: 'slack-pr-monitor', variable: 'SLACK_URL')]) {
                sh 'sbt run'
            }
        }
    }
  }
}