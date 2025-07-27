#!/bin/bash

echo "🚀 Запуск Notes Application..."

# Проверяем наличие Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Пожалуйста, установите Docker."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен. Пожалуйста, установите Docker Compose."
    exit 1
fi

# Создаем необходимые env файлы если их нет
if [ ! -f "auth-service/.env" ]; then
    echo "📝 Создание auth-service/.env..."
    cat > auth-service/.env << EOF
AUTH_DB_NAME=auth_db
AUTH_DB_USER=auth_user
AUTH_DB_PASSWORD=auth_password
JWT_SECRET=mySecretKey123456789
MAIL_HOST=mailhog
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MINIO_URL=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
EOF
fi

if [ ! -f "notes-service/.env" ]; then
    echo "📝 Создание notes-service/.env..."
    cat > notes-service/.env << EOF
NOTES_DB_NAME=notes_db
NOTES_DB_USER=notes_user
NOTES_DB_PASSWORD=notes_password
JWT_SECRET=mySecretKey123456789
AUTH_SERVICE_URL=http://auth-service:8082
EOF
fi

echo "🔧 Сборка и запуск сервисов..."
docker-compose up -d

echo "⏳ Ожидание запуска сервисов..."
sleep 10

echo "✅ Приложение запущено!"
echo ""
echo "🌐 Доступные сервисы:"
echo "   Frontend:    http://localhost:3000"
echo "   Auth API:    http://localhost:8082"
echo "   Notes API:   http://localhost:8081"
echo "   MailHog:     http://localhost:8025"
echo "   MinIO:       http://localhost:9001"
echo ""
echo "📖 Для остановки запустите: docker-compose down"