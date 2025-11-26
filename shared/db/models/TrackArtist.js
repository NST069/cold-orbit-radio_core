const BaseModel = require('./BaseModel');

class TrackArtist extends BaseModel {
  static get tableName() {
    return 'track_artists';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['track_id', 'artist_id'],
      properties: {
        id: { type: 'integer' },
        track_id: { type: 'integer' },
        artist_id: { type: 'integer' },
        artist_role: { 
          type: 'string', 
          enum: ['main', 'featured', 'remixer', 'producer'],
          default: 'main'
        },
        artist_order: { type: 'integer', default: 0 },
        created_at: { type: 'string', format: 'date-time' }
      }
    };
  }

  static get relationMappings() {
    const Track = require('./Track');
    const Artist = require('./Artist');
    
    return {
      track: {
        relation: Model.BelongsToOneRelation,
        modelClass: Track,
        join: {
          from: 'track_artists.track_id',
          to: 'tracks.id'
        }
      },
      artist: {
        relation: Model.BelongsToOneRelation,
        modelClass: Artist,
        join: {
          from: 'track_artists.artist_id',
          to: 'artists.id'
        }
      }
    };
  }
}

module.exports = TrackArtist;