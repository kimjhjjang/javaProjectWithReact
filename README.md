# Spring Boot + React 프로젝트

Spring Boot 백엔드와 React 프론트엔드를 통합한 풀스택 웹 애플리케이션입니다.

## 📚 목차

- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [로컬 개발 환경](#로컬-개발-환경)
- [Jenkins CI/CD 설정](#jenkins-cicd-설정)
- [배포](#배포)

---

## 🛠 기술 스택

### Backend

- Java 17
- Spring Boot 3.3.0
- Gradle 8.7
- MyBatis
- MySQL
- Redis

### Frontend

- React 18
- TypeScript
- Create React App

### DevOps

- Jenkins
- AWS EC2
- Git/GitHub

---

## 📁 프로젝트 구조

```
spring-boot-with-reactjs/
├── server/               # Spring Boot 백엔드
│   ├── src/
│   └── build.gradle
├── app/                  # React 프론트엔드
│   ├── src/
│   └── package.json
├── Jenkinsfile           # Jenkins 파이프라인 설정
├── gradle.properties     # Gradle 설정
└── README.md
```

---

## 💻 로컬 개발 환경

### 사전 요구사항

- Java 17
- Node.js 18+
- MySQL (선택사항)
- Redis (선택사항)

### 백엔드 실행

```bash
# 개발 모드로 실행 (dev profile)
./gradlew :server:bootRun

# 빌드
./gradlew clean build -x test
```

애플리케이션은 `http://localhost:8080` 에서 실행됩니다.

### 프론트엔드 개발

```bash
cd app
npm install
npm start
```

개발 서버는 `http://localhost:3000` 에서 실행됩니다.

---

## 🚀 Jenkins CI/CD 설정

### 1. EC2 인스턴스 준비

#### 권장 사양

- **인스턴스 타입**: t2.medium 이상 (4GB RAM)
  - t2.small (2GB)도 가능하지만 빌드 속도가 느림
- **OS**: Amazon Linux 2023 또는 Ubuntu
- **스토리지**: 최소 20GB

#### 보안 그룹 설정 (인바운드 규칙)

| Type       | Protocol | Port Range | Source    | Description    |
| ---------- | -------- | ---------- | --------- | -------------- |
| SSH        | TCP      | 22         | 0.0.0.0/0 | SSH 접속       |
| Custom TCP | TCP      | 8080       | 0.0.0.0/0 | Spring Boot 앱 |
| Custom TCP | TCP      | 9090       | 0.0.0.0/0 | Jenkins        |

---

### 2. Jenkins 설치 (Amazon Linux 2023)

```bash
# Java 17 설치
sudo yum install java-17-amazon-corretto-devel -y

# Jenkins 저장소 추가
sudo wget -O /etc/yum.repos.d/jenkins.repo \
    https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key

# Jenkins 설치
sudo yum install jenkins -y

# Jenkins 시작
sudo systemctl start jenkins
sudo systemctl enable jenkins

# 초기 비밀번호 확인
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

---

### 3. Jenkins 초기 설정

1. **브라우저에서 접속**

   ```
   http://[EC2-Public-IP]:9090
   ```

2. **초기 비밀번호 입력**
   - 위에서 확인한 `initialAdminPassword` 입력

3. **플러그인 설치**
   - "Install suggested plugins" 선택
   - 추가로 설치할 플러그인:
     - `Pipeline: Stage View Plugin` (Stage 시각화)
     - `Blue Ocean` (선택사항, 더 나은 UI)

4. **관리자 계정 생성**
   - Username, Password 설정

---

### 4. JDK 설정

1. **Jenkins 관리 → Tools**

2. **JDK 섹션에서 Add JDK**
   - Name: `jdk17`
   - JAVA_HOME: `/usr/lib/jvm/java-17-amazon-corretto`
   - Install automatically 체크 해제

---

### 5. Pipeline 프로젝트 생성

1. **New Item** 클릭

2. **프로젝트 정보**
   - 이름: `react-java` (또는 원하는 이름)
   - 타입: **Pipeline** 선택

3. **Pipeline 설정**
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/kimjhjjang/javaProjectWithReact.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`

4. **저장**

---

### 6. Stage View 플러그인 설치 (선택사항)

시각적인 파이프라인 단계를 보려면:

1. **Jenkins 관리 → Plugins → Available plugins**

2. **검색**: `Pipeline: Stage View Plugin`

3. **Install** 클릭

4. **Restart Jenkins** 체크

5. 재시작 후 프로젝트 페이지에서 Stage View 확인

---

### 7. 빌드 실행

1. **"Build Now" 클릭**

2. **빌드 진행 상황 확인**
   - Checkout: 코드 가져오기
   - Build: Gradle 빌드 + React 빌드
   - Deploy: 애플리케이션 배포

3. **Stage View에서 각 단계별 시간 확인**

---

## 📊 파이프라인 단계

Jenkinsfile에 정의된 파이프라인:

```
📥 Checkout
  └─ Git에서 코드 체크아웃

📦 Build
  └─ Java 컴파일
  └─ React 프로덕션 빌드
  └─ JAR 패키징

🚀 Deploy
  └─ 기존 프로세스 종료
  └─ 새 애플리케이션 시작
  └─ 포트 8080에서 실행
```

---

## 🎯 배포

### 자동 배포 (Jenkins)

Jenkins에서 빌드를 실행하면:

1. 소스코드 다운로드
2. React 프로덕션 빌드
3. Spring Boot JAR 빌드
4. 애플리케이션 자동 배포

### 수동 배포

```bash
# 서버 접속
ssh -i your-key.pem ec2-user@[EC2-IP]

# 프로젝트 디렉토리
cd /var/lib/jenkins/workspace/react-java

# 애플리케이션 시작
nohup java -jar -Dspring.profiles.active=prod \
  server/build/libs/demo-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &

# 로그 확인
tail -f logs/app.log
```

---

## 🔍 트러블슈팅

### 빌드 실패 시

1. **Jenkins 로그 확인**

   ```
   Build → Console Output
   ```

2. **애플리케이션 로그 확인**

   ```bash
   tail -100 /var/lib/jenkins/workspace/react-java/logs/app.log
   ```

3. **프로세스 확인**
   ```bash
   ps -ef | grep demo-0.0.1-SNAPSHOT.jar
   sudo ss -tlnp | grep 8080
   ```

### 메모리 부족 시

t2.small 인스턴스에서 메모리 부족 발생 시:

1. **스왑 메모리 추가**

   ```bash
   sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
   sudo chmod 600 /swapfile
   sudo mkswap /swapfile
   sudo swapon /swapfile
   echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
   ```

2. **또는 인스턴스 업그레이드**
   - t2.medium 권장 (4GB RAM)

---

## 📝 환경 변수

### Development (dev)

- 포트: 8080
- 프로파일: `dev`

### Production (prod)

- 포트: 8080
- 프로파일: `prod`
- Database: MySQL (선택사항)
- Cache: Redis (선택사항)

---

## 🔗 접속 URL

- **로컬 개발**: http://localhost:8080
- **배포 환경**: http://[EC2-Public-IP]:8080
- **Jenkins**: http://[EC2-Public-IP]:9090

---

## 📄 라이센스

MIT License

---

## 👤 작성자

김정훈

---

## 📮 문의

이슈가 있으시면 GitHub Issues에 등록해주세요.
