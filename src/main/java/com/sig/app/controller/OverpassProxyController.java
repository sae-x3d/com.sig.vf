package com.sig.app.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URLEncoder;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class OverpassProxyController {

	private final File cacheDir = new File("/tmp/osm-overpass-cache");
	private final ObjectMapper mapper = new ObjectMapper();

	@GetMapping("/api/osm/search")
	public ResponseEntity<String> search(
			@RequestParam String nom,
			@RequestParam double y,
			@RequestParam double x) {
		try {
			cacheDir.mkdirs();
			String cacheKey = nom.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + Math.round(y * 1000) + "_" + Math.round(x * 1000);
			File cached = new File(cacheDir, cacheKey + ".json");
			if (cached.exists()) {
				String data = new String(java.nio.file.Files.readAllBytes(cached.toPath()), StandardCharsets.UTF_8);
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(data);
			}
			String query = "[out:json];(node(around:100," + y + "," + x + ")[\"name\"~\"" + nom + "\",i];node(around:100," + y + "," + x + ")[\"tourism\"~\"hotel\",i][\"name\"~\"" + nom + "\",i];);out body 20;";
			String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
			byte[] data;
			var conn = (java.net.HttpURLConnection) URI.create(url).toURL().openConnection();
			conn.setRequestProperty("User-Agent", "SIG-Tana/1.0 (educational project; contact@sig-tana.local)");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(15000);
			try (var in = conn.getInputStream(); var buf = new java.io.ByteArrayOutputStream()) {
				in.transferTo(buf);
				data = buf.toByteArray();
			}
			java.nio.file.Files.write(cached.toPath(), data);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new String(data, StandardCharsets.UTF_8));
		} catch (Exception e) {
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"hors-ligne\",\"elements\":[]}");
		}
	}
}
