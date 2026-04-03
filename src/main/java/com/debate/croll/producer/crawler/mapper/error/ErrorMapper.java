package com.debate.croll.producer.crawler.mapper.error;

import com.debate.croll.producer.crawler.dto.ErrorDTO;
import com.debate.croll.producer.entity.ErrorEntity;

public class ErrorMapper {

	public static ErrorEntity toEntity(ErrorDTO.CreateErrorDTO dto){

		return ErrorEntity.builder()
			.OriginClass(dto.originClass())
			.type(dto.type())
			.name(dto.name())
			.exceptionClass(dto.exceptionClassName())
			.message(dto.message())
			.url(dto.url())
			.createdAt(dto.createdAt())
			.build();
	}
}
