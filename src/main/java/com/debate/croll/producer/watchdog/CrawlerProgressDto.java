package com.debate.croll.producer.watchdog;

import com.debate.croll.producer.crawler.common.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlerProgressDto {
	private Long id;
	private String updatedAt;
	private Status status;
}
