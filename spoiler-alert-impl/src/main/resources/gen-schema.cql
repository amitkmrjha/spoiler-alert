// Syndeia Cloud Store Keyspace

CREATE KEYSPACE IF NOT EXISTS spoiler_alert WITH durable_writes = true AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE IF NOT EXISTS spoiler_alert.spoileralerts (
id timeuuid,
key text,
user_name text,
series_name text,
percentage double,
PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS spoiler_alert.spoileralerts_by_key (
id timeuuid,
key text,
user_name text,
series_name text,
percentage double,
PRIMARY KEY (key)
);

CREATE TABLE IF NOT EXISTS spoiler_alert.spoileralerts_by_users_series (
id timeuuid,
key text,
user_name text,
series_name text,
percentage double,
PRIMARY KEY (user_name,series_name)
);

CREATE TABLE IF NOT EXISTS spoiler_alert.spoileralerts_by_series_percentage (
id timeuuid,
key text,
user_name text,
series_name text,
percentage double,
PRIMARY KEY (series_name,percentage,user_name)
);

CREATE TABLE IF NOT EXISTS spoiler_alert.auto_key (
model_context text,
model_name text,
key_number counter,
PRIMARY KEY (model_context, model_name)
) WITH CLUSTERING ORDER BY ( model_name ASC );

