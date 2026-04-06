package com.debate.croll.producer.crawler.manager;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.mapper.checkpoint.CheckPointProjection;
import com.debate.croll.producer.crawler.source.community.config.CommunitySourceList;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.infrastructure.entity.ErrorEntity;
import com.debate.croll.infrastructure.repository.jpa.ErrorJpaRepository;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
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

	// 뉴스 크롤러 목록
	private final NewsRunner newsRunner;
	private final NewsUrlList newsUrlList;

	// 에러 처리를 위한 빈
	private final ErrorJpaRepository errorJpaRepository;

	public void startCommunityCrawler() {

		List<AbstractCommunitySource> communityCrawlList = communitySourceList.getSourceList();

		try {
			// 1. 커뮤니티 크롤링
			log.info("Start Community Crawling ~ ");

			for (AbstractCommunitySource e : communityCrawlList) {
				e.crawl(Status.STEADY, -1);
				Thread.sleep(5000); // 네트워크 폭주를 방지하기 위한 설정.
			}

			// Cool down
			Thread.sleep(10000);

		}
		catch (InterruptedException e) {

			String[] arr = e.getMessage().split("\\n");

			ErrorEntity errorEntity = ErrorEntity.builder()
				.OriginClass(OriginClass.CRAWLER_RUNNER)
				.type(Type.COMMUNITY)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorJpaRepository.save(errorEntity);

			Thread.currentThread().interrupt(); // 예상치 못한 스레드 종료 신호를 받을 때, interrupt flag를 바꾸어 종료될 수 있도록 해야함. 근데 이런 경우 거의 없는데...

		}
	}

	public void rebootCommunityCrawler(CheckPointProjection checkPointProjection){

		List<AbstractCommunitySource> communitySourcesList = communitySourceList.getSourceList();

		// int size = communitySourcesList.size();
		// int nextStartIndex = 0;

		boolean isLock = true;

		try{

			// 1.
			for (AbstractCommunitySource source : communitySourcesList) {

				// 타겟을 찾았다. -> 타겟의 끊어진 부분부터 시작해서 나머지 전체를 시작한다.
				if (source.getCommunityName().equals(checkPointProjection.getName())) {

					Status status = checkPointProjection.getStatus();//REBOOT
					source.crawl(status, checkPointProjection.getCrawlIndex()+1);// 다음꺼부터 크롤링하기
					// nextStartIndex++; // 다음을 겨누고 있어야 한다.
					// break;

					isLock = false; // isLock 해지 -> 다음꺼는 정상적으로 가져올 수 있다.
					continue; // 중복 실행을 막기 위해서, continue를 사용.

				}

				if(!isLock){
					source.crawl(Status.STEADY,-1);
					Thread.sleep(5000);
				}

				//nextStartIndex++;

			}

			// 2. 커뮤니티의 남은 부분 전체 실행하기
			// for(int i=nextStartIndex; i<size; i++){
			// 	AbstractCommunitySource source = communitySourcesList.get(i);
			// 	source.crawl(Status.STEADY,-1);
			// 	Thread.sleep(5000); // 네트워크 폭주를 방지하기 위한 설정.
			// }

			// 3. News는 정상적으로 실행하기
			this.startNewsCrawler();

		}
		catch (Exception e){

		}




	}

	public void startNewsCrawler(){

		log.info("Start News Crawling ~ ");

		//
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
		catch (InterruptedException e) {

			String[] arr = e.getMessage().split("\\n");

			ErrorEntity errorEntity = ErrorEntity.builder()
				.OriginClass(OriginClass.CRAWLER_RUNNER)
				.type(Type.NEWS)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorJpaRepository.save(errorEntity);

			Thread.currentThread().interrupt(); // 예상치 못한 스레드 종료 신호를 받을 때, interrupt flag를 바꾸어 종료될 수 있도록 해야함. 근데 이런 경우 거의 없는데...
		}
		catch (Exception unexpectedException){

			String[] arr = unexpectedException.getMessage().split("\\n");

			ErrorEntity errorEntity = ErrorEntity.builder()
				.OriginClass(OriginClass.CRAWLER_RUNNER)
				.type(Type.NEWS)
				.name(null)
				.exceptionClass(unexpectedException.getClass().getName())
				.message(arr[0])
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorJpaRepository.save(errorEntity);

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
}
