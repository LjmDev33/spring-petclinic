package org.springframework.samples.petclinic.counsel;

/*
 * Project : spring-petclinic
 * File    : CounselStatus.java
 * Created : 2025-10-21
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 답글 상태값 ENUM -> WAIT(답변대기) , COMPLETE(답변완료) , END(종료)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public enum CounselStatus {
	WAIT,
	COMPLETE ,
	END
}
