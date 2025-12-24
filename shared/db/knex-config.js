const knex = require('knex');

const config = {
  client: 'pg',
  connection: {
    host: process.env.DB_HOST || 'postgres',
    port: process.env.DB_PORT || 5432,
    user: process.env.DB_USER || 'radio',
    password: process.env.DB_PASSWORD || 'radio_pass',
    database: process.env.DB_NAME || 'coldorbit'
  },
  pool: {
    min: 2,
    max: 10,
    acquireTimeoutMillis: 30000,
    createTimeoutMillis: 30000,
    destroyTimeoutMillis: 5000,
    idleTimeoutMillis: 30000,
    reapIntervalMillis: 1000,
    createRetryIntervalMillis: 100
  },
  migrations: {
    tableName: 'knex_migrations'
  }
};

const db = knex(config);

db.raw('SELECT 1')
  .then(() => console.log('Database connected successfully'))
  .catch(err => {
    console.error('Database connection failed:', err);
    process.exit(1);
  });

module.exports = { db, config };