package com.sig.app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sig.app.repository.EtablissementRepository;
import com.sig.app.repository.FokontanyRepository;
import com.sig.app.service.DataInitService;

@RestController
@RequestMapping("/api/manage")
public class ManageController {

	private final EtablissementRepository etabRepo;
	private final FokontanyRepository fokoRepo;
	private final DataInitService dataInit;

	public ManageController(EtablissementRepository etabRepo,
			FokontanyRepository fokoRepo, DataInitService dataInit) {
		this.etabRepo = etabRepo;
		this.fokoRepo = fokoRepo;
		this.dataInit = dataInit;
	}

	@PostMapping("/reload/fokontany")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> reloadFokontany() throws Exception {
		dataInit.reloadFokontany();
		return Map.of("status", "ok", "message", "Fokontany actualisés");
	}

	@PostMapping("/reload/etablissement")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> reloadEtablissements() throws Exception {
		dataInit.reloadEtablissements();
		return Map.of("status", "ok", "message", "Établissements actualisés");
	}

	@PostMapping("/reload")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> reload() throws Exception {
		dataInit.reload();
		return Map.of("status", "ok", "message", "Toutes les données réimportées");
	}
}
