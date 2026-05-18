package com.debate.croll.producer.crawler.mapper.error;

import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.infrastructure.entity.ErrorEntity;

public class ErrorMapper {

	public static ErrorEntity toEntity(CrawlerErrorDTO dto){

		return ErrorEntity.builder()
			.OriginClass(dto.originClass())
			.type(dto.type())
			.name(dto.name())
			.exceptionClass(dto.exceptionClassName())
			.message(dto.message())
			.url(dto.url())
			.createdAt(dto.createdAt())
			.retry(0)
			.build();
	}
}
