const BaseModel = require('./BaseModel');
const Track = require('./Track');
const TrackArtist = require('./TrackArtist');

class Artist extends BaseModel {
  static get tableName() {
    return 'artists';
  }

  static get idColumn() {
    return 'id';
  }

  static get jsonSchema() {
    return {
      type: 'object',
      required: ['name'],
      properties: {
        id: { type: 'integer' },
        name: { type: 'string', maxLength: 255 },
        normalized_name: { type: 'string', maxLength: 255 },
        created_at: { type: 'string', format: 'date-time' }
      }
    };
  }

  static get relationMappings() {
    return {
      tracks: {
        relation: BaseModel.ManyToManyRelation,
        modelClass: Track,
        join: {
          from: 'artists.id',
          through: {
            modelClass: TrackArtist,
            from: 'track_artists.artist_id',
            to: 'track_artists.track_id'
          },
          to: 'tracks.id'
        }
      }
    };
  }
}

module.exports = Artist;