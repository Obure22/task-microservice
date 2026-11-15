# Task Microservice

Микросервис для управления задачами по RESTful API. Лабораторная работа: Java + Spring Boot + PostgreSQL + Docker.

## Стек

| Компонент | Версия / технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.3.5 (Web, Data JPA, Validation, Actuator) |
| СУБД | PostgreSQL 16 |
| Сборка | Maven (multi-stage Docker build) |
| Контейнеризация | Docker + docker-compose |
| Тестирование API | Postman |

## Как это работает

```text
Postman / curl
      │ HTTP :8080
      ▼
┌──────────────────────────┐
│  app (Spring Boot)       │
│  Controller → Service    │
│       → Repository (JPA) │
└────────────┬─────────────┘
             │ JDBC :5432
             ▼
┌──────────────────────────┐
│  db (PostgreSQL 16)      │
│  volume pgdata           │
└──────────────────────────┘
```

Оба контейнера описаны в `docker-compose.yml` и запускаются одной командой. Приложение стартует только после того, как PostgreSQL пройдёт healthcheck. Данные сохраняются в named volume `pgdata` и не теряются при перезапуске контейнеров.

## Структура проекта

```text
.
├── Dockerfile                 # multi-stage: maven-сборка → JRE-запуск
├── docker-compose.yml         # app + PostgreSQL, healthcheck, volume
├── pom.xml
├── README.md
├── REPORT.md                  # полный отчёт по лабораторной работе
├── REPORT.html                # версия отчёта для просмотра и печати
├── postman/
│   └── Task-Microservice.postman_collection.json
└── src/main/
    ├── java/ru/tpu/microservice/
    │   ├── TaskMicroserviceApplication.java
    │   ├── domain/            # Task, TaskStatus, TaskPriority
    │   ├── dto/               # запросы и ответы API
    │   ├── exception/         # TaskNotFoundException
    │   ├── repository/        # Spring Data JPA
    │   ├── service/           # бизнес-логика
    │   └── web/               # контроллер и обработчик ошибок
    └── resources/application.yml
```

## Документация

- `README.md` — инструкция по запуску и описание API.
- `REPORT.md` — полный отчёт по лабораторной работе.
- `REPORT.html` — тот же отчёт в виде готовой к просмотру и печати HTML-страницы.

## Запуск

Требуется установленный Docker Desktop (или Docker Engine + Compose v2).

```bash
git clone https://github.com/Obure22/task-microservice.git
cd task-microservice
docker compose up --build
```

После сборки:

- API: <http://localhost:8080/api/tasks>
- Healthcheck: <http://localhost:8080/actuator/health>

Проверка:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

Остановка:

```bash
docker compose down          # остановить и удалить контейнеры
docker compose down -v       # дополнительно удалить данные PostgreSQL
```

## REST API

Базовый URL: `http://localhost:8080/api/tasks`

| № | Метод | Endpoint | Назначение | Успешный код |
|---|---|---|---|---|
| 1 | GET | `/api/tasks` | Список задач (можно фильтровать `?status=NEW`) | 200 |
| 2 | GET | `/api/tasks/{id}` | Задача по идентификатору | 200 |
| 3 | POST | `/api/tasks` | Создать задачу | 201 + `Location` |
| 4 | PUT | `/api/tasks/{id}` | Полностью обновить задачу | 200 |
| 5 | PATCH | `/api/tasks/{id}` | Частично обновить задачу | 200 |
| 6 | DELETE | `/api/tasks/{id}` | Удалить задачу | 204 |

### Модель задачи

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | number | только чтение |
| `title` | string | обязательно, 1–120 символов |
| `description` | string | до 1000 символов, может быть `null` |
| `status` | enum | `NEW`, `IN_PROGRESS`, `DONE` (по умолчанию `NEW`) |
| `priority` | enum | `LOW`, `MEDIUM`, `HIGH` (по умолчанию `MEDIUM`) |
| `createdAt` | ISO-8601 | только чтение |
| `updatedAt` | ISO-8601 | только чтение |

### Примеры запросов

Создание:

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Подготовить отчёт",
        "description": "Собрать результаты за квартал",
        "status": "NEW",
        "priority": "HIGH"
      }'
```

Ответ `201 Created`:

```json
{
  "id": 1,
  "title": "Подготовить отчёт",
  "description": "Собрать результаты за квартал",
  "status": "NEW",
  "priority": "HIGH",
  "createdAt": "2026-11-15T08:30:00Z",
  "updatedAt": "2026-11-15T08:30:00Z"
}
```

Список и фильтрация:

```bash
curl http://localhost:8080/api/tasks
curl "http://localhost:8080/api/tasks?status=NEW"
```

Частичное обновление:

```bash
curl -X PATCH http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

Удаление:

```bash
curl -i -X DELETE http://localhost:8080/api/tasks/1
```

## Обработка ошибок

Все ошибки возвращаются в едином JSON-формате, приложение не «падает» на некорректных данных:

```json
{
  "timestamp": "2026-11-15T08:31:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Ошибка валидации входных данных",
  "path": "/api/tasks",
  "fieldErrors": {
    "title": "Поле 'title' обязательно и не может быть пустым"
  }
}
```

| Ситуация | HTTP-код | Сообщение |
|---|---|---|
| Пустой `title`, превышение длины | 400 | `Ошибка валидации входных данных` |
| Невалидный JSON / неизвестный enum | 400 | `Некорректный JSON в теле запроса...` |
| Неверный тип `id` в пути | 400 | `Некорректное значение параметра 'id'` |
| Задача не найдена | 404 | `Задача с id=... не найдена` |
| Метод не поддерживается | 405 | `Метод ... не поддерживается` |
| Неверный `Content-Type` | 415 | `Используйте application/json` |
| Конфликт с БД | 409 | `Запрос конфликтует с текущим состоянием базы данных` |
| Непредвиденная ошибка | 500 | `Внутренняя ошибка сервера` |

## Postman

1. Открыть Postman → **Import**.
2. Выбрать файл `postman/Task-Microservice.postman_collection.json`.
3. Коллекция использует переменную `baseUrl` со значением `http://localhost:8080`.
4. Выполнять запросы по порядку: создание → получение → обновление → удаление. В коллекции также есть запросы для проверки ошибок 400 и 404.

Новые задачи сохраняют `id` в переменную `taskId` через тест-скрипт, поэтому последующие запросы автоматически работают с только что созданной задачей.

## Конфигурация

Параметры подключения к БД задаются переменными окружения в `docker-compose.yml`:

| Переменная | Значение по умолчанию |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/taskdb` |
| `SPRING_DATASOURCE_USERNAME` | `taskuser` |
| `SPRING_DATASOURCE_PASSWORD` | `taskpass` |

Схема таблицы создаётся автоматически Hibernate (`ddl-auto: update`). Для учебного проекта этого достаточно; в production обычно используют Flyway или Liquibase.
