package com.debate.croll.producer.crawler.request;

import java.time.LocalDateTime;

import com.debate.croll.producer.crawler.type.OriginClass;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.entity.ErrorEntity;

public class ErrorDTO {

	public record CreateErrorDTO(
		OriginClass originClass,
		Type type,
		String name,
		String exceptionClassName,
		String message,
		String stackTrace,
		String createdAt
	){}
}
