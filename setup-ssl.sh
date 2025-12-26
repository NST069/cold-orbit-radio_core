#!/bin/bash

set -e  # Прерывать при ошибках

echo "=== Cold Orbit Radio - SSL Setup (Production) ==="
echo "Время: $(date)"
echo

# 1. Проверяем необходимые утилиты
echo "1. Проверка зависимостей..."
if ! command -v openssl &> /dev/null; then
    echo "Установка openssl..."
    apt-get update && apt-get install -y openssl
fi

# 2. Создаем структуру папок
echo "2. Создание структуры папок..."
mkdir -p nginx/ssl nginx/conf.d shared/{music,db,liquidsoap} shared/liquidsoap/logs

# 3. Генерируем сильные продакшен-сертификаты
echo "3. Генерация SSL сертификатов..."
openssl genrsa -out nginx/ssl/private.key 2048
openssl req -new -key nginx/ssl/private.key -out nginx/ssl/certificate.csr \
    -subj "/C=RU/ST=Moscow/L=Moscow/O=Cold Orbit Radio/CN=radio"
openssl x509 -req -days 3650 -in nginx/ssl/certificate.csr \
    -signkey nginx/ssl/private.key -out nginx/ssl/certificate.crt

# 4. Генерируем Diffie-Hellman параметры (усиливает безопасность)
echo "4. Генерация DH параметров (это займет ~1 минуту)..."
openssl dhparam -out nginx/ssl/dhparam.pem 2048

# 5. Создаем конфигурацию nginx
echo "5. Создание конфигурации nginx..."
cat > nginx/nginx.conf << 'EOF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # Основные настройки
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    client_max_body_size 50M;
    
    # Кэширование статики
    open_file_cache max=2000 inactive=20s;
    open_file_cache_valid 60s;
    open_file_cache_min_uses 2;
    open_file_cache_errors on;
    
    # Логирование
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    access_log /var/log/nginx/access.log main;
    
    # Настройки SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    ssl_dhparam /etc/nginx/ssl/dhparam.pem;
    
    # HTTP → HTTPS редирект
    server {
        listen 80 default_server;
        listen [::]:80 default_server;
        server_name _;
        
        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        
        # Redirect to HTTPS
        return 301 https://$host$request_uri;
    }
    
    # HTTPS сервер
    server {
        listen 443 ssl http2 default_server;
        listen [::]:443 ssl http2 default_server;
        server_name _;
        
        # SSL сертификаты
        ssl_certificate /etc/nginx/ssl/certificate.crt;
        ssl_certificate_key /etc/nginx/ssl/private.key;
        
        # Security headers
        add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        
        # Корневой location для gateway
        location / {
            proxy_pass http://gateway:3000;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;
            
            # Таймауты
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }
        
        # Аудиопоток Icecast
        location ~ ^/(radio\.mp3|stream|listen|live) {
            proxy_pass http://icecast:8000;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Отключаем буферизацию для потоков
            proxy_buffering off;
            proxy_cache off;
            proxy_read_timeout 3600s;
            
            # CORS для аудиоплееров
            add_header Access-Control-Allow-Origin "*" always;
            add_header Access-Control-Allow-Methods "GET, OPTIONS" always;
            add_header Access-Control-Allow-Headers "Range, Origin, Accept" always;
            add_header Access-Control-Expose-Headers "Content-Length, Content-Range" always;
            
            if ($request_method = 'OPTIONS') {
                add_header Access-Control-Allow-Origin "*";
                add_header Access-Control-Allow-Methods "GET, OPTIONS";
                add_header Access-Control-Allow-Headers "Range, Origin, Accept";
                add_header Access-Control-Max-Age 1728000;
                add_header Content-Type 'text/plain charset=UTF-8';
                add_header Content-Length 0;
                return 204;
            }
        }
        
        # Статус Icecast
        location /status.xsl {
            proxy_pass http://icecast:8000/status.xsl;
            proxy_set_header Host $host;
            
            # Кэшируем статус на 10 секунд
            expires 10s;
            add_header Cache-Control "public, must-revalidate";
        }
        
        # Статические файлы (если понадобятся)
        location /static/ {
            alias /var/www/static/;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # Health check для мониторинга
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
        
        # Запрещаем доступ к скрытым файлам
        location ~ /\. {
            deny all;
            access_log off;
            log_not_found off;
        }
    }
}
EOF

# 6. Создаем файл проверки SSL
echo "6. Создание тестового файла..."
cat > nginx/ssl/README.md << 'EOF'
# SSL Сертификаты для Cold Orbit Radio

Сгенерировано: $(date)

## Файлы:
- `certificate.crt` - публичный сертификат
- `private.key` - приватный ключ
- `certificate.csr` - запрос на сертификат
- `dhparam.pem` - Diffie-Hellman параметры

## Проверка:
openssl x509 -in certificate.crt -text -noout

## Для замены на Let's Encrypt (опционально):
1. Получите сертификаты: certbot certonly --standalone -d ваш-домен.com
2. Скопируйте:
   - cp /etc/letsencrypt/live/домен/fullchain.pem certificate.crt
   - cp /etc/letsencrypt/live/домен/privkey.pem private.key
3. Перезапустите nginx: docker-compose restart nginx
EOF

# 7. Проверяем сертификаты
echo "7. Проверка сгенерированных сертификатов..."
echo "=== Информация о сертификате ==="
openssl x509 -in nginx/ssl/certificate.crt -text -noout | grep -E "(Subject:|Not Before:|Not After :|Issuer:)"
echo

# 8. Выводим инструкцию
echo "=== НАСТРОЙКА SSL ЗАВЕРШЕНА! ==="
echo
echo "Сертификаты созданы:"
echo "  • nginx/ssl/certificate.crt"
echo "  • nginx/ssl/private.key"
echo "  • nginx/ssl/dhparam.pem"
echo
echo "Проверка:"
echo "  openssl s_client -connect localhost:443 -servername radio 2>/dev/null | openssl x509 -noout -dates"
echo
echo "Следующие шаги:"
echo "1. Запустите: docker-compose up -d"
echo "2. Откройте в браузере: https://ваш-сервер"
echo "3. Поток будет доступен по: https://ваш-сервер/radio.mp3"
echo
echo "Для добавления Let's Encrypt в будущем:"
echo "1. Настройте DNS запись A на ваш IP"
echo "2. Временно остановите nginx: docker-compose stop nginx"
echo "3. Установите certbot и получите сертификаты"
echo "4. Скопируйте их в nginx/ssl/"
echo "5. Запустите: docker-compose start nginx"
echo
echo "Готово! SSL настроен на 10 лет."
