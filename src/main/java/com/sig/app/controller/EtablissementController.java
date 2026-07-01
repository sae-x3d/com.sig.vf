package com.sig.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sig.app.model.Etablissement;
import com.sig.app.repository.EtablissementRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/etablissements")
public class EtablissementController {

	private final EtablissementRepository repo;

	public EtablissementController(EtablissementRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	public List<Etablissement> getAll(@RequestParam(required = false) String type,
			@RequestParam(required = false) Double noteMin) {
		if (type != null && noteMin != null)
			return repo.findByTypeAndNoteGreaterThanEqual(type, noteMin);
		if (type != null)
			return repo.findByType(type);
		if (noteMin != null)
			return repo.findByNoteGreaterThanEqual(noteMin);
		return repo.findAll();
	}

	@GetMapping("/{id}")
	public Etablissement getById(@PathVariable Long id) {
		return repo.findById(id).orElseThrow();
	}

	@GetMapping("/search")
	public List<Etablissement> search(@RequestParam String q) {
		return repo.searchByNom(q);
	}

	@GetMapping("/top")
	public List<Etablissement> getTop() {
		return repo.findTop20ByOrderByNoteDesc();
	}

	@GetMapping("/proximity")
	public List<Etablissement> getProximity(@RequestParam Double x, @RequestParam Double y,
			@RequestParam(defaultValue = "500") Double radius) {
		return repo.findByProximity(x, y, radius);
	}

	@GetMapping("/by-fokontany/{fokontanyId}")
	public List<Etablissement> getByFokontany(@PathVariable Long fokontanyId,
			@RequestParam(required = false) String type) {
		if (type != null)
			return repo.findByFokontanyIdAndType(fokontanyId, type);
		return repo.findByFokontanyId(fokontanyId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Etablissement create(@Valid @RequestBody Etablissement e) {
		return repo.save(e);
	}

	@PutMapping("/{id}")
	public Etablissement update(@PathVariable Long id, @Valid @RequestBody Etablissement updated) {
		var existing = repo.findById(id).orElseThrow();
		existing.setNom(updated.getNom());
		existing.setType(updated.getType());
		existing.setNote(updated.getNote());
		existing.setX(updated.getX());
		existing.setY(updated.getY());
		return repo.save(existing);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		repo.deleteById(id);
	}
}
