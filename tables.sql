CREATE TABLE etablissement (
    id       BIGSERIAL    PRIMARY KEY,
    nom      VARCHAR(255),
    type     VARCHAR(255),
    note     DOUBLE PRECISION,
    x        DOUBLE PRECISION,
    y        DOUBLE PRECISION
);

CREATE TABLE fokontany (
    id             BIGSERIAL    PRIMARY KEY,
    nom            VARCHAR(255),
    arrondissement VARCHAR(255),
    pcode          VARCHAR(255),
    polygon        TEXT
);

ALTER TABLE etablissement ADD COLUMN geom GEOMETRY(Point, 4326);

UPDATE etablissement SET geom = ST_SetSRID(ST_MakePoint(x, y), 4326) WHERE geom IS NULL;

CREATE INDEX idx_etablissement_geom ON etablissement USING GIST (geom);

CREATE INDEX idx_fokontany_polygon_geom ON fokontany USING GIST (ST_SetSRID(ST_GeomFromGeoJSON(polygon), 4326));
