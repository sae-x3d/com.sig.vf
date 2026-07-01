CREATE INDEX IF NOT EXISTS idx_fokontany_polygon_geom ON fokontany USING GIST (ST_SetSRID(ST_GeomFromGeoJSON(polygon), 4326));
ALTER TABLE etablissement ADD COLUMN IF NOT EXISTS geom GEOMETRY(Point, 4326);
UPDATE etablissement SET geom = ST_SetSRID(ST_MakePoint(x, y), 4326) WHERE geom IS NULL;
CREATE INDEX IF NOT EXISTS idx_etablissement_geom ON etablissement USING GIST (geom);
