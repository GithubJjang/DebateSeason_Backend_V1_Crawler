package com.debate.croll.publisher.crawler.manager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
