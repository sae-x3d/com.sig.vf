package com.sig.app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sig.app.model.Fokontany;
import com.sig.app.repository.FokontanyRepository;

@RestController
@RequestMapping("/api/fokontany")
public class FokontanyController {

	private final FokontanyRepository repo;

	public FokontanyController(FokontanyRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	public List<Fokontany> getAll() {
		return repo.findAll();
	}

	@GetMapping("/{id}")
	public Fokontany getById(@PathVariable Long id) {
		return repo.findById(id).orElseThrow();
	}

	@GetMapping("/search")
	public Fokontany findByPoint(@RequestParam Double x, @RequestParam Double y) {
		return repo.findByPoint(x, y);
	}
}
