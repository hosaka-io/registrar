-- db/sql/schemas.sql

-- :name get-schemas-for-type-sql :? :*
SELECT ver, schema_data
FROM registrar.schemas
WHERE depreciated_on IS NULL AND entity_type = :entity_type
ORDER BY created_on DESC
