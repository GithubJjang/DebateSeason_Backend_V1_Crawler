package com.debate.croll.producer.crawler.dto;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.common.Type;

public record CheckPointDTO(
	String name,
	Integer subKey,
	Integer crawlIndex,
	Type type,
	String updatedAt,
	Status status
) {
}
