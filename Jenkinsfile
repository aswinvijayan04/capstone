// Declarative pipeline: a fixed structure of stages that Jenkins runs top to bottom.
// If any stage fails, the pipeline stops and later stages do not run.
pipeline {
    agent any    // run on any available Jenkins machine (here, the Jenkins container itself)

    stages {
        stage('Checkout') {
            steps {
                // Pull the code from the Git repo this job is connected to
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // ./mvnw = Maven Wrapper (downloads the right Maven itself); -B = batch mode, cleaner logs
                sh './mvnw -B clean compile'
            }
        }

        stage('Test') {
            steps {
                // Runs the JUnit tests. A failing test returns a non-zero exit code, which fails this stage.
                sh './mvnw -B test'
            }
            post {
                always {
                    // Publish test results in Jenkins, even when tests fail
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                // Build the jar. Tests already ran in the previous stage, so skip them here.
                sh './mvnw -B package -DskipTests'
            }
            post {
                success {
                    // Keep the jar on the Jenkins build page
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }
}