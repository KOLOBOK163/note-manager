# Notes App Frontend

Современный фронтенд для приложения заметок, построенный на React с TypeScript.

## Функциональность

- **Аутентификация пользователей**: Регистрация, вход и выход
- **Управление заметками**: Создание, редактирование, удаление и поиск заметок
- **Современный UI**: Красивый и отзывчивый интерфейс с использованием Tailwind CSS
- **Безопасность**: JWT токены с автоматическим обновлением
- **Уведомления**: Toast уведомления для пользователя

## Технологии

- React 18 с TypeScript
- React Router для навигации
- React Hook Form для форм
- Yup для валидации
- Axios для HTTP запросов
- Tailwind CSS для стилизации
- Lucide React для иконок
- React Hot Toast для уведомлений

## Запуск проекта

1. Установите зависимости:
```bash
npm install
```

2. Создайте файл .env с переменными окружения (уже создан):
```
REACT_APP_AUTH_API_URL=http://localhost:8082/api
REACT_APP_NOTES_API_URL=http://localhost:8081/api
```

3. Запустите проект:
```bash
npm start
```

4. Откройте [http://localhost:3000](http://localhost:3000) в браузере

## Структура проекта

```
src/
├── components/          # React компоненты
│   ├── Dashboard.tsx    # Главная страница с заметками
│   ├── Login.tsx        # Форма входа
│   ├── Register.tsx     # Форма регистрации
│   ├── NoteModal.tsx    # Модальное окно для заметок
│   └── ProtectedRoute.tsx # Защищенные маршруты
├── contexts/            # React контексты
│   └── AuthContext.tsx  # Контекст аутентификации
├── services/            # API сервисы
│   └── api.ts          # HTTP клиент и API методы
├── types/              # TypeScript типы
│   └── index.ts        # Определения типов
└── App.tsx             # Главный компонент
```

## API Endpoints

### Аутентификация (порт 8082)
- POST `/api/auth/register` - Регистрация
- POST `/api/auth/login` - Вход
- POST `/api/auth/refresh-token` - Обновление токена
- POST `/api/auth/forgot-password` - Забыли пароль
- POST `/api/auth/reset-password` - Сброс пароля

### Заметки (порт 8081)
- GET `/api/notes` - Получить все заметки
- POST `/api/notes` - Создать заметку
- PUT `/api/notes/{id}` - Обновить заметку
- DELETE `/api/notes/{id}` - Удалить заметку
- GET `/api/notes/{id}` - Получить заметку по ID
- GET `/api/notes/search?query=` - Поиск заметок

## Доступные скрипты

- `npm start` - Запуск в режиме разработки
- `npm run build` - Сборка для продакшена
- `npm test` - Запуск тестов
- `npm run eject` - Извлечение конфигурации (необратимо)
