#!/bin/bash

if [ -z "$1" ]; then
  echo "Ошибка: не указан путь к скрипту .liq" >&2
  exit 1
fi

SCRIPT_PATH="$1"
CONFIG_DIR=$(dirname "$SCRIPT_PATH")
CURRENT_SCRIPT="$CONFIG_DIR/radio.liq"
NEW_SCRIPT="$CONFIG_DIR/radio_new.liq"
CONTAINER="cor_liquidsoap"

# 1. Валидация
if ! liquidsoap --check "$NEW_SCRIPT"; then
  echo "Ошибка синтаксиса в новом скрипте:"
  liquidsoap --check "$NEW_SCRIPT" 2>&1
  exit 1
fi

# 2. Проверка на наличие текущего скрипта
if [ ! -f "$CURRENT_SCRIPT" ]; then
  echo "Текущий скрипт $CURRENT_SCRIPT не найден. Выполняем первичную установку..."
  
  # Копируем новый скрипт на место текущего
  cp "$SCRIPT_PATH" "$CURRENT_SCRIPT"
  
  # Жёсткий перезапуск (т.к. это первая установка)
  docker restart "$CONTAINER"
  echo "Выполнен жёсткий перезапуск (docker restart) — первичная установка"
  exit 0
fi

# 3. Анализ изменений
FORMAT_CHANGED=$(diff <(grep -Eo 'format="[^"]+"' "$CURRENT_SCRIPT") <(grep -Eo 'format="[^"]+"' "$NEW_SCRIPT"))
QUEUE_ID_CHANGED=$(diff <(grep -o 'request\.queue(id="[^"]+"' "$CURRENT_SCRIPT") <(grep -o 'request\.queue(id="[^"]+"' "$NEW_SCRIPT"))
CRITICAL_CHANGES=$(diff <(grep -E 'server\.register|add_protocol|set\("server\.' "$CURRENT_SCRIPT") <(grep -E 'server\.register|add_protocol|set\("server\.' "$NEW_SCRIPT"))

if [ -z "$FORMAT_CHANGED" ] && [ -z "$QUEUE_ID_CHANGED" ] && [ -z "$CRITICAL_CHANGES" ]; then
  # Мягкий перезапуск
  cp "$NEW_SCRIPT" "$CURRENT_SCRIPT"

  LIQUIDSOAP_PID=$(docker exec "$CONTAINER" ps aux | grep -E "liquidsoap.*radio" | grep -v grep | awk '{print $2}' | head -1)

  if [ -z "$LIQUIDSOAP_PID" ] || [ "$LIQUIDSOAP_PID" = "1" ]; then
    LIQUIDSOAP_PID=$(docker exec "$CONTAINER" sh -c 'ps -eo ppid,pid,cmd | awk "\$1 == 1 && /liquidsoap/ {print \$2}" | head -1')
  fi

  echo "$LIQUIDSOAP_PID"

  # docker kill -s SIGHUP "$CONTAINER"
  if [ -n "$LIQUIDSOAP_PID" ] && [ "$LIQUIDSOAP_PID" != "1" ]; then
    if docker exec "$CONTAINER" kill -s SIGHUP "$LIQUIDSOAP_PID"; then
      echo "SIGHUP отправлен, проверяем процесс через 2 секунды..."
      sleep 2
      
      # Проверяем, жив ли процесс
      if docker exec "$CONTAINER" ps -p "$LIQUIDSOAP_PID" >/dev/null 2>&1; then
        echo "Процесс жив после SIGHUP. Предполагаем, что конфигурация перезагружена."
      fi
    else
      echo "Не удалось отправить SIGHUP (возможно, процесс уже завершился)."
      echo "Выполняем restart контейнера..."
      docker restart "$CONTAINER"
    fi
  fi
else
  # Жёсткий перезапуск
  echo "Изменения требуют жёсткого перезапуска. Причины:"
  [ -n "$FORMAT_CHANGED" ] && echo "  - Изменён формат вывода"
  [ -n "$QUEUE_ID_CHANGED" ] && echo "  - Изменён ID очереди запросов"
  [ -n "$CRITICAL_CHANGES" ] && echo "  - Критические изменения конфигурации"
  
  docker restart "$CONTAINER"
  echo "Выполнен жёсткий перезапуск (docker restart)"
fi

exit 0
