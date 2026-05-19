package com.debate.croll.producer.crawler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.infrastructure.service.CheckPointService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.producer.crawler.manager.CrawlerRunner;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CrawlerScheduler {

	private final CrawlerRunner crawlerRunner;
	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;

	private final CheckPointService checkPointService;
	private final ErrorService errorService;

	//@Scheduled(initialDelay = 15000,fixedDelay = 86400000)
	//@Scheduled(cron = "0 18 13 * * ?", zone = "Asia/Seoul")
	@Scheduled(fixedDelay = 86400000)
	//@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	public void crawl(){

		// 1. 커뮤니티 크롤링
		crawlerRunner.startCommunityCrawler();

		// 2. 뉴스 크롤링
		crawlerRunner.startNewsCrawler();

		// 3. 마지막 체크포인트 업데이트
		checkPointService.updateLastCheckPoint();

	}

}
