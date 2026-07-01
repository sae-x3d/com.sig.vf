package com.sig.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sig.app.model.Etablissement;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

	List<Etablissement> findByType(String type);

	List<Etablissement> findByTypeAndNoteGreaterThanEqual(String type, Double noteMin);

	List<Etablissement> findByNoteGreaterThanEqual(Double noteMin);

	@Query("SELECT e FROM Etablissement e WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :q, '%'))")
	List<Etablissement> searchByNom(@Param("q") String q);

	List<Etablissement> findTop20ByOrderByNoteDesc();

	@Query(value = """
			SELECT e.* FROM etablissement e, fokontany f
			WHERE f.id = :fokontanyId
			AND ST_Within(e.geom, ST_SetSRID(ST_GeomFromGeoJSON(f.polygon), 4326))
			""", nativeQuery = true)
	List<Etablissement> findByFokontanyId(@Param("fokontanyId") Long fokontanyId);

	@Query(value = """
			SELECT e.* FROM etablissement e, fokontany f
			WHERE f.id = :fokontanyId
			AND e.type = :type
			AND ST_Within(e.geom, ST_SetSRID(ST_GeomFromGeoJSON(f.polygon), 4326))
			""", nativeQuery = true)
	List<Etablissement> findByFokontanyIdAndType(@Param("fokontanyId") Long fokontanyId,
			@Param("type") String type);

	@Query(value = """
			SELECT e.* FROM etablissement e
			WHERE ST_DWithin(CAST(e.geom AS geography), CAST(ST_SetSRID(ST_MakePoint(:x, :y), 4326) AS geography), :radius)
			""", nativeQuery = true)
	List<Etablissement> findByProximity(@Param("x") Double x, @Param("y") Double y,
			@Param("radius") Double radius);

	@Query(value = """
			SELECT f.id, f.nom, COUNT(e.id) AS cnt
			FROM fokontany f
			LEFT JOIN etablissement e
			ON ST_Contains(ST_SetSRID(ST_GeomFromGeoJSON(f.polygon), 4326), e.geom)
			GROUP BY f.id, f.nom
			ORDER BY cnt DESC
			""", nativeQuery = true)
	List<Object[]> countByFokontany();

	@Query(value = "SELECT COUNT(*) FROM etablissement", nativeQuery = true)
	Long countAll();

	@Query(value = "SELECT AVG(note) FROM etablissement", nativeQuery = true)
	Double avgNote();

	@Query(value = "SELECT MIN(note) FROM etablissement", nativeQuery = true)
	Double minNote();

	@Query(value = "SELECT MAX(note) FROM etablissement", nativeQuery = true)
	Double maxNote();

	@Query(value = "SELECT COUNT(*) FROM etablissement WHERE type = :type", nativeQuery = true)
	Long countByType(@Param("type") String type);

	@Modifying
	@Query(value = "UPDATE etablissement SET geom = ST_SetSRID(ST_MakePoint(x, y), 4326) WHERE geom IS NULL", nativeQuery = true)
	void populateGeomColumn();
}
