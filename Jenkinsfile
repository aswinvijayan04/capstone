pipeline {
    agent any

     triggers {
        // Every ~2 minutes, check Git for new commits; "H" spreads the check time to avoid load spikes
        pollSCM('H/2 * * * *')
    }

    environment {
        // Image name on Docker Hub, and a version tag built from the Jenkins build number
        IMAGE_NAME = 'aswinvijayan04/transaction-validator'
        IMAGE_TAG  = "1.0.${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Container Build') {
            steps {
                // Uses your laptop's Docker engine through the mounted socket
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
            }
        }

        stage('Push') {
            steps {
                // Jenkins injects the credential as two temporary variables and masks them in the log
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    // Single quotes: the shell reads the variables, so the secret never passes through Groovy.
                    // --password-stdin keeps the token out of the process list.
                    sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
                    sh 'docker push $IMAGE_NAME:$IMAGE_TAG'
                    sh 'docker logout'
                }
            }
        }
    }
}