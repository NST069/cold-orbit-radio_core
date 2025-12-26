#!/bin/bash

# Простой скрипт для генерации SSL сертификатов с certbot
set -e

echo "=== Генерация SSL сертификатов ==="
echo

# Проверяем, запущен ли скрипт с sudo
if [ "$EUID" -ne 0 ]; then 
  echo "Запустите скрипт с sudo: sudo ./generate-ssl.sh"
  exit 1
fi

# Проверяем наличие домена
if [ -z "$1" ]; then
  echo "Использование: sudo ./generate-ssl.sh ваш-домен.com [ваш-email@домен.com]"
  echo "Пример: sudo ./generate-ssl.sh radio.example.com admin@example.com"
  exit 1
fi

DOMAIN="$1"
EMAIL="${2:-admin@$DOMAIN}"

echo "Домен: $DOMAIN"
echo "Email: $EMAIL"
echo

# 1. Проверяем, что домен указывает на сервер
echo "1. Проверка DNS записи для $DOMAIN..."
IP=$(curl -s ifconfig.me)
DNS_IP=$(dig +short $DOMAIN)

if [ "$DNS_IP" != "$IP" ]; then
  echo "ВНИМАНИЕ: Домен $DOMAIN указывает на $DNS_IP, а ваш сервер имеет IP $IP"
  echo "Убедитесь, что A-запись для $DOMAIN настроена на IP $IP"
  read -p "Продолжить? (y/N): " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
  fi
else
  echo "✓ DNS запись настроена правильно"
fi

# 2. Останавливаем nginx если запущен
echo "2. Остановка nginx..."
systemctl stop nginx 2>/dev/null || docker stop cor_nginx 2>/dev/null || true

# 3. Устанавливаем certbot если нет
echo "3. Установка certbot..."
if ! command -v certbot &> /dev/null; then
  apt-get update
  apt-get install -y certbot
fi

# 4. Создаем папку для сертификатов
echo "4. Создание структуры папок..."
mkdir -p nginx/ssl

# 5. Получаем сертификат
echo "5. Получение SSL сертификата от Let's Encrypt..."
echo "Это может занять несколько секунд..."

if certbot certonly --standalone -d "$DOMAIN" --non-interactive --agree-tos -m "$EMAIL"; then
  echo "✓ Сертификат успешно получен!"
else
  echo "ОШИБКА: Не удалось получить сертификат"
  echo "Возможные причины:"
  echo " 1. Домен не настроен на IP этого сервера"
  echo " 2. Порт 80 занят другим процессом"
  echo " 3. Проблемы с сетью"
  exit 1
fi

# 6. Копируем сертификаты в папку проекта
echo "6. Копирование сертификатов..."
cp "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" "nginx/ssl/certificate.crt"
cp "/etc/letsencrypt/live/$DOMAIN/privkey.pem" "nginx/ssl/private.key"

# 7. Генерируем DH параметры (опционально, можно пропустить)
echo "7. Генерация DH параметров (может занять минуту)..."
openssl dhparam -out nginx/ssl/dhparam.pem 2048 2>/dev/null || true

# 8. Создаем простой конфиг nginx
echo "8. Создание конфигурации nginx..."

cat > nginx/nginx.conf << EOF
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    sendfile on;
    keepalive_timeout 65;
    
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log warn;
    
    # HTTP -> HTTPS редирект
    server {
        listen 80;
        listen [::]:80;
        server_name $DOMAIN;
        
        location / {
            return 301 https://\$host\$request_uri;
        }
    }
    
    # HTTPS сервер
    server {
        listen 443 ssl;
        listen [::]:443 ssl;
        server_name $DOMAIN;
        
        # SSL сертификаты
        ssl_certificate /etc/nginx/ssl/certificate.crt;
        ssl_certificate_key /etc/nginx/ssl/private.key;
        
        # Безопасные настройки
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
        ssl_prefer_server_ciphers off;
        
        # Прокси на gateway
        location / {
            proxy_pass http://gateway:3000;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto \$scheme;
        }
        
        # Прокси на icecast
        location ~ ^/(radio\.mp3|stream|listen) {
            proxy_pass http://icecast:8000;
            proxy_set_header Host \$host;
            proxy_buffering off;
            
            # Для аудиопотоков
            proxy_read_timeout 3600s;
            
            # CORS
            add_header Access-Control-Allow-Origin *;
            add_header Access-Control-Allow-Methods 'GET, OPTIONS';
        }
        
        # Статус Icecast
        location /status.xsl {
            proxy_pass http://icecast:8000/status.xsl;
        }
    }
}
EOF

# 9. Настраиваем автообновление сертификатов
echo "9. Настройка автообновления сертификатов..."

# Создаем скрипт для обновления
cat > /usr/local/bin/update-ssl << 'EOF'
#!/bin/bash
certbot renew --quiet --post-hook "docker restart cor_nginx 2>/dev/null || systemctl reload nginx"
EOF
chmod +x /usr/local/bin/update-ssl

# Добавляем в cron
(crontab -l 2>/dev/null; echo "0 3 * * * /usr/local/bin/update-ssl") | crontab -

# 10. Создаем readme файл
cat > nginx/ssl/README.md << EOF
# SSL Сертификаты для Cold Orbit Radio
Сгенерировано: $(date)

Домен: $DOMAIN
Email: $EMAIL

Файлы:
- certificate.crt - публичный сертификат
- private.key - приватный ключ
- dhparam.pem - параметры Диффи-Хеллмана

Сертификат будет автоматически обновляться.
EOF

echo "=== ГОТОВО! ==="
echo
echo "Что было сделано:"
echo "✓ SSL сертификат получен от Let's Encrypt"
echo "✓ Сертификаты сохранены в nginx/ssl/"
echo "✓ Конфигурация nginx создана"
echo "✓ Автообновление настроено (раз в 3 месяца)"
echo
echo "Теперь измените docker-compose.yml:"
echo "1. Уберите порты 3000:3000 из gateway"
echo "2. Уберите порты 8000:8000 из icecast"
echo "3. В nginx секции volumes добавьте:"
echo "   - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro"
echo "   - ./nginx/ssl:/etc/nginx/ssl:ro"
echo
echo "Запустите: docker-compose up -d"
echo
echo "Проверьте SSL:"
echo "curl -I https://$DOMAIN"
echo "curl -I https://$DOMAIN/radio.mp3"
echo
echo "Дополнительно:"
echo "Для проверки автообновления: certbot renew --dry-run"
echo "Для принудительного обновления: certbot renew --force-renewal"