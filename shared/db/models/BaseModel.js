const { Model } = require('objection');
const { db } = require('../knex-config');

Model.knex(db);

class BaseModel extends Model {
  $beforeInsert() {
    this.created_at = new Date().toISOString();
    this.updated_at = new Date().toISOString();
  }

  $beforeUpdate() {
    this.updated_at = new Date().toISOString();
  }
}

module.exports = BaseModel;