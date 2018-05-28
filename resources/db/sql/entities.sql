-- db/sql/entities.sql

-- :name upsert-entity-sql :! :n
INSERT INTO registrar.entities (id, entity_type, parent, created_by)
VALUES (CAST(:id AS UUID), :entity_type, CAST(:parent AS UUID), CAST(:created_by AS UUID))
ON CONFLICT (id) DO UPDATE
SET entity_type = :entity_type, parent = CAST(:parent AS UUID)

-- :name insert-entity-data-sql :! :n
INSERT INTO registrar.entity_data (entity_id, current_value, data, created_by, schema_version, entity_type, data_hash)
VALUES (CAST(:entity_id AS UUID), false, CAST(:data AS JSONB), CAST(:created_by AS UUID), :schema_version, :entity_type, :data_hash)
ON CONFLICT (entity_id, data_hash) DO NOTHING

-- :name update-current-value-sql :! :n
UPDATE registrar.entity_data
SET current_value = (data_hash = :data_hash)
WHERE entity_id = CAST(:entity_id AS UUID)

-- :name get-entity-by-id-sql :? :1
SELECT data
FROM registrar.entity_data
WHERE current_value AND entity_id = CAST(:id AS UUID)
