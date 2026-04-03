package com.debate.croll.producer.crawler.dto;

import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

public class ErrorDTO {

	public record CreateErrorDTO(
		OriginClass originClass,
		Type type,
		String name,
		String exceptionClassName,
		String message,
		String url,
		String createdAt
	){}
}
