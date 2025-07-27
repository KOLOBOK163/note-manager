# Notes Application

Полнофункциональное приложение для создания и управления заметками с микросервисной архитектурой.

## Архитектура

Проект состоит из следующих сервисов:

- **Auth Service** (порт 8082) - Сервис аутентификации и управления пользователями
- **Notes Service** (порт 8081) - Сервис управления заметками
- **Frontend** (порт 3000) - React фронтенд с современным UI
- **PostgreSQL** - База данных для auth-service
- **PostgreSQL** - База данных для notes-service
- **MinIO** - S3-совместимое хранилище для файлов
- **MailHog** - SMTP сервер для тестирования email

## Технологии

### Backend
- Java 17 + Spring Boot
- Spring Security + JWT
- PostgreSQL
- Maven
- Docker

### Frontend
- React 18 + TypeScript
- Tailwind CSS
- React Router
- Axios
- React Hook Form + Yup

## Быстрый запуск

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd notes-application
```

2. Запустите все сервисы с помощью Docker Compose:
```bash
docker-compose up -d
```

3. Откройте приложение в браузере:
- Фронтенд: http://localhost:3000
- Auth API: http://localhost:8082
- Notes API: http://localhost:8081
- MailHog UI: http://localhost:8025
- MinIO Console: http://localhost:9001

## Использование

1. **Регистрация**: Создайте новый аккаунт на странице регистрации
2. **Вход**: Войдите в систему с вашими учетными данными
3. **Создание заметок**: Используйте кнопку "Новая заметка" для создания заметок
4. **Поиск**: Используйте строку поиска для нахождения заметок по заголовку или содержимому
5. **Редактирование**: Нажмите на иконку редактирования для изменения заметки
6. **Удаление**: Нажмите на иконку корзины для удаления заметки

## API Документация

### Auth Service (http://localhost:8082)

#### Аутентификация
- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Вход пользователя
- `POST /api/auth/refresh-token` - Обновление токена
- `POST /api/auth/forgot-password` - Запрос сброса пароля
- `POST /api/auth/reset-password` - Сброс пароля

#### Пользователи
- `GET /api/user/{userId}` - Получить пользователя по ID
- `GET /api/user` - Получить текущего пользователя
- `POST /api/user/update-avatar` - Обновить аватар
- `GET /api/user/{userId}/avatar` - Получить аватар пользователя

### Notes Service (http://localhost:8081)

- `GET /api/notes` - Получить все заметки пользователя
- `POST /api/notes` - Создать новую заметку
- `GET /api/notes/{id}` - Получить заметку по ID
- `PUT /api/notes/{id}` - Обновить заметку
- `DELETE /api/notes/{id}` - Удалить заметку
- `GET /api/notes/search?query={query}` - Поиск заметок

## Разработка

### Запуск в режиме разработки

1. **Backend сервисы**:
```bash
# Запустите базы данных и вспомогательные сервисы
docker-compose up -d db1 db2 mailhog minio

# Запустите auth-service
cd auth-service
mvn spring-boot:run

# Запустите notes-service
cd notes-service
mvn spring-boot:run
```

2. **Frontend**:
```bash
cd frontend
npm install
npm start
```

### Переменные окружения

Создайте файлы `.env` в каждом сервисе:

**auth-service/.env**:
```
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_user
AUTH_DB_PASSWORD=auth_password
```

**notes-service/.env**:
```
NOTES_DB_NAME=notes_db
NOTES_DB_USER=notes_user
NOTES_DB_PASSWORD=notes_password
```

## Структура проекта

```
├── auth-service/          # Сервис аутентификации
├── notes-service/         # Сервис заметок
├── frontend/              # React фронтенд
├── docker-compose.yml     # Docker Compose конфигурация
└── README.md              # Этот файл
```

## Безопасность

- JWT токены для аутентификации
- CORS настроен для безопасного взаимодействия
- Валидация данных на фронтенде и бэкенде
- Безопасные HTTP заголовки
- Хеширование паролей

## Лицензия

MIT License