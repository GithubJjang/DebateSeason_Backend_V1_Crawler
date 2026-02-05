package com.debate.croll.producer.crawler.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProgressLogFormatter {

	private String name;
	private String modifiedDate;

}
