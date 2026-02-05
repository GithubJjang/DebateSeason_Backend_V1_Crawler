package com.debate.croll.producer.crawler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.entity.Error;
import com.debate.croll.producer.repository.ErrorRepository;
import com.debate.croll.producer.crawler.manager.CrawlerManager;
import com.debate.croll.producer.crawler.type.OriginClass;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.monitor.FailCounter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

	private final CrawlerManager crawlerManager;
	private final ErrorRepository errorRepository;

	//@Scheduled(initialDelay = 15000,fixedDelay = 86400000)
	//@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
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

			Error error = Error.builder()
				.OriginClass(OriginClass.SCHEDULER)
				.type(Type.DRIVER)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorRepository.save(error);

			//
			FailCounter.count();


		}

	}

}
