package com.debate.croll.producer.crawler.dto.error;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

@Component
public class CrawlerErrorDtoFactory { // 싱글톤

	// 파라미터: Exception, OriginClass, Type, name, url
	public CrawlerErrorDTO createErrorDto(Exception exception, OriginClass originClass, Type type, String name, String url){

		String[] arr = exception.getMessage().split("\\n");

		return new CrawlerErrorDTO(
			originClass,
			type,
			name,
			exception.getClass().getName(),
			arr[0],
			url,
			LocalDateTime.now().toString()
		)
		;

	}
}
