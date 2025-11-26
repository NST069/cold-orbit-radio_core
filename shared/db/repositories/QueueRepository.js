const TrackQueue = require('../models/TrackQueue');
const Track = require('../models/Track');
const { db } = require('../knex-config');

class QueueRepository {
    /**
     * Получает количество треков в очереди
     */
    async getActiveCount() {
        const result = await TrackQueue.query()
            .whereIn('status', ['pending', 'scheduled', 'playing'])
            .resultSize();

        return result;
    }

    /**
     * Добавляет трек в очередь
     */
    async addToQueue(queueData) {
        return await TrackQueue.query()
            .insert(queueData)
            .onConflict('track_id')
            .ignore();
    }

    /**
     * Получает следующий трек
     */
    async getNextTrack() {
        return await db.transaction(async trx => {
            // Берем только file_name и id
            const nextTrack = await trx('track_queue')
                .where('status', 'pending')
                .select('id', 'file_name')
                .orderBy('created_at', 'asc')
                .forUpdate()
                .skipLocked()
                .first();

            if (!nextTrack) {
                return null;
            }

            return nextTrack; // { id, file_name }
        });
    }

    /**
     * Обновляет статус трека в очереди
     */
    async updateQueueStatus(fileName, status, additionalData = {}) {
        const updateData = { status, ...additionalData };

        if (status === 'scheduled') {
            updateData.scheduled_at = new Date().toISOString();
        } else if (status === 'played') {
            updateData.played_at = new Date().toISOString();
        }

        return await TrackQueue.query()
            .patch(updateData)
            .where('file_name', fileName);
    }

    /**
     * Получает текущую очередь
     */
    async getQueue() {
        return await TrackQueue.query()
            .withGraphFetched('track.artists')
            .orderBy('created_at', 'asc');
    }

    /**
     * Проверяет, находится ли трек в активной очереди
     */
    async isTrackInQueue(trackId) {
        const result = await TrackQueue.query()
            .where('track_id', trackId)
            .whereIn('status', ['pending', 'scheduled', 'playing'])
            .first();

        return !!result;
    }

    /**
     * Получает список всех файлов в активной очереди
     */
    async getActiveQueueFiles() {
        const result = await TrackQueue.query()
            .whereIn('status', ['pending', 'scheduled', 'playing'])
            .select('file_name', 'cover_file_name');
        return {
            audioFiles: result.map(item => item.file_name),
            coverFiles: result.map(item => item.cover_file_name).filter(Boolean)
        };
    }

    /**
     * Удаляет проигранные и битые треки из очереди
     */
    async cleanupTrackQueue() {
        return await TrackQueue.query()
            .whereIn('status', ['played', 'failed'])
            .delete();
    }
}

module.exports = new QueueRepository();