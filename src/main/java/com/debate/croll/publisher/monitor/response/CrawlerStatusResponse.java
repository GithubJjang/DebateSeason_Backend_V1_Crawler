package com.debate.croll.publisher.monitor.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerStatusResponse {

	private ResponseState responseState; // progressLogMap로 변경
	private Map<String,String> logMap; // 로그 목록.
	private Map<String,Integer> exceptionTypeCountMap; // 예외 집계 맵.
	private CrawlerExecutionStats state;// 현재 상태.


}
