package org.springframework.samples.petclinic.counsel.service;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	// [확인 필요] application.yml의 petclinic.file.base-dir 경로와 일치해야 합니다.
	// 만약 yml 설정이 바뀌었다면 이 경로도 맞춰주거나, @Value로 주입받는 것이 좋습니다.
	private static final String BASE = "C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/content";
	private static final Set<String> ALLOWED_EXT = Set.of(".html");
	private static final Set<String> ALLOWED_MIME = Set.of("text/html", "application/xhtml+xml");
	private static final Tika TIKA = new Tika();

	private static final Logger log = LoggerFactory.getLogger(CounselContentStorage.class);

	/**
	 * HTML 본문을 sanitize 후 안전한 위치에 저장합니다.
	 * @param rawHtml 사용자가 입력한 원본 HTML
	 * @return 저장된 절대경로(String)
	 * @throws IOException 디스크 쓰기/검증 과정 실패 시
	 */
	public String saveHtml(String rawHtml) throws IOException {

		// 1. [진단] 원본 HTML 로그 출력 (INFO 레벨로 변경하여 무조건 출력)
		//log.info("📝 [HTML INPUT] Length: {}", rawHtml == null ? 0 : rawHtml.length());
		//log.info("📝 [HTML INPUT CONTENT] : {}", rawHtml); // 내용 직접 확인

		// [수정] Quill 에디터 호환성을 위한 Safelist 커스텀
		Safelist safelist = Safelist.relaxed()
			.addTags("img") // img 태그 명시적 허용
			.addAttributes(":all", "style", "class", "width", "height") // 스타일 허용
			.addAttributes("img", "src", "alt", "title", "data-filename") // img 속성 허용
			.preserveRelativeLinks(true) // 상대 경로(/images/...) 보존
			// [핵심 해결책] relaxed 모드가 기본적으로 강제하는 http, https 검사를 '제거'합니다.
			// 이렇게 하면 Jsoup은 src 값을 'URL'이 아닌 '단순 문자열'로 취급하여
			// /images/ 로 시작하는 상대 경로를 100% 허용하게 됩니다.
		    .removeProtocols("img", "src", "http", "https");

		// 2. HTML 정제 (Cleaning)
		String sanitized = Jsoup.clean(rawHtml == null ? "" : rawHtml, safelist);

		// [디버깅] Jsoup이 태그를 지웠는지 확인하기 위해 로그 출력
		// 만약 sanitized에 <img> 태그가 없다면 Jsoup 설정 문제입니다.
		//log.info("🧹 [HTML Sanitize] Result: {}", sanitized);

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

		//log.info("✅ [HTML Save] Success: {}", file);

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

		// 로드 시 로그
		// log.debug("📖 [HTML Load] Reading file: {}", file);

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
