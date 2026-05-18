package com.debate.croll.producer.crawler.dto.error;

import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

public record CrawlerErrorDTO(
	OriginClass originClass,
	Type type,
	String name,
	String exceptionClassName,
	String message,
	String url,
	String createdAt
){}
