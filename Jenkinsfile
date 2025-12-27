pipeline {
    agent any

    stages {

        stage('Checkout latest code') {
            steps {
                checkout scm
            }
        }

        stage('Stop previous deployment') {
            steps {
                sh 'docker compose down || true'
            }
        }

        stage('Deploy latest version (no rebuild)') {
            steps {
                sh 'docker compose up -d --no-build'
            }
        }
    }
}
