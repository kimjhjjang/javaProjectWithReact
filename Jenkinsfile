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
                            # 로그 디렉토리 생성
                            mkdir -p logs
                            
                            # 백그라운드 실행 (Jenkins 파이프라인에서도 유지되도록)
                            BUILD_ID=dontKillMe nohup java -jar -Dspring.profiles.active=prod server/build/libs/demo-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
                            
                            # 프로세스 시작 확인
                            sleep 3
                            if ps -ef | grep -v grep | grep demo-0.0.1-SNAPSHOT.jar > /dev/null; then
                                echo "Application started successfully"
                            else
                                echo "Failed to start application"
                                exit 1
                            fi
                            
                            echo "Log file: $PWD/logs/app.log"
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
