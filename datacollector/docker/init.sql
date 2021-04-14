create database if not exists ves;
use ves;

create table if not exists ves_measurement (
    -- our id
    id INTEGER NOT NULL AUTO_INCREMENT,
    -- common header
    event_type TEXT,
    version TEXT,
    source_id TEXT,
    reporting_entity_name TEXT,
    start_epoch_microsec BIGINT,
    event_id TEXT,
    last_epoch_microsec BIGINT,
    priority TEXT,
    sequence INT,
    source_name TEXT,
    domain TEXT,
    event_name TEXT,
    reporting_entity_id TEXT,
    nfc_naming_code TEXT,
    nf_naming_code TEXT,
    time_zone_offset TEXT,
    rawdata TEXT NOT NULL,
    CONSTRAINT ves_measurement_pk PRIMARY KEY(id)

);

create table if not exists ves_measurement_fields (
    event_id INTEGER NOT NULL,
    measurement_interval LONG,
    measurement_fields_version VARCHAR(32),
    CONSTRAINT ves_measurement_fields_pk PRIMARY KEY (event_id),
    CONSTRAINT ves_measurement_fields_fk1 FOREIGN KEY (event_id) REFERENCES ves_measurement(id) ON UPDATE CASCADE ON DELETE CASCADE
);


create table if not exists additional_measurement (
    event_id INTEGER NOT NULL,
    am_name VARCHAR(128) NOT NULL,
    ves_measurement_fields_key INTEGER,
    CONSTRAINT additional_measurement_pk PRIMARY KEY(event_id, am_name),
    CONSTRAINT additional_measurement_fk1 FOREIGN KEY(event_id) REFERENCES ves_measurement(id) ON UPDATE CASCADE ON DELETE CASCADE
);

create table if not exists additional_measurement_value (
    event INTEGER NOT NULL,
    am_name VARCHAR(128) NOT NULL,
    additional_measurement_key INTEGER,
     ves_measurement_fields_key INTEGER,
    am_key TEXT NOT NULL,
    am_value TEXT,
    CONSTRAINT additional_measurement_value_fk1 FOREIGN KEY(event, am_name) REFERENCES additional_measurement(event_id, am_name) ON UPDATE CASCADE ON DELETE CASCADE
);

-- to store raw payload i.e. without any parsing
create table if not exists payload (
    event_id INTEGER NOT NULL,
    payload TEXT NOT NULL
);

