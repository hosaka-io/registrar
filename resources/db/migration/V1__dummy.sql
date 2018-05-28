CREATE TABLE registrar.entity_types
(
entity_type text COLLATE pg_catalog."default" NOT NULL,
description text COLLATE pg_catalog."default",
rank integer NOT NULL,
CONSTRAINT registrar_entity_types_pkey PRIMARY KEY (entity_type)
);
