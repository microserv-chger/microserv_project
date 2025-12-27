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
                script {
                    if (isUnix()) {
                        sh 'docker compose down'
                    } else {
                        bat 'docker compose down'
                    }
                }
            }
        }

        stage('Deploy latest version (no rebuild)') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker compose up -d --no-build'
                    } else {
                        bat 'docker compose up -d --no-build'
                    }
                }
            }
        }
    }
}
