pipeline {
    agent any

    tools {
        jdk 'jdk17'  // Jenkins에 설정된 JDK 이름
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code from repository...'
                git branch: 'main',
                    url: 'https://github.com/kimjhjjang/javaProjectWithReact.git'
                echo '✅ Code checkout completed'
            }
        }

        stage('Build') {
            options {
                timeout(time: 20, unit: 'MINUTES')
            }
            steps {
                echo '📦 Building application...'
                script {
                    if (isUnix()) {
                        sh 'chmod +x gradlew'
                        sh './gradlew clean build -x test --build-cache --daemon --quiet'
                    } else {
                        bat 'gradlew.bat clean build -x test --build-cache --daemon --quiet'
                    }
                }
                echo '✅ Build completed'
            }
        }


        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'
                script {
                    // 기존 프로세스 종료
                    if (isUnix()) {
                        sh '''
                            PID=$(ps -ef | grep "demo-0.0.1-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
                            if [ ! -z "$PID" ]; then
                                echo "🔄 Stopping existing process (PID: $PID)"
                                kill -9 $PID
                                sleep 2
                            fi
                        '''
                    }

                    // 새로운 애플리케이션 실행
                    if (isUnix()) {
                        sh '''
                            #!/bin/bash
                            set -e
                            
                            mkdir -p logs
                            
                            # 시작 스크립트 생성
                            cat > start-app.sh << 'EOF'
#!/bin/bash
cd /var/lib/jenkins/workspace/react-java
nohup java -jar -Dspring.profiles.active=prod server/build/libs/demo-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
EOF
                            
                            chmod +x start-app.sh
                            JENKINS_NODE_COOKIE=dontKillMe ./start-app.sh
                            
                            # 프로세스 시작 확인
                            sleep 5
                            if ps -ef | grep -v grep | grep demo-0.0.1-SNAPSHOT.jar > /dev/null; then
                                echo "✅ Application deployed successfully on port 9090"
                            else
                                echo "❌ Deployment failed"
                                [ -f logs/app.log ] && tail -20 logs/app.log
                                exit 1
                            fi
                        '''
                    } else {
                        bat 'start /B java -jar -Dspring.profiles.active=prod server\\build\\libs\\demo-0.0.1-SNAPSHOT.jar'
                    }
                }
            }
        }
    }

    post {
        success {
            echo '🎉 ✅ Build and deployment successful!'
            echo '🌐 Application is running on port 9090'
        }
        failure {
            echo '❌ Build or deployment failed!'
        }
    }
}
