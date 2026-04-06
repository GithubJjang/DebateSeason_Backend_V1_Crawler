package com.debate.croll.producer.crawler.manager;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.infrastructure.service.CheckPointService;
import com.debate.croll.producer.crawler.common.Type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CrawlerRestartManager {

	private final CheckPointService checkPointService;
	private final CrawlerRunner crawlerRunner;

	public void restartCrawler(){

		// 체크포인트 불러오기
		CheckPointProjection checkPointProjection = checkPointService.getMostRecentCheckPoint();

		if(checkPointProjection!=null){ // 체크포인트가 있다면, 실행

			// DONE이 최신 체크포인트라면, 이미 크롤링을 한 바퀴 다 돈 것을 의미한다. ->실행하지 않음.
			Status status = checkPointProjection.getStatus();

			if(status==Status.DONE){
				log.info("All Jobs done. because the checkpoint status is [DONE]");
				return;
			}

			Type type = checkPointProjection.getType();// Type 불러오기
			switch (type){
				case COMMUNITY -> crawlerRunner.rebootCommunityCrawler(checkPointProjection);
				case NEWS -> crawlerRunner.rebootNewsCrawler(checkPointProjection);
			}

			// 다 하고나서, 마지막 체크포인트를 등록한다.
			checkPointService.updateLastCheckPoint();

		}
		else{
			log.error("There is no CheckPoint");
		}

	}
}
