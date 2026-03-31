package com.debate.croll.producer.crawler.manager;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerBootstrap { // 장애 복구를 하기 위해서, 실행이 되는 컴포넌트

	private final CrawlerRestartManager crawlerRestartManager;

	//@EventListener(ApplicationReadyEvent.class)
	public void recovery(){

		log.info("Crawler Recovery On");
		crawlerRestartManager.restartCrawler();


	}
}
