package com.debate.croll.producer.crawler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.common.RandomDelay;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.producer.crawler.source.community.config.CommunitySourceList;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.source.community.item.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.source.news.config.NewsUrlList;
import com.debate.croll.producer.crawler.source.news.runner.NewsRunner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerRunner {

	// 커뮤니티 Source 목록 리스트
	private final CommunitySourceList communitySourceList;
	private final RandomDelay randomDelay;

	// 뉴스 크롤러 목록
	private final NewsRunner newsRunner;
	private final NewsUrlList newsUrlList;

	// 에러 처리를 위한 빈
	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final ErrorService errorService;


	public void startCommunityCrawler() { // 1. 커뮤니티 크롤링

		List<AbstractCommunitySource> communityCrawlList = communitySourceList.getSourceList();
		try {
			log.info("Start Community Crawling ~ ");
			for (AbstractCommunitySource source : communityCrawlList) {
				source.crawl(-1);
				Thread.sleep(randomDelay.getCommunityCrawlerDelay()); // 네트워크 폭주를 방지하기 위한 설정.
			}
			// Cool down
			Thread.sleep(10000);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // 스레드 무한 루프 탈출을 위해서, 인터럽트 예외를 처리한다.
		}
	}

	public void rebootCommunityCrawler(CheckPointProjection checkPointProjection){

		List<AbstractCommunitySource> communitySourcesList = communitySourceList.getSourceList();

		boolean isLock = true;

		try{
			// 1. 커뮤니티 크롤링 작업 복구.
			for (AbstractCommunitySource source : communitySourcesList) {
				// 타겟을 찾았다. -> 작업 중단 지점 이후부터 작업을 실행한다.
				if (source.getCommunityName().equals(checkPointProjection.getName())) {

					source.crawl(checkPointProjection.getCrawlIndex());// 다음꺼부터 크롤링하기

					isLock = false; // isLock 해지 -> 다음꺼는 정상적으로 가져올 수 있다.
					continue; // 중복 실행을 막기 위해서, continue를 사용.
				}
				if(!isLock){
					source.crawl(-1);
					Thread.sleep(randomDelay.getCommunityCrawlerDelay());
				}
			}
			// 2. 뉴스 크롤링 작업 정상 진행.
			this.startNewsCrawler();
		}
		catch (InterruptedException e){
			Thread.currentThread().interrupt(); // 스레드 무한 루프 탈출을 위해서, 인터럽트 예외를 처리한다.
		}
	}

	public void startNewsCrawler(){

		//
		log.info("Start News Crawling ~ ");

		List<String> pressNameList = newsUrlList.getNewsNameList();
		LinkedHashMap<String,String> linkedNewsList = newsUrlList.getNewsList();
		List<Integer> category =  newsUrlList.getCategory();

		try{

			for(String pressName : pressNameList) {

				String url = linkedNewsList.get(pressName);

				for (Integer i : category) {

					newsRunner.crawl(Status.STEADY,url,pressName,i,-1);
					Thread.sleep(2000);

				}
			}

		}
		catch (InterruptedException exception) {

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER_RUNNER,
				Type.COMMUNITY,
				null,
				null);

			errorService.save(errorDTO);
		}
	}

	public void rebootNewsCrawler(CheckPointProjection checkPointProjection){

		// 1. 중단된 단건에 대한 메타데이터
		String name = checkPointProjection.getName(); // MBC, KBS, SBS 등...

		LinkedHashMap<String,String> urlLinkedHashMap = newsUrlList.getNewsList();
		String url = urlLinkedHashMap.get(name);

		Status status = checkPointProjection.getStatus(); // 상태값.

		Integer subKey = checkPointProjection.getSubKey(); // 101(카테고리 넘버)
		Integer crawIndex = checkPointProjection.getCrawlIndex();// 1,2

		List<Integer> categoryNumberList = newsUrlList.getCategory();// 100,101,102,104

		// 2.
		boolean start = false;
		Set<String> pressNameSet =  urlLinkedHashMap.keySet();

		for(String s : pressNameSet){// MBC, SBS, KBS ...

			if(s.equals(name)){ // 2. 중단된 단건에 대해서 실행

				newsRunner.crawl(status,url,name,subKey,crawIndex+1); // 101번 다 긁어옴.

				for(Integer category : categoryNumberList){ // 102, 104번, ...
					if(subKey < category){
						newsRunner.crawl(Status.STEADY,url,name,category,-1); // 정상적으로 가져온다.
					}
				}
				start = true;
			}
			else if(start){ // 중단된 지점 이후, 나머지 언론사들의 크롤링을 정상적으로 수행한다.

				for(Integer category : categoryNumberList){
					newsRunner.crawl(Status.STEADY,urlLinkedHashMap.get(s),s,category,-1);
				}

			}
		}
	}

	public void retryFailedRequests(){

	}
}
