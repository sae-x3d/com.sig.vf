package com.sig.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sig.app.model.Fokontany;

public interface FokontanyRepository extends JpaRepository<Fokontany, Long> {

	@Query(value = """
			SELECT f.* FROM fokontany f
			WHERE ST_Within(ST_SetSRID(ST_MakePoint(:x, :y), 4326),
			                ST_SetSRID(ST_GeomFromGeoJSON(f.polygon), 4326))
			LIMIT 1
			""", nativeQuery = true)
	Fokontany findByPoint(@Param("x") Double x, @Param("y") Double y);
}
