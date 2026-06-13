package com.debate.croll.producer.recovery;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerRecoveryLauncher { // 장애 복구를 하기 위해서, 실행이 되는 컴포넌트

	private final CrawlerRecoveryManager crawlerRecoveryManager;

	@EventListener(ApplicationReadyEvent.class)
	public void recovery(){

		log.info("Crawler Recovery System Activated!");
		crawlerRecoveryManager.restartCrawler();


	}
}
