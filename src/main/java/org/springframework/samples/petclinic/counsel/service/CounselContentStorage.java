package org.springframework.samples.petclinic.counsel.service;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/*
 * Project : spring-petclinic
 * File    : CounselContentStorage.java
 * Created : 2025-10-30
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 게시글 본문을 파일로 안전하게 저장/로드하는 컴포넌트
 *   - XSS sanitize(JSoup), MIME 화이트리스트(Tika), 경로역참조 차단을 적용
 *   - 저장 경로: C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/content/{yyyy}/{MM}/{uuid}.html
 *   미구현(후속): 버전관리, 대용량 스트리밍 업로드, 암호화 저장, 백업/보관 정책 연계
 */
@Component
public class CounselContentStorage {

	private static final String BASE = "C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/content";
	private static final Set<String> ALLOWED_EXT = Set.of(".html");
	private static final Set<String> ALLOWED_MIME = Set.of("text/html", "application/xhtml+xml");
	private static final Tika TIKA = new Tika();

	/**
	 * HTML 본문을 sanitize 후 안전한 위치에 저장합니다.
	 * @param rawHtml 사용자가 입력한 원본 HTML
	 * @return 저장된 절대경로(String)
	 * @throws IOException 디스크 쓰기/검증 과정 실패 시
	 */
	public String saveHtml(String rawHtml) throws IOException {
		String sanitized = Jsoup.clean(rawHtml == null ? "" : rawHtml, Safelist.relaxed());
		LocalDate today = LocalDate.now();
		String yyyy = String.format("%04d", today.getYear());
		String mm = String.format("%02d", today.getMonthValue());
		String uuid = UUID.randomUUID().toString();
		Path dir = Paths.get(BASE, yyyy, mm).normalize();
		Files.createDirectories(dir);
		Path file = dir.resolve(uuid + ".html").normalize();
		// 역참조 차단: base 경로 하위만 허용
		Path base = Paths.get(BASE).toAbsolutePath().normalize();
		if (!file.toAbsolutePath().normalize().startsWith(base)) {
			throw new SecurityException("Invalid path traversal attempt");
		}
		Files.writeString(file, sanitized, StandardCharsets.UTF_8);
		// MIME 확인
		String mime = TIKA.detect(file);
		if (!ALLOWED_MIME.contains(mime)) {
			Files.deleteIfExists(file);
			throw new IOException("Invalid mime type: " + mime);
		}
		return file.toString();
	}

	/**
	 * 저장된 HTML 파일을 로드합니다. 경로는 base 디렉터리 하위만 허용합니다.
	 * @param path 저장 시 반환된 절대경로
	 * @return 파일 내용(HTML 문자열), 없으면 빈 문자열
	 * @throws IOException MIME 검증 실패 혹은 I/O 오류 시
	 */
	public String loadHtml(String path) throws IOException {
		if (path == null || path.isBlank()) return "";
		Path base = Paths.get(BASE).toAbsolutePath().normalize();
		Path file = Paths.get(path).toAbsolutePath().normalize();
		if (!file.startsWith(base)) {
			throw new SecurityException("Invalid path traversal attempt");
		}
		if (!Files.exists(file)) return "";
		String mime = TIKA.detect(file);
		if (!ALLOWED_MIME.contains(mime)) {
			throw new IOException("Invalid mime type: " + mime);
		}
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	/**
	 * 저장된 HTML 파일을 삭제합니다. 경로는 base 디렉터리 하위만 허용합니다.
	 * @param path 삭제할 파일의 절대경로
	 * @throws IOException 파일 삭제 실패 시
	 */
	public void deleteHtml(String path) throws IOException {
		if (path == null || path.isBlank()) return;
		Path base = Paths.get(BASE).toAbsolutePath().normalize();
		Path file = Paths.get(path).toAbsolutePath().normalize();
		if (!file.startsWith(base)) {
			throw new SecurityException("Invalid path traversal attempt");
		}
		if (Files.exists(file)) {
			Files.delete(file);
		}
	}
}
