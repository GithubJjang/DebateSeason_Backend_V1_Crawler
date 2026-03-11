package com.debate.croll.producer.crawler.request;

import java.time.LocalDateTime;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.entity.CheckPointEntity;

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
