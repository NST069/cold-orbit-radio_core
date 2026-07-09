#!/bin/bash

if [ -z "$1" ]; then
  echo "Ошибка: не указан путь к скрипту .liq" >&2
  exit 1
fi

SCRIPT_PATH="$1"
CONFIG_DIR=$(dirname "$SCRIPT_PATH")
CURRENT_SCRIPT="$CONFIG_DIR/radio.liq"
NEW_SCRIPT="$CONFIG_DIR/radio_new.liq"
CONTAINER="cor-liquidsoap"

# 1. Проверка на наличие текущего скрипта
if [ ! -f "$CURRENT_SCRIPT" ]; then
  echo "Текущий скрипт $CURRENT_SCRIPT не найден. Выполняем первичную установку..."
  
  # Копируем новый скрипт на место текущего
  cp "$SCRIPT_PATH" "$CURRENT_SCRIPT"
  
  # Жёсткий перезапуск (т.к. это первая установка)
  docker restart "$CONTAINER"
  echo "Выполнен жёсткий перезапуск (docker restart) — первичная установка"
  exit 0
fi

# 2. Анализ изменений
FORMAT_CHANGED=$(diff <(grep -Eo 'format="[^"]+"' "$CURRENT_SCRIPT") <(grep -Eo 'format="[^"]+"' "$NEW_SCRIPT"))
QUEUE_ID_CHANGED=$(diff <(grep -o 'request\.queue(id="[^"]+"' "$CURRENT_SCRIPT") <(grep -o 'request\.queue(id="[^"]+"' "$NEW_SCRIPT"))
CRITICAL_CHANGES=$(diff <(grep -E 'server\.register|add_protocol|set\("server\.' "$CURRENT_SCRIPT") <(grep -E 'server\.register|add_protocol|set\("server\.' "$NEW_SCRIPT"))

#if [ -z "$FORMAT_CHANGED" ] && [ -z "$QUEUE_ID_CHANGED" ] && [ -z "$CRITICAL_CHANGES" ]; then
#  # Мягкий перезапуск
#  cp "$NEW_SCRIPT" "$CURRENT_SCRIPT"
#
#  if docker kill -s SIGHUP "$CONTAINER"; then
#    echo "SIGHUP отправлен, проверяем процесс через 3 секунды..."
#    sleep 3
#
#    CONTAINER_RUNNING=$(docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null)
#
#    if [ "$CONTAINER_RUNNING" = "true" ]; then
#      echo "Процесс жив после SIGHUP. Предполагаем, что конфигурация перезагружена."
#    else
#      echo "Не удалось отправить SIGHUP (возможно, процесс уже завершился)."
#      echo "Выполняем restart контейнера..."
#      docker restart "$CONTAINER"
#    fi
#
#  else
#    echo "Не удалось отправить SIGHUP. Выполняем restart..."
#    docker restart "$CONTAINER"
#  fi
#else
  # Жёсткий перезапуск
  echo "Изменения требуют жёсткого перезапуска. Причины:"
  [ -n "$FORMAT_CHANGED" ] && echo "  - Изменён формат вывода"
  [ -n "$QUEUE_ID_CHANGED" ] && echo "  - Изменён ID очереди запросов"
  [ -n "$CRITICAL_CHANGES" ] && echo "  - Критические изменения конфигурации"
  
  docker restart "$CONTAINER"
  echo "Выполнен жёсткий перезапуск (docker restart)"
#fi

exit 0
