BEGIN;
-- ENUM для ролей артистов
CREATE TYPE artist_role_enum AS ENUM ('main', 'featured', 'remixer', 'producer');
-- ENUM для статусов очереди
CREATE TYPE track_status_enum AS ENUM (
    'pending',
    'scheduled',
    'playing',
    'played',
    'failed'
);
-- Таблица артистов
CREATE TABLE artists (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    normalized_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
-- Основная таблица треков
CREATE TABLE tracks (
    id SERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE NOT NULL,
    -- msg.id из Telegram
    title VARCHAR(500) NOT NULL,
    performer VARCHAR(500) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    duration INTEGER NOT NULL,
    file_size BIGINT,
    telegram_file_id VARCHAR(500),
    telegram_cover_id VARCHAR(500),
    caption TEXT,
    total_plays INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
-- Связь треков и артистов
CREATE TABLE track_artists (
    id SERIAL PRIMARY KEY,
    track_id INTEGER REFERENCES tracks(id) ON DELETE CASCADE,
    artist_id INTEGER REFERENCES artists(id) ON DELETE CASCADE,
    artist_role artist_role_enum DEFAULT 'main',
    artist_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(track_id, artist_id, artist_role)
);
-- История проигрываний (ограниченный размер)
CREATE TABLE play_history (
    id SERIAL PRIMARY KEY,
    track_id INTEGER REFERENCES tracks(id),
    played_at TIMESTAMP DEFAULT NOW(),
    avg_listeners INTEGER DEFAULT 0
);
-- Рейтинги треков
CREATE TABLE track_ratings (
    id SERIAL PRIMARY KEY,
    track_id INTEGER REFERENCES tracks(id),
    rating INTEGER CHECK (rating IN (1, -1)),
    -- 1 like, -1 dislike
    user_identifier VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(track_id, user_identifier)
);
-- Очередь треков
CREATE TABLE track_queue (
    id SERIAL PRIMARY KEY,
    track_id INTEGER REFERENCES tracks(id) UNIQUE,
    file_name VARCHAR(500) NOT NULL,
    cover_file_name VARCHAR(500),
    status track_status_enum DEFAULT 'pending',
    priority INTEGER DEFAULT 5 CHECK (
        priority BETWEEN 1 AND 10
    ),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    scheduled_at TIMESTAMP NULL,
    played_at TIMESTAMP NULL
);
-- Индексы для производительности
CREATE INDEX idx_play_history_played_at ON play_history(played_at);
CREATE INDEX idx_play_history_track_id ON play_history(track_id);
CREATE INDEX idx_tracks_telegram_id ON tracks(telegram_id);
CREATE INDEX idx_tracks_created_at ON tracks(created_at);
-- Функция для нормализации имен артистов
CREATE OR REPLACE FUNCTION normalize_artist_name(raw_name TEXT) RETURNS TEXT AS $$ BEGIN RETURN lower(trim(raw_name));
END;
$$ LANGUAGE plpgsql;
-- Функция для парсинга артистов из строки
CREATE OR REPLACE FUNCTION parse_and_link_artists(
        p_track_id INTEGER,
        p_artist_string TEXT
    ) RETURNS void AS $$
DECLARE artist_names TEXT [];
artist_name TEXT;
artist_id INTEGER;
i INTEGER := 0;
main_artists TEXT [];
featured_artists TEXT [];
raw_string TEXT;
BEGIN raw_string := p_artist_string;
-- Определяем featured артистов (через "feat.", "ft.", "featuring")
IF raw_string ~* 'feat\.|ft\.|featuring' THEN -- Разделяем основного и приглашенного артиста
main_artists := string_to_array(split_part(raw_string, 'feat.', 1), '&');
featured_artists := string_to_array(split_part(raw_string, 'feat.', 2), '&');
-- Обрабатываем основных артистов
FOREACH artist_name IN ARRAY main_artists LOOP artist_name := trim(artist_name);
CONTINUE
WHEN artist_name = '';
INSERT INTO artists (name, normalized_name)
VALUES (artist_name, normalize_artist_name(artist_name)) ON CONFLICT (name) DO
UPDATE
SET name = EXCLUDED.name
RETURNING id INTO artist_id;
INSERT INTO track_artists (track_id, artist_id, artist_role, artist_order)
VALUES (p_track_id, artist_id, 'main', i);
i := i + 1;
END LOOP;
-- Обрабатываем приглашенных артистов
FOREACH artist_name IN ARRAY featured_artists LOOP artist_name := trim(artist_name);
CONTINUE
WHEN artist_name = '';
INSERT INTO artists (name, normalized_name)
VALUES (artist_name, normalize_artist_name(artist_name)) ON CONFLICT (name) DO
UPDATE
SET name = EXCLUDED.name
RETURNING id INTO artist_id;
INSERT INTO track_artists (track_id, artist_id, artist_role, artist_order)
VALUES (p_track_id, artist_id, 'featured', i);
i := i + 1;
END LOOP;
ELSE -- Стандартный парсинг для остальных случаев
artist_names := string_to_array(raw_string, '&');
IF array_length(artist_names, 1) = 1 THEN artist_names := string_to_array(raw_string, ',');
END IF;
FOREACH artist_name IN ARRAY artist_names LOOP artist_name := trim(artist_name);
CONTINUE
WHEN artist_name = '';
INSERT INTO artists (name, normalized_name)
VALUES (artist_name, normalize_artist_name(artist_name)) ON CONFLICT (name) DO
UPDATE
SET name = EXCLUDED.name
RETURNING id INTO artist_id;
INSERT INTO track_artists (track_id, artist_id, artist_role, artist_order)
VALUES (p_track_id, artist_id, 'main', i);
i := i + 1;
END LOOP;
END IF;
END;
$$ LANGUAGE plpgsql;
-- Триггер для обновления счетчиков при проигрывании
CREATE OR REPLACE FUNCTION update_track_counters() RETURNS TRIGGER AS $$ BEGIN
UPDATE tracks
SET total_plays = total_plays + 1,
    updated_at = NOW()
WHERE id = NEW.track_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER track_play_trigger
AFTER
INSERT ON play_history FOR EACH ROW EXECUTE FUNCTION update_track_counters();
-- Ограничение очереди (максимум 10 треков)
CREATE OR REPLACE FUNCTION check_queue_limit() RETURNS TRIGGER AS $$
DECLARE queue_count INTEGER;
BEGIN -- Считаем только треки в статусе pending
SELECT COUNT(*) INTO queue_count
FROM track_queue
WHERE status = 'pending';
-- Если добавляется новый pending трек, проверяем лимит
IF NEW.status = 'pending'
AND queue_count >= 10 THEN RAISE EXCEPTION 'Queue limit exceeded (max 10 pending tracks)';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER queue_limit_trigger BEFORE
INSERT
    OR
UPDATE ON track_queue FOR EACH ROW EXECUTE FUNCTION check_queue_limit();
-- Создание материализованного представления для статистики
CREATE MATERIALIZED VIEW track_stats AS
SELECT t.id,
    t.title,
    t.duration,
    t.total_plays,
    t.like_count,
    t.dislike_count,
    array_agg(DISTINCT a.name) as artists,
    COUNT(DISTINCT ph.id) as actual_play_count,
    COUNT(
        DISTINCT CASE
            WHEN tr.rating = 1 THEN tr.id
        END
    ) as actual_likes,
    COUNT(
        DISTINCT CASE
            WHEN tr.rating = -1 THEN tr.id
        END
    ) as actual_dislikes
FROM tracks t
    LEFT JOIN track_artists ta ON t.id = ta.track_id
    LEFT JOIN artists a ON ta.artist_id = a.id
    LEFT JOIN play_history ph ON t.id = ph.track_id
    LEFT JOIN track_ratings tr ON t.id = tr.track_id
GROUP BY t.id,
    t.title,
    t.duration,
    t.total_plays,
    t.like_count,
    t.dislike_count;
CREATE UNIQUE INDEX idx_track_stats_id ON track_stats(id);
COMMIT;