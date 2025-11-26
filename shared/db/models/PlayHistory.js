const BaseModel = require('./BaseModel');

class PlayHistory extends BaseModel {
  static get tableName() {
    return 'play_history';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['track_id'],
      properties: {
        id: { type: 'integer' },
        track_id: { type: 'integer' },
        played_at: { type: 'string', format: 'date-time' },
        avg_listeners: { type: 'integer', default: 0 }
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
          from: 'play_history.track_id',
          to: 'tracks.id'
        }
      }
    };
  }
}

module.exports = PlayHistory;