package org.springframework.samples.petclinic.common.service;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * [공통 컴포넌트] HTML 본문 저장소
 * - 역할: XSS 방어(Jsoup) + 파일 저장(File I/O)
 * - 특징: 도메인(counsel, photo, notice)을 인자로 받아 경로를 동적으로 분기함.
 */
@Component
public class CommonHtmlStorage {

	private static final Logger log = LoggerFactory.getLogger(CommonHtmlStorage.class);

	// [설정] 최상위 루트 경로 (counsel, photo 등의 상위 폴더)
	// 실제 운영 시에는 application.yml에서 @Value로 주입받는 것이 좋습니다.
	@Value("${petclinic.file.base-dir}")
	private String rootBase;

	private static final Set<String> ALLOWED_MIME = Set.of("text/html", "application/xhtml+xml");
	private static final Tika TIKA = new Tika();

	/**
	 * HTML 저장 (통합 메서드)
	 * @param rawHtml 원본 HTML
	 * @param domain 도메인명 (예: "counsel", "photo", "notice")
	 * @return 저장된 파일의 절대 경로
	 */
	public String saveHtml(String rawHtml, String domain) throws IOException {
		log.info("📝 [HTML Save] Domain: {}, Length: {}", domain, rawHtml == null ? 0 : rawHtml.length());

		// 1. 보안 정책 적용 (중앙 집중 관리)
		Safelist safelist = Safelist.relaxed()
			.addTags("img")
			.addAttributes(":all", "style", "class", "width", "height")
			.addAttributes("img", "src", "alt", "title", "data-filename")
			.preserveRelativeLinks(true)
			.removeProtocols("img", "src", "http", "https"); // 프로토콜 제약 해제

		// 2. 정제 (Sanitize)
		String sanitized = Jsoup.clean(rawHtml == null ? "" : rawHtml, safelist);

		// 2-1. [2차 보안] 자바스크립트 프로토콜 명시적 차단
		if (sanitized.contains("src=\"javascript:") || sanitized.contains("src='javascript:")) {
			log.warn("🚨 [Security] XSS attempt detected in domain: {}", domain);
			throw new SecurityException("XSS attempt detected");
		}

		// 3. 저장 경로 동적 생성
		// 예: ROOT_BASE + "/counsel" + "/content"
		Path domainPath = Paths.get(rootBase, domain, "content");
		LocalDate today = LocalDate.now();
		String yyyy = String.format("%04d", today.getYear());
		String mm = String.format("%02d", today.getMonthValue());

		// 최종 디렉토리: .../data/counsel/content/2026/02
		Path targetDir = domainPath.resolve(Paths.get(yyyy, mm)).normalize();
		Files.createDirectories(targetDir);

		// 파일명 생성
		String uuid = UUID.randomUUID().toString();
		Path file = targetDir.resolve(uuid + ".html").normalize();

		// 4. 역참조(Path Traversal) 방어
		// 해당 도메인 폴더 내부에 있는지 확인
		if (!file.startsWith(domainPath.normalize())) {
			throw new SecurityException("Invalid path traversal attempt");
		}

		// 5. 파일 쓰기
		Files.writeString(file, sanitized, StandardCharsets.UTF_8);

		// 6. MIME 검증
		String mime = TIKA.detect(file);
		if (!ALLOWED_MIME.contains(mime)) {
			Files.deleteIfExists(file);
			throw new IOException("Invalid mime type: " + mime);
		}

		log.info("✅ [HTML Save] Success: {}", file);
		return file.toString();
	}

	/**
	 * HTML 로드
	 */
	public String loadHtml(String path, String domain) throws IOException {
		if (path == null || path.isBlank()) return "";

		// 검증용 루트: .../data/{domain}/content
		Path domainBase = Paths.get(rootBase, domain, "content").toAbsolutePath().normalize();
		Path file = Paths.get(path).toAbsolutePath().normalize();

		// 보안 검사: 요청한 파일이 해당 도메인 폴더 안에 있는가?
		if (!file.startsWith(domainBase)) {
			log.warn("🚨 [Security] Invalid path access attempt. Domain: {}, Path: {}", domain, path);
			throw new SecurityException("Invalid path traversal attempt");
		}

		if (!Files.exists(file)) return "";
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	// deleteHtml도 동일한 방식으로 domain 받아서 처리...
	public void deleteHtml(String path, String domain) throws IOException {
		if (path == null || path.isBlank()) return;
		Path domainBase = Paths.get(rootBase, domain, "content").toAbsolutePath().normalize();
		Path file = Paths.get(path).toAbsolutePath().normalize();
		if (!file.startsWith(domainBase)) throw new SecurityException("Invalid path traversal attempt");
		if (Files.exists(file)) Files.delete(file);
	}
}
