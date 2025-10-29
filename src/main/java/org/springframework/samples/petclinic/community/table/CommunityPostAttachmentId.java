package org.springframework.samples.petclinic.community.table;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/*
 * Project : spring-petclinic
 * File    : CommunityPostAttachmentId.java
 * Created : 2025-10-23
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 공용 Attachment테이블과 게시글-Attachment 테이블이 분리되어있고 이 두 테이블의 연결관계를
 *         테이블의 복합키(post_id , attachment_id)로 설계했기 때문에 JPA에서 그 PK를 표현하려고
 *         @Embeddable ID 클래스(ex: CommunityPostAttachmentId)를 사용 함.
 *         우리 프로젝트는 그룹웨어처럼 첨부파일의 순서 , 노출여부 등을 컬럼으로 제어할필요없이
 *         한게시판에서 첨부하기만하면 되기때문에 복합키방식을 채택했다.
 *         변경이 거의 (추가 / 삭제) 이며 키변경(update)가 필요없기때문
 * 		   장점
 *         1. 자연키 기반 무결성 : (post_id, attachment_id) 자체가 존재 의미이므로 별도 UNIQUE 제약 불필요(=PK가 곧 유니크).
 *         2. 중복 연결 방지 : 같은 파일을 같은 글에 중복으로 달 수 없음(디비가 보장).
 * 		   3. 정규화 + 재사용성 : 파일 메타는 한 곳(attachment)에서 관리, 게시판들은 링크만 가짐 → 저장/백업/서빙 정책 일관.
 *		   4. 조인 성능/명확성 : 조인 조건이 PK/FK 기반으로 깔끔하고, 인덱스 전략 수립이 명확.
 *  	   단점
 *         1. 리포지토리 제네릭이 복합키 : JpaRepository<CommunityPostAttachment, CommunityPostAttachmentId>처럼 다소 장황하고, 식별자를 다루는 코드가 복잡해짐.
 *         2. 키 변경이 곧 식별자 변경 : (post_id, attachment_id) 중 하나라도 바꾸려면 update가 아니라 delete+insert가 맞음(식별자 변경은 곤란).
 *         3. equals/hashCode/Serializable 구현 의무 : 키 클래스에 보일러플레이트 코드 필요. (테스트/리뷰로 품질 관리 필요)
 *         4. 다형 매핑이 필요하면 번거로움 : “모든 게시판을 하나의 xxx_attachment_link로 묶고 싶다(타입/타겟ID polymorphic)” 같은 요구엔FK 무결성이 약해지거나(타겟을 FK로 못 걸어 enum+target_id만 두는 방식) 추가 설계가 필요.
 *
 *   TODO: 사용목적
 *           “첨부파일을 게시글과 분리해 Attachment 메타를 재사용하고, 게시글-첨부의 조인은 (post_id, attachment_id)를 복합 PK로 설계해 중복 연결을 DB에서 강제 차단했습니다.
 * 			 JPA에서는 복합키를 식별자로 쓰려면 @Embeddable ID 클래스가 필요하고, 이를 통해 무결성과 조인 성능을 확보했습니다.
 * 			 대안으로는 링크 테이블에 단일 PK를 두고 (post_id, attachment_id)에 UNIQUE 제약을 두는 방법이 있는데, 이 경우 리포지토리는 단순해지지만 자연키의 명료함은 떨어집니다.
 * 			 우리 시스템은 링크 행을 개별 식별자로 다룰 필요가 적고, 중복 방지가 중요해 복합 PK를 택했습니다.”
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Embeddable
public class CommunityPostAttachmentId implements Serializable {

	private Long communityPostId;
	private Long attachmentId;

	public CommunityPostAttachmentId() {}
	public CommunityPostAttachmentId(Long communityPostId, Long attachmentId) {
		this.communityPostId = communityPostId;
		this.attachmentId = attachmentId;
	}

	@Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof CommunityPostAttachmentId that)) return false;
		return Objects.equals(communityPostId, that.communityPostId) && Objects.equals(attachmentId, that.attachmentId); }
	@Override public int hashCode(){ return Objects.hash(communityPostId, attachmentId); }

	public Long getCommunityPostId() {
		return communityPostId;
	}

	public void setCommunityPostId(Long communityPostId) {
		this.communityPostId = communityPostId;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}
}
