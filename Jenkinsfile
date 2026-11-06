pipeline {
    agent any

    tools {
        maven 'maven-3.9.9'
        jdk 'jdk-17'
    }

    parameters {
        string(name: 'RELEASE_VERSION', defaultValue: '1.0.0', description: 'Версия релиза в формате X.Y.Z')
        booleanParam(name: 'RUN_KAFKA', defaultValue: false, description: 'Запустить сценарии проверки событий в Kafka')
        booleanParam(name: 'CREATE_RELEASE_OBJECTS', defaultValue: false, description: 'Создать релизную ветку и тег')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        skipDefaultCheckout()
    }

    environment {
        DB_URL = 'jdbc:postgresql://localhost:5432/habits'
        DB_USER = 'habits'
        DB_PASSWORD = 'habits'
        KAFKA_BOOTSTRAP = 'localhost:9092'
        APP_EVENTS_ENABLED = "${params.RUN_KAFKA}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --abbrev-ref HEAD'
                sh 'git log -1 --pretty=format:"%h %s (%an)"'
            }
        }

        stage('Инфраструктура: PostgreSQL и Kafka') {
            steps {
                sh 'docker compose up -d postgres kafka'
                sh '''
                    for i in $(seq 1 30); do
                        if docker compose exec -T postgres pg_isready -U habits -d habits; then
                            echo "PostgreSQL готов"
                            exit 0
                        fi
                        sleep 2
                    done
                    echo "PostgreSQL не поднялся"
                    exit 1
                '''
            }
        }

        stage('Сборка') {
            steps {
                sh 'mvn -B -DskipTests package'
            }
        }

        stage('API-тесты (Cucumber + Rest Assured + PostgreSQL)') {
            steps {
                sh 'mvn -B test'
            }
        }

        stage('Kafka-тесты') {
            when {
                expression { params.RUN_KAFKA }
            }
            steps {
                sh 'mvn -B test -Pkafka'
            }
        }

        stage('Релизные объекты') {
            when {
                expression { params.CREATE_RELEASE_OBJECTS }
            }
            steps {
                sh '''
                    set -e
                    git checkout -B "release/${RELEASE_VERSION}"
                    git push -u origin "release/${RELEASE_VERSION}"
                    git tag -a "v${RELEASE_VERSION}" -m "Release v${RELEASE_VERSION}"
                    git push origin "v${RELEASE_VERSION}"
                '''
                echo "Не забудьте приложить docs/release-checklist.md (ЦОИФТ) к релизному объекту"
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            allure includeProperties: false,
                   jdk: '',
                   results: [[path: 'target/allure-results']]
        }
        cleanup {
            sh 'docker compose down -v || true'
        }
    }
}
