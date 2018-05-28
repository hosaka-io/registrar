-- db/sql/health.sql

-- :name get-db-health-sql :? :1
SELECT current_setting('server_version_num') as pg_version, now() as time, installed_rank,
version, description, type, script, checksum, installed_by, installed_on,
execution_time, success
FROM registrar.flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 1
