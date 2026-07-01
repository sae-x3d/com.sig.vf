package com.sig.app.controller;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingProxyController {

	private final File cacheDir = new File("/tmp/osrm-cache");

	@GetMapping("/api/routing")
	public ResponseEntity<String> route(
			@RequestParam double startLon, @RequestParam double startLat,
			@RequestParam double endLon, @RequestParam double endLat) {
		try {
			cacheDir.mkdirs();
			String cacheKey = Math.round(startLon * 1000) + "_" + Math.round(startLat * 1000)
					+ "_" + Math.round(endLon * 1000) + "_" + Math.round(endLat * 1000);
			File cached = new File(cacheDir, cacheKey + ".json");
			if (cached.exists()) {
				String data = new String(java.nio.file.Files.readAllBytes(cached.toPath()), StandardCharsets.UTF_8);
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(data);
			}
			String url = "https://router.project-osrm.org/route/v1/driving/"
					+ startLon + "," + startLat + ";" + endLon + "," + endLat
					+ "?geometries=geojson&overview=full&steps=true&alternatives=true";
			var conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
			conn.setRequestProperty("User-Agent", "SIG-Tana/1.0 (educational project; contact@sig-tana.local)");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(15000);
			byte[] data;
			try (var in = conn.getInputStream(); var buf = new java.io.ByteArrayOutputStream()) {
				in.transferTo(buf);
				data = buf.toByteArray();
			}
			java.nio.file.Files.write(cached.toPath(), data);
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new String(data, StandardCharsets.UTF_8));
		} catch (Exception e) {
			try {
				String fallback = "{\"code\":\"NoRoute\",\"message\":\"Hors ligne ou service indisponible\"}";
				if (cacheDir.list() != null) {
					File[] files = cacheDir.listFiles((d, n) -> n.endsWith(".json"));
					if (files != null && files.length > 0 && files[0].exists()) {
						String cached = new String(java.nio.file.Files.readAllBytes(files[0].toPath()), StandardCharsets.UTF_8);
						return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(cached);
					}
				}
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fallback);
			} catch (Exception e2) {
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
						.body("{\"code\":\"NoRoute\",\"message\":\"Hors ligne\"}");
			}
		}
	}
}
