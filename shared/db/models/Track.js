const BaseModel = require('./BaseModel');
const Artist = require('./Artist');
const TrackArtist = require('./TrackArtist');
const TrackQueue = require('./TrackQueue');
const PlayHistory = require('./PlayHistory');
const TrackRating = require('./TrackRating');

class Track extends BaseModel {
  static get tableName() {
    return 'tracks';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['telegram_id', 'title', 'file_name', 'duration'],
      properties: {
        id: { type: 'integer' },
        telegram_id: { type: 'integer' },
        title: { type: 'string', maxLength: 500 },
        title_fixed: { type: 'string', maxLength: 500 },
        performer: { type: 'string', maxLength: 500 },
        performer_fixed: { type: 'string', maxLength: 500 },
        file_name: { type: 'string', maxLength: 500 },
        duration: { type: 'integer' },
        file_size: { type: 'integer' },
        telegram_file_id: { type: ['string', 'null'], maxLength: 500 },
        telegram_cover_id: { type: ['string', 'null'], maxLength: 500 },
        caption: { type: ['string', 'null'] },
        total_plays: { type: 'integer', default: 0 },
        like_count: { type: 'integer', default: 0 },
        dislike_count: { type: 'integer', default: 0 },
        created_at: { type: 'string', format: 'date-time' },
        updated_at: { type: 'string', format: 'date-time' }
      }
    };
  }

  static get relationMappings() {
    return {
      artists: {
        relation: BaseModel.ManyToManyRelation,
        modelClass: Artist,
        join: {
          from: 'tracks.id',
          through: {
            modelClass: TrackArtist,
            from: 'track_artists.track_id',
            to: 'track_artists.artist_id'
          },
          to: 'artists.id'
        }
      },

      queueEntries: {
        relation: BaseModel.HasManyRelation,
        modelClass: TrackQueue,
        join: {
          from: 'tracks.id',
          to: 'track_queue.track_id'
        }
      },

      playHistory: {
        relation: BaseModel.HasManyRelation,
        modelClass: PlayHistory,
        join: {
          from: 'tracks.id',
          to: 'play_history.track_id'
        }
      },

      ratings: {
        relation: BaseModel.HasManyRelation,
        modelClass: TrackRating,
        join: {
          from: 'tracks.id',
          to: 'track_ratings.track_id'
        }
      }
    };
  }
}

module.exports = Track;