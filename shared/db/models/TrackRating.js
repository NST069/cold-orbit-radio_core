const BaseModel = require('./BaseModel');

class TrackRating extends BaseModel {
  static get tableName() {
    return 'track_ratings';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['track_id', 'rating'],
      properties: {
        id: { type: 'integer' },
        track_id: { type: 'integer' },
        rating: { type: 'integer', enum: [1, -1] },
        user_identifier: { type: 'string', maxLength: 255 },
        created_at: { type: 'string', format: 'date-time' },
        updated_at: { type: 'string', format: 'date-time' }
      }
    };
  }

  static get relationMappings() {
    const Track = require('./Track');
    
    return {
      track: {
        relation: Model.BelongsToOneRelation,
        modelClass: Track,
        join: {
          from: 'track_ratings.track_id',
          to: 'tracks.id'
        }
      }
    };
  }
}

module.exports = TrackRating;