package com.debate.croll.producer.crawler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.request.ErrorDTO;
import com.debate.croll.producer.crawler.service.CheckPointService;
import com.debate.croll.producer.crawler.service.ErrorService;
import com.debate.croll.producer.crawler.manager.CrawlerRunner;
import com.debate.croll.producer.crawler.type.OriginClass;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.monitor.FailCounter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

	private final CrawlerRunner crawlerRunner;

	private final CheckPointService checkPointService;
	private final ErrorService errorService;

	//@Scheduled(initialDelay = 15000,fixedDelay = 86400000)
	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	//@Scheduled(cron = "0 18 13 * * ?", zone = "Asia/Seoul")
	//@Scheduled(fixedDelay = 86400000)
	public void crawl(){

		try{
			// 1. 커뮤니티 크롤링
			crawlerRunner.startCommunityCrawler();

			// 2. 뉴스 크롤링
			 crawlerRunner.startNewsCrawler();

			// 3. 마지막 체크포인트 업데이트
			 checkPointService.updateLastCheckPoint();

		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			ErrorDTO.CreateErrorDTO errorDTO = new ErrorDTO.CreateErrorDTO(
				OriginClass.SCHEDULER,
				Type.DRIVER,
				null,
				e.getClass().getName(),
				arr[0],
				null,
				LocalDateTime.now().toString()
			);

			errorService.save(errorDTO);

			//
			FailCounter.count();


		}

	}

}
