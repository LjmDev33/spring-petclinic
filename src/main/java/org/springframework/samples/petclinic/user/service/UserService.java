package org.springframework.samples.petclinic.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.mapper.CounselPostMapper;
import org.springframework.samples.petclinic.counsel.repository.CounselCommentRepository;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.samples.petclinic.user.dto.UserRegisterDto;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Project : spring-petclinic
 * File    : UserService.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 사용자 관리 서비스
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CounselPostRepository counselPostRepository;
	private final CounselCommentRepository counselCommentRepository;
	private final CounselPostMapper counselPostMapper;

	public UserService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder,
					   CounselPostRepository counselPostRepository,
					   CounselCommentRepository counselCommentRepository,
					   CounselPostMapper counselPostMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.counselPostRepository = counselPostRepository;
		this.counselCommentRepository = counselCommentRepository;
		this.counselPostMapper = counselPostMapper;
	}

	/**
	 * 회원가입 처리
	 * - 아이디, 이메일, 닉네임 중복 검증
	 * - 비밀번호 BCrypt 암호화
	 * - ROLE_USER 권한 부여
	 *
	 * @param dto 회원가입 정보
	 * @throws IllegalArgumentException 중복된 정보 존재 시
	 */
	public void register(UserRegisterDto dto) {
		// 1. 아이디 중복 검증
		if (userRepository.existsByUsername(dto.getUsername())) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}

		// 2. 이메일 중복 검증
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		// 3. 닉네임 중복 검증 (새로 추가)
		if (userRepository.existsByNickname(dto.getNickname())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}

		// 4. User 엔티티 생성
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setEmail(dto.getEmail());
		user.setName(dto.getName());
		user.setNickname(dto.getNickname()); // 닉네임 저장
		user.setPhone(dto.getPhone());

		// 5. 권한 설정 (기본: ROLE_USER)
		Set<String> roles = new HashSet<>();
		roles.add("ROLE_USER");
		user.setRoles(roles);

		// 6. 저장
		userRepository.save(user);
		log.info("User registered: username={}, nickname={}", dto.getUsername(), dto.getNickname());
	}

	/**
	 * 사용자 정보 조회 (username 기준)
	 *
	 * @param username 사용자 아이디
	 * @return User 엔티티
	 */
	@Transactional(readOnly = true)
	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
	}

	/**
	 * 사용자 정보 수정
	 * - 이메일, 이름, 닉네임, 전화번호 수정 가능
	 * - 닉네임 중복 검증 (본인 제외)
	 *
	 * @param username 사용자 아이디
	 * @param email 이메일
	 * @param name 이름
	 * @param nickname 닉네임
	 * @param phone 전화번호
	 */
	public void updateProfile(String username, String email, String name, String nickname, String phone) {
		User user = findByUsername(username);

		// 닉네임 중복 검증 (본인 제외)
		if (!user.getNickname().equals(nickname) && userRepository.existsByNickname(nickname)) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}

		// 이메일 중복 검증 (본인 제외)
		if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}

		user.setEmail(email);
		user.setName(name);
		user.setNickname(nickname);
		user.setPhone(phone);

		userRepository.save(user);
		log.info("User profile updated: username={}, nickname={}", username, nickname);
	}

	/**
	 * 비밀번호 변경
	 *
	 * @param username 사용자 아이디
	 * @param newPassword 새 비밀번호 (평문)
	 */
	public void changePassword(String username, String newPassword) {
		User user = findByUsername(username);
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		log.info("User password changed: username={}", username);
	}

	/**
	 * 내가 작성한 온라인상담 게시글 목록 조회
	 *
	 * @param nickname 작성자 닉네임
	 * @param pageable 페이징 정보
	 * @return 게시글 페이지
	 */
	@Transactional(readOnly = true)
	public PageResponse<CounselPostDto> getMyPosts(String nickname, Pageable pageable) {
		Page<CounselPost> entityPage = counselPostRepository.findByAuthorNameOrderByCreatedAtDesc(nickname, pageable);
		Page<CounselPostDto> dtoPage = entityPage.map(counselPostMapper::toDto);
		return new PageResponse<>(dtoPage);
	}

	/**
	 * 내가 작성한 댓글 목록 조회
	 *
	 * @param nickname 작성자 닉네임
	 * @param pageable 페이징 정보
	 * @return 댓글 페이지
	 */
	@Transactional(readOnly = true)
	public Page<CounselComment> getMyComments(String nickname, Pageable pageable) {
		return counselCommentRepository.findByAuthorNameOrderByCreatedAtDesc(nickname, pageable);
	}
}
