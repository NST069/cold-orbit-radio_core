const Track = require("../models/Track");
const TrackQueue = require('../models/TrackQueue');
const Artist = require('../models/Artist');
const { db } = require('../knex-config');

class TrackRepository {
    /**
     * Создает трек и связывает артистов
     */
    async createTrackWithArtists(trackData) {
        const { performerString, ...trackFields } = trackData;

        return await db.transaction(async trx => {
            // Создаем или находим трек
            let track = await Track.query(trx)
                .findOne({ telegram_id: trackFields.telegram_id });

            if (!track) {
                track = await Track.query(trx)
                    .insert(trackFields)
                    .returning('*');
            }

            // Парсим и связываем артистов через DB функцию
            if (performerString) {
                await trx.raw('SELECT parse_and_link_artists(?, ?)', [track.id, performerString]);
            }

            return track;
        });
    }

    /**
     * Добавляет трек в очередь
     */
    async addToQueue(trackId, fileName, coverFileName = null) {
        return await TrackQueue.query()
            .insert({
                track_id: trackId,
                file_name: fileName,
                cover_file_name: coverFileName,
                status: 'pending'
            })
            .onConflict('track_id')
            .ignore();
    }

    /**
     * Находит трек по имени файла с артистами
     */
    async findTrackByFileName(fileName) {
        return await Track.query()
            .findOne({ file_name: fileName })
            .withGraphFetched('artists');
    }

    /**
     * Получает статистику по трекам
     */
    async getTrackStats() {
        return await db('track_stats').select('*');
    }

    /**
     * Получает случайный трек, которого нет в активной очереди
     */
    async getRandomTrackExcludingQueue() {
        return await db.raw(`
      SELECT t.* 
      FROM tracks t
      WHERE t.id NOT IN (
        SELECT track_id 
        FROM track_queue 
        WHERE status IN ('pending', 'scheduled', 'playing')
      )
      ORDER BY RANDOM()
      LIMIT 1
    `).then(result => result.rows[0]);
    }

    /**
     * Находит трек по telegram_file_id
     */
    async findByFileId(telegramFileId) {
        return await Track.query()
            .findOne({ telegram_file_id: telegramFileId })
            .withGraphFetched('artists');
    }

    /**
     * Находит трек по нормализованным title, performer, возвращает Id
     */
    async findIdByTitleAndPerformer(performer, title) {
        return await Track.query()
            .findOne({ title_fixed: title, performer_fixed: performer })
            .select('id');
    }

    /**
     * Получает общее количество треков в БД
     */
    async getTotalCount() {
        const result = await db('tracks').count('* as count').first();
        return parseInt(result.count);
    }

    /**
     * Находит трек по ID
     */
    async findById(id) {
        return await Track.query()
            .findById(id)
            .withGraphFetched('artists');
    }

    /**
     * Находит трек по названию файла в очереди
     */
    async findByQueueFileName(queueFileName) {
        return await Track.query()
            .select('tracks.*')
            .whereExists(
                TrackQueue.query()
                    .select(1)
                    .whereColumn('track_queue.track_id', 'tracks.id')
                    .where('track_queue.file_name', queueFileName)
            )
            .withGraphFetched('artists')
            .first();
    }
}

module.exports = new TrackRepository();
