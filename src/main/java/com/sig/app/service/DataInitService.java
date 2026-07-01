package com.sig.app.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sig.app.model.Etablissement;
import com.sig.app.model.Fokontany;
import com.sig.app.repository.EtablissementRepository;
import com.sig.app.repository.FokontanyRepository;

@Component
public class DataInitService implements CommandLineRunner {

	private final EtablissementRepository etabRepo;
	private final FokontanyRepository fokoRepo;
	private final ObjectMapper mapper;

	public DataInitService(EtablissementRepository etabRepo, FokontanyRepository fokoRepo,
			ObjectMapper mapper) {
		this.etabRepo = etabRepo;
		this.fokoRepo = fokoRepo;
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		if (fokoRepo.count() == 0) {
			loadFokontany();
		}
		if (etabRepo.count() == 0) {
			loadEtablissements();
		}
		populateGeom();
	}

	private void populateGeom() {
		etabRepo.populateGeomColumn();
	}

	@Transactional
	public void reloadFokontany() throws Exception {
		fokoRepo.deleteAll();
		loadFokontany();
		System.out.println("Fokontany réimportés");
	}

	@Transactional
	public void reloadEtablissements() throws Exception {
		etabRepo.deleteAll();
		loadEtablissements();
		populateGeom();
		System.out.println("Établissements réimportés");
	}

	@Transactional
	public void reload() throws Exception {
		fokoRepo.deleteAll();
		etabRepo.deleteAll();
		loadFokontany();
		loadEtablissements();
		populateGeom();
		System.out.println("Reimport terminé");
	}

	private void loadFokontany() throws Exception {
		var is = getClass().getResourceAsStream("/data/quartiers.geojson");
		var root = mapper.readTree(is);
		var features = root.get("features");
		List<Fokontany> batch = new ArrayList<>();
		for (JsonNode feat : features) {
			var props = feat.get("properties");
			var f = new Fokontany();
			f.setNom(props.get("fokontany").asText());
			f.setArrondissement(props.get("arrondissement").asText());
			f.setPcode(props.get("pcode").asText());
			f.setPolygon(feat.get("geometry").toString());
			batch.add(f);
		}
		fokoRepo.saveAll(batch);
		System.out.println("Importés : " + batch.size() + " fokontany");
	}

	private void loadEtablissements() throws Exception {
		var is = getClass().getResourceAsStream("/data/etablissements.csv");
		var reader = new BufferedReader(new InputStreamReader(is));
		String header = reader.readLine();
		if (header == null)
			return;
		List<Etablissement> batch = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isBlank())
				continue;
			String[] parts = parseCsvLine(line);
			if (parts.length < 5)
				continue;
			var e = new Etablissement();
			e.setNom(parts[0]);
			e.setType(parts[1]);
			e.setNote(Double.parseDouble(parts[2]));
			e.setX(Double.parseDouble(parts[3]));
			e.setY(Double.parseDouble(parts[4]));
			batch.add(e);
		}
		etabRepo.saveAll(batch);
		System.out.println("Importés : " + batch.size() + " établissements");
	}

	private String[] parseCsvLine(String line) {
		List<String> fields = new ArrayList<>();
		boolean inQuotes = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
					sb.append('"');
					i++;
				} else {
					inQuotes = !inQuotes;
				}
			} else if (c == ',' && !inQuotes) {
				fields.add(sb.toString().trim());
				sb.setLength(0);
			} else {
				sb.append(c);
			}
		}
		fields.add(sb.toString().trim());
		return fields.toArray(new String[0]);
	}
}
