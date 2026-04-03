package com.debate.croll.producer.crawler.dto;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.common.Type;

public class CheckPointDTO {

	public record CreateCheckPointDTO(
		String name,
		Integer subKey,
		Integer crawlIndex,
		Type type,
		String updatedAt,
		Status status
	){
	}
}
