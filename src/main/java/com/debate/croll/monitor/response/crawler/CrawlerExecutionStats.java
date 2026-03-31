package com.debate.croll.monitor.response.crawler;

import java.util.List;

import com.debate.croll.monitor.response.error.ErrorFormatMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrawlerExecutionStats {

	private int success;
	private int error;
	private int total;
	//private String progress;

	private List<ErrorFormatMapper> errorFormatMapperList;

}
