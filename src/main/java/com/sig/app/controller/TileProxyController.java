package com.sig.app.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TileProxyController {

	private final File cacheDir = new File("/tmp/osm-tiles");

	private static final byte[] BLANK_TILE = java.util.Base64.getDecoder().decode(
		"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR4nGNgAAIAAAUAAXpeqz8AAAAASUVORK5CYII=");

	@GetMapping("/tiles/{z}/{x}/{y}.png")
	public ResponseEntity<byte[]> tile(@PathVariable int z, @PathVariable int x, @PathVariable int y) {
		try {
			File cached = new File(cacheDir, z + "/" + x + "/" + y + ".png");
			if (cached.exists()) {
				byte[] data = readFile(cached);
				if (isValidPng(data)) {
					return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(data);
				}
				cached.delete();
			}
			String url = "https://tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png";
			byte[] data = download(url);
			cached.getParentFile().mkdirs();
			try (var out = new FileOutputStream(cached)) {
				out.write(data);
			}
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(data);
		} catch (Exception e) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(BLANK_TILE);
		}
	}

	private boolean isValidPng(byte[] data) {
		return data.length > 8 && data[0] == (byte) 0x89 && data[1] == 0x50
			&& data[2] == 0x4E && data[3] == 0x47;
	}

	private byte[] readFile(File f) throws Exception {
		try (var in = new FileInputStream(f)) {
			return in.readAllBytes();
		}
	}

	private byte[] download(String url) throws Exception {
		var conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestProperty("User-Agent", "SIG-Tana/1.0 (educational project; contact@sig-tana.local)");
		conn.setRequestProperty("Referer", "http://localhost:8080/");
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(10000);
		String contentType = conn.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			try (var err = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream()) {
				err.readAllBytes();
			}
			throw new Exception("Blocked: " + conn.getResponseCode() + " " + contentType);
		}
		try (var in = conn.getInputStream(); var buf = new ByteArrayOutputStream()) {
			in.transferTo(buf);
			return buf.toByteArray();
		}
	}
}
