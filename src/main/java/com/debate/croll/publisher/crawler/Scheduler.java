package com.debate.croll.publisher.crawler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.logger.common.crawler.OriginClass;
import com.debate.croll.logger.common.crawler.Type;
import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.crawler.manager.CrawlerManager;
import com.debate.croll.publisher.monitor.FailCounter;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

	private final CrawlerManager crawlerManager;
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	//@Scheduled(initialDelay = 15000,fixedDelay = 86400000)
	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	//@Scheduled(cron = "0 18 13 * * ?", zone = "Asia/Seoul")
	//@Scheduled(fixedDelay = 86400000)
	public void crawl(){

		try{
			// 1. 커뮤니티 크롤링
			crawlerManager.startCommunityCrawler();

			// 2. 뉴스 크롤링
			crawlerManager.startNewsCrawler();

		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.SCHEDULER)
				.type(Type.DRIVER)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			//
			FailCounter.count();


		}

	}

}
