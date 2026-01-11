package com.debate.croll.publisher.crawler.reboot;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerBootstrap { // 장애 복구를 하기 위해서, 실행이 되는 컴포넌트

	private final CrawlerRecoveryManager crawlerRecoveryManager;

	@EventListener(ApplicationReadyEvent.class)
	public void recovery(){

		log.info("Crawler Recovery On");

		crawlerRecoveryManager.rebootCrawl();
	}
}
