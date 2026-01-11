package com.debate.croll.logger.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;

public class CrawlerErrorProcessor {

	public Map<String,Integer> countExceptionClass(List<CrawlerErrorEvent> crawlerErrorEventList){

		// 이걸 통해서 어떤 에러가 빈도수가 높은지 알 수 있으며, 이를 통해서 조치를 취할 수 있다.
		// ex) NoSuchElementException -> IP 차단?, 페이지 element 변경?

		Map<String,Integer> exceptionTypeCountMap = new HashMap<>();

		for(CrawlerErrorEvent c : crawlerErrorEventList){

			String exceptionName = c.getExceptionClass();

			if(exceptionTypeCountMap.get(exceptionName)==null){ // exceptionName이 없으면 새로 추가하고,
				exceptionTypeCountMap.put(exceptionName,1);
			}
			else{ // exceptionName이 이미 있으면, 기존 값 +1

				Integer value = exceptionTypeCountMap.get(exceptionName);
				exceptionTypeCountMap.put(exceptionName,value+1);
			}

		}

		return exceptionTypeCountMap;

	}
}
