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
        SONAR_ORG     = 'aswinvijayan04'        // identifier, not a secret
        SONAR_PROJECT = 'aswinvijayan04_capstone'    // identifier, not a secret
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
         stage('Security Checks') {
            steps {
                // The token comes from the Jenkins credential "sonar-token" and is masked in the log
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    // Analyses the code, uploads results to SonarCloud, and waits for the Quality Gate verdict.
                    // If the gate fails, this stage fails and nothing after it runs.
                    sh '''
                      ./mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389:sonar \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.organization=$SONAR_ORG \
                        -Dsonar.projectKey=$SONAR_PROJECT \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.qualitygate.wait=true
                    '''
                }
            }
        }

        stage('Container Build') {
            steps {
                // Uses your laptop's Docker engine through the mounted socket
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
            }
        }
                stage('Security Scan') {
            steps {
                // 1) Save a readable report (never fails the build), so we always have scan evidence
                sh '''
                  docker run --rm \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v trivy-cache:/root/.cache/ \
                    aquasec/trivy:latest image \
                    --scanners vuln --severity HIGH,CRITICAL \
                    $IMAGE_NAME:$IMAGE_TAG > trivy-report.txt || true
                '''
                archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true

                // 2) The GATE: --exit-code 1 makes Trivy return an error on any HIGH or CRITICAL finding,
                //    which fails this stage, so the Push stage never runs
                sh '''
                  docker run --rm \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v trivy-cache:/root/.cache/ \
                    aquasec/trivy:latest image \
                    --scanners vuln --severity HIGH,CRITICAL \
                    --exit-code 1 \
                    $IMAGE_NAME:$IMAGE_TAG
                '''
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
                stage('Deploy') {
            steps {
                // Jenkins injects the AWS key as two temporary variables, masked in the log
                withCredentials([usernamePassword(credentialsId: 'aws-creds',
                                                  usernameVariable: 'AWS_ACCESS_KEY_ID',
                                                  passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                      export AWS_DEFAULT_REGION=ap-south-1
                      export KUBECONFIG=$WORKSPACE/kubeconfig

                      # Fetch the connection details for whichever cluster exists right now
                      aws eks update-kubeconfig --name finsecure-cluster --region ap-south-1

                      # Make sure the ConfigMap and Service exist
                      kubectl apply -f k8s/configmap.yaml -f k8s/service.yaml

                      # Point the Deployment at the image this build just pushed: this starts the rolling update
                      kubectl set image deployment/transaction-validator transaction-validator=$IMAGE_NAME:$IMAGE_TAG

                      # Record what changed, so "rollout history" is readable
                      kubectl annotate deployment/transaction-validator \
                        kubernetes.io/change-cause="Jenkins build $BUILD_NUMBER: $IMAGE_TAG" --overwrite

                      # Wait for the rollout; if it does not finish in 3 minutes, this fails the stage
                      kubectl rollout status deployment/transaction-validator --timeout=180s
                      kubectl get pods -o wide
                    '''
                }
            }
        }
    }
}