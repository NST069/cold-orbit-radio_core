const BaseModel = require('./BaseModel');
const Track = require('./Track');

class TrackQueue extends BaseModel {
  static get tableName() {
    return 'track_queue';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['track_id', 'file_name'],
      properties: {
        id: { type: 'integer' },
        track_id: { type: 'integer' },
        file_name: { type: 'string', maxLength: 500 },
        cover_file_name: { type: ['string', 'null'], maxLength: 500 },
        status: { 
          type: 'string', 
          enum: ['pending', 'scheduled', 'playing', 'played', 'failed'],
          default: 'pending'
        },
        priority: { type: 'integer', minimum: 1, maximum: 10, default: 5 },
        created_at: { type: 'string', format: 'date-time' },
        updated_at: { type: 'string', format: 'date-time' },
        scheduled_at: { type: ['string', 'null'], format: 'date-time' },
        played_at: { type: ['string', 'null'], format: 'date-time' }
      }
    };
  }

  static get relationMappings() {
    return {
      track: {
        relation: BaseModel.BelongsToOneRelation,
        modelClass: Track,
        join: {
          from: 'track_queue.track_id',
          to: 'tracks.id'
        }
      }
    };
  }
}

module.exports = TrackQueue;