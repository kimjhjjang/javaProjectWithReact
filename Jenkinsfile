pipeline {
    agent any

    tools {
        jdk 'jdk17'  // Jenkins에 설정된 JDK 이름
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/kimjhjjang/javaProjectWithReact.git'
            }
        }

        stage('Build') {
            options {
                timeout(time: 20, unit: 'MINUTES')  // t2.small에서는 빌드 시간이 더 걸림
            }
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x gradlew'
                        // t2.small 인스턴스: 순차 빌드로 메모리 절약
                        sh './gradlew clean build -x test --build-cache --daemon'
                    } else {
                        bat 'gradlew.bat clean build -x test --build-cache --daemon'
                    }
                }
            }
        }


        stage('Deploy') {
            steps {
                script {
                    // 기존 프로세스 종료 (있을 경우)
                    if (isUnix()) {
                        sh '''
                            PID=$(ps -ef | grep "demo-0.0.1-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
                            if [ ! -z "$PID" ]; then
                                echo "Killing existing process: $PID"
                                kill -9 $PID
                            fi
                        '''
                    }

                    // 새로운 애플리케이션 실행
                    if (isUnix()) {
                        sh '''
                            #!/bin/bash
                            set -e
                            
                            # 로그 디렉토리 생성
                            mkdir -p logs
                            
                            # 시작 스크립트 생성
                            cat > start-app.sh << 'EOF'
#!/bin/bash
cd /var/lib/jenkins/workspace/react-java
nohup java -jar -Dspring.profiles.active=prod server/build/libs/demo-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
EOF
                            
                            # 실행 권한 부여
                            chmod +x start-app.sh
                            
                            # 백그라운드 실행 (Jenkins가 종료해도 유지)
                            JENKINS_NODE_COOKIE=dontKillMe ./start-app.sh
                            
                            # 프로세스 시작 확인
                            sleep 5
                            if ps -ef | grep -v grep | grep demo-0.0.1-SNAPSHOT.jar > /dev/null; then
                                echo "✅ Application started successfully"
                                echo "📁 Log file: $PWD/logs/app.log"
                            else
                                echo "❌ Failed to start application"
                                if [ -f logs/app.log ]; then
                                    echo "Last 20 lines of log:"
                                    tail -20 logs/app.log
                                fi
                                exit 1
                            fi
                        '''
                    } else {
                        bat '''
                            start /B java -jar -Dspring.profiles.active=prod server\\build\\libs\\demo-0.0.1-SNAPSHOT.jar
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Build and deployment successful!'
        }
        failure {
            echo 'Build or deployment failed!'
        }
    }
}
