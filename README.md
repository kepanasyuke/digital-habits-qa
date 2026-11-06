# digital-habits-qa

## Простыми словами

Это учебный проект по тестированию: есть сервис цифровых привычек, и к нему написаны автотесты так, как это делают в команде, — сценарии на понятном языке, проверки API, базы и событий, отчёты и запуск в CI.

Сценарии описаны словами («пользователь создаёт привычку — она появляется в списке»), а код превращает их в реальные запросы к сервису и проверки в PostgreSQL. Отдельно показано, как готовить релиз: ветки, теги, чек-лист и отчёт Allure.

Учебный проект под вакансию **Fullstack-тестировщик** (автоматизация на Gherkin + ручное тестирование).
Стенд — сервис цифровых привычек `habits-service`: REST API, PostgreSQL, события в Kafka.

Проект закрывает все пункты вакансии на практике:

| Пункт вакансии | Где в проекте |
| --- | --- |
| Cucumber framework (связка `.feature` + Java steps) | `src/test/resources/features/*.feature`, `src/test/java/ru/digitalhabits/steps/` |
| REST API тестирование на Rest Assured | `src/test/java/ru/digitalhabits/support/ApiClient.java` |
| Автоматизация интеграционных тест-кейсов на Gherkin | 25 сценариев в 6 фичах, включая интеграционные (API → БД, API → Kafka) |
| SQL-запросы для проверки данных в PostgreSQL | `src/test/java/ru/digitalhabits/support/DbClient.java` |
| Kafka (концепции) | `HabitEventPublisher`, `KafkaVerifier`, `features/06_kafka_events.feature` |
| Allure Reporting | `allure-cucumber7-jvm`, вложения HTTP-запросов/ответов, `src/test/resources/allure.properties` |
| Jenkins CI/CD | `Jenkinsfile` (стадии, публикация Allure и JUnit, релизные объекты) |
| Git — понимание GitFlow | `docs/gitflow.md`, `scripts/release-branch.ps1`, `scripts/release-branch.sh` |
| Создание релизных объектов, ЦОИФТ, ветки и теги | `scripts/release-*`, `docs/release-checklist.md`, стадия «Релизные объекты» в `Jenkinsfile` |
| Тест-дизайн, smoke, тест-кейсы, отладка багов | `01_smoke.feature`, `05_validation.feature`, классы эквивалентности и граничные значения в целе `targetPerWeek` (0/1/7/8) |

## Что тестируется

API (`/api/v1`):

- `POST /users`, `GET /users/{id}` — создание и чтение пользователя
- `POST /habits`, `GET /habits/{id}`, `GET /users/{userId}/habits`, `PATCH /habits/{id}/status`, `DELETE /habits/{id}`
- `POST /habits/{id}/checkins`, `GET /habits/{id}/checkins` — отметки о выполнении
- `GET /habits/{id}/stats` — серия дней, прогресс по цели недели

Бизнес-правила, которые проверяются:

- отметка за одну и ту же дату — только одна (409)
- нельзя отметить будущую дату (400)
- нельзя отметить архивную привычку (409)
- цель недели — от 1 до 7 (400 на 0 и 8)
- удаление привычки не оставляет висячих отметок в БД
- каждый чек-ин публикует событие `HABIT_CHECKIN_CREATED` в топик `habit-events`

## Запуск

Нужны JDK 17 и Maven 3.9+. Docker для локального прогона **не нужен**: PostgreSQL поднимается
встроенным (`io.zonky.test:embedded-postgres`), Kafka — встроенным KRaft-брокером (`spring-kafka-test`).

```bash
mvn test                      # 23 сценария: smoke, CRUD, чек-ины, статистика, валидация
mvn test -Pkafka              # 2 сценария проверки событий в Kafka
mvn test -Pall                # все 25 сценариев
mvn test -Dcucumber.filter.tags=@smoke   # только smoke, как перед релизом
```

Отчёт Allure:

```bash
# вариант 1: официальный CLI
allure generate target/allure-results -o target/allure-report --clean
allure open target/allure-report

# вариант 2: maven-плагин
mvn allure:report     # target/site/allure-report
```

Если Maven не установлен, используйте `mvnw.cmd` (Windows) или `./mvnw` — wrapper скачает Maven сам.

### Стенд в Docker (как в реальном проекте)

```bash
docker compose up -d postgres kafka
APP_EVENTS_ENABLED=true mvn spring-boot:run
docker compose up -d --build app     # или весь стенд целиком
```

Переменные окружения: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP`, `APP_EVENTS_ENABLED`.

## Структура

```
src/main/java/ru/digitalhabits
  domain/        User, Habit, Checkin, HabitStatus
  repository/    Spring Data JPA
  service/       HabitService, HabitEventPublisher
  web/           контроллеры, DTO, обработка ошибок
src/main/resources/db/migration/V1__init.sql   схема БД (Flyway)
src/test/java/ru/digitalhabits
  RunCucumberTest.java                          запуск Cucumber через JUnit Platform Suite
  support/                                      ApiClient, DbClient, KafkaVerifier, инициализаторы, фильтр Allure
  steps/                                        step definitions на русском Gherkin
src/test/resources/features/                    6 фич, 25 сценариев
Jenkinsfile, docker-compose.yml, Dockerfile, scripts/, docs/
```

## Как устроены тесты

- **Один Spring Boot на прогон.** `CucumberSpringConfiguration` поднимает приложение на случайном порту,
  `PostgresInitializer` стартует встроенный PostgreSQL и подставляет `spring.datasource.*` в контекст.
- **Состояние сценария** — `ScenarioContext` в `@ScenarioScope`: id пользователя и привычки не «протекают»
  между сценариями.
- **Чистая БД перед каждым сценарием** — `Hooks#prepareDatabase` делает `TRUNCATE checkins, habits, users CASCADE`,
  поэтому проверки вида «ровно 1 отметка» детерминированы.
- **Проверки на двух уровнях**: ответ API (Rest Assured) и фактические данные в PostgreSQL (JDBC),
  например `DbSteps#noCheckins` дополнительно проверяет отсутствие висячих записей.
- **Kafka-сценарии** помечены тегом `@kafka` и по умолчанию исключены (`cucumber.filter.tags=not @kafka`),
  включаются профилем `-Pkafka`, брокер поднимается в той же JVM.

