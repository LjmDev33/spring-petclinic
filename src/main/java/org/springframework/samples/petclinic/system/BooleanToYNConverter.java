package org.springframework.samples.petclinic.system;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/*
 * Project : spring-petclinic
 * File    : BooleanToYNConverter.java
 * Created : 2025-10-23
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: boolean타입으로 테이블생성해도 TRUE / FALSE에따라 Y / N의 String값으로 자동치환되어 테이블에 insert됨
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Converter(autoApply = true)
public class BooleanToYNConverter implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean attribute) {
		if (attribute == null) {
			return "N";
		}
		return attribute ? "Y" : "N";
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return false;
		}
		return dbData.equalsIgnoreCase("Y");
	}
}
