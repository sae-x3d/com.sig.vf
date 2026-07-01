package com.sig.app.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sig.app.repository.EtablissementRepository;
import com.sig.app.repository.FokontanyRepository;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

	private final EtablissementRepository etabRepo;
	private final FokontanyRepository fokoRepo;

	public StatsController(EtablissementRepository etabRepo, FokontanyRepository fokoRepo) {
		this.etabRepo = etabRepo;
		this.fokoRepo = fokoRepo;
	}

	@GetMapping("/summary")
	public Map<String, Object> summary() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("total", etabRepo.countAll());
		m.put("avgNote", Math.round(etabRepo.avgNote() * 100.0) / 100.0);
		m.put("minNote", Math.round(etabRepo.minNote() * 10.0) / 10.0);
		m.put("maxNote", Math.round(etabRepo.maxNote() * 10.0) / 10.0);
		m.put("fokontanyCount", fokoRepo.count());
		m.put("restaurants", etabRepo.countByType("restaurant"));
		m.put("hotels", etabRepo.countByType("hotel"));
		return m;
	}

	@GetMapping("/by-fokontany")
	public List<Map<String, Object>> byFokontany() {
		List<Object[]> rows = etabRepo.countByFokontany();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] row : rows) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("id", row[0]);
			m.put("nom", row[1]);
			m.put("count", row[2]);
			result.add(m);
		}
		return result;
	}

	@GetMapping("/by-type")
	public List<Map<String, Object>> byType() {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> r = new LinkedHashMap<>();
		r.put("type", "restaurant");
		r.put("count", etabRepo.countByType("restaurant"));
		result.add(r);
		Map<String, Object> h = new LinkedHashMap<>();
		h.put("type", "hotel");
		h.put("count", etabRepo.countByType("hotel"));
		result.add(h);
		return result;
	}
}
