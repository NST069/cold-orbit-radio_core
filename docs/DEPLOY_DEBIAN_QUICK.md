# Быстрая инструкция по развёртыванию (Debian)

Короткая, рабочая инструкция чтобы быстро поднять `cold-orbit-radio_core` на Debian (подходит для Debian 13). Предполагается, что вы переносите репозиторий на сервер и запускаете приложение как systemd-сервис под непользовательским юзером `coldorbit`.

Кратко:
- Установите зависимости: Node (18+ LTS), ffmpeg, liquidsoap, git
- Создайте системного пользователя `coldorbit` и директории `/opt/...` и `/var/lib/cold-orbit`
- Поместите код в `/opt/cold-orbit-radio_core` (или ваш путь) и установите npm-зависимости
- Создайте `/etc/cold-orbit.env` с секретами и переменными
- В systemd укажите `EnvironmentFile=/etc/cold-orbit.env` и `User=coldorbit`
- Обязательно использовать writable `LIQUIDSOAP_SCRIPT_DIR` (рекомендация: `/var/lib/cold-orbit/liquidsoap`)

Минимальные команды (запускать от root или с sudo):

```bash
apt update && apt upgrade -y
apt install -y git curl build-essential ffmpeg liquidsoap
# Node 18 (NodeSource)
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs

# Создать system user и директории
useradd -r -m -s /usr/sbin/nologin coldorbit
mkdir -p /opt/cold-orbit-radio_core
chown -R coldorbit:coldorbit /opt/cold-orbit-radio_core
mkdir -p /var/lib/cold-orbit/liquidsoap /var/log/cold-orbit
chown -R coldorbit:coldorbit /var/lib/cold-orbit /var/log/cold-orbit

# Клонировать и установить зависимости как coldorbit
cd /opt
# git clone ... (или rsync из артефактов)
chown -R coldorbit:coldorbit /opt/cold-orbit-radio_core
cd /opt/cold-orbit-radio_core
sudo -u coldorbit npm ci --production
```

Пример содержимого `/etc/cold-orbit.env` (не храните секреты с доступом для всех):

```ini
LIQUIDSOAP_PWD=your_liquidsoap_password
API_ID=123456
API_HASH=xxxxxxxxxxxxxxxxxxxx
PHN=+1234567890
CHANNEL=@your_channel
NODE_ENV=production
# рекомендую явно указать куда писать сгенерированные файлы
LIQUIDSOAP_SCRIPT_DIR=/var/lib/cold-orbit/liquidsoap
```

Минимальный `systemd` unit (`/etc/systemd/system/cold-orbit.service`):

```ini
[Unit]
Description=Cold Orbit Radio Core
After=network.target

[Service]
Type=simple
User=coldorbit
Group=coldorbit
WorkingDirectory=/opt/cold-orbit-radio_core
EnvironmentFile=/etc/cold-orbit.env
ExecStart=/usr/bin/node /opt/cold-orbit-radio_core/server.js
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Активировать сервис:

```bash
systemctl daemon-reload
systemctl enable --now cold-orbit.service
# посмотреть логи
journalctl -u cold-orbit -f
```

Ключевые примечания и проверка работоспособности

- LIQUIDSOAP_SCRIPT_DIR: по умолчанию код ранее мог писать в `/etc/liquidsoap`. Для сервиса без root-прав это даст ошибки. Явно укажите `LIQUIDSOAP_SCRIPT_DIR=/var/lib/cold-orbit/liquidsoap` и убедитесь, что `coldorbit` владеет этой директорией.
- Проверка синтаксиса Liquidsoap перед стартом (рекомендация):

```bash
# проверить сгенерированный файл
sudo -u coldorbit liquidsoap -c /var/lib/cold-orbit/liquidsoap/radio.liq
```

Это быстро обнаружит синтаксические ошибки или несовместимости версии Liquidsoap.

- Если вы увидите parse error, откройте `radio.liq` и проверьте подстановки токенов (PWD, host, port, mount). Частая проблема: некорректная замена или лишние аргументы в `null.get(...)` с разными версиями Liquidsoap.

- Telnet порт: Node подключается к telnet-порту Liquidsoap (по умолчанию 1234). Обычно это локальный порт — не открывайте его в сеть. Если Node пишет `ECONNREFUSED`, проверьте что Liquidsoap запущен и слушает: `ss -ltnp | grep 1234`.

- Права и владельцы: убедитесь, что `coldorbit` владеет `/var/lib/cold-orbit` и поддиректориями. Liquidsoap и Node должны иметь право писать `radio.liq`, `trackQueue.json` и лог-файл, если они там создаются.

Быстрые проверки после старта

- Служба Node запущена: `systemctl status cold-orbit`
- Логи в реальном времени: `journalctl -u cold-orbit -f`
- Сгенерированный `radio.liq` в `LIQUIDSOAP_SCRIPT_DIR` и `liquidsoap.log` рядом (или в `/var/log/cold-orbit`) — проверьте их на ошибки.
- Если работа с Telegram/tdlib: убедитесь, что npm-зависимости установлены как `coldorbit` и что бинарные модули (prebuilt-tdlib) корректно загрузились.

Короткие рекомендации для продакшна

- Храните runtime-файлы вне git-репозитория — используйте `/var/lib/cold-orbit`.
- Добавьте `EnvironmentFile` в systemd и держите секреты в `/etc/cold-orbit.env` с правами `640`.
- Добавьте простую проверку в `streamManager.js` (или systemd `ExecStartPre`) — `liquidsoap -c /path/to/radio.liq` чтобы неприятности проявлялись до запуска.
- Добавьте `logrotate` для больших логов, если не полагаетесь только на journald.

