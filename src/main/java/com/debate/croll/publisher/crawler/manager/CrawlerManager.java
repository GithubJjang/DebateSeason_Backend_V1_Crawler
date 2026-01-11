package com.debate.croll.publisher.crawler.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import com.debate.croll.logger.common.crawler.OriginClass;
import com.debate.croll.logger.common.crawler.Type;
import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.crawler.common.Status;
import com.debate.croll.publisher.crawler.community.template.AbstractCommunityCrawl;
import com.debate.croll.publisher.crawler.community.type.BobaeDream;
import com.debate.croll.publisher.crawler.community.type.Clien;
import com.debate.croll.publisher.crawler.community.type.MlbPark;
import com.debate.croll.publisher.crawler.community.type.RuliWeb;
import com.debate.croll.publisher.crawler.community.type.TodayHumor;
import com.debate.croll.publisher.crawler.community.type.FmKorea;
import com.debate.croll.publisher.crawler.community.type.HumorUniv;
import com.debate.croll.publisher.crawler.community.type.Ppompu;
import com.debate.croll.publisher.crawler.news.NewsUrlList;
import com.debate.croll.publisher.crawler.news.News;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerManager {

	// 커뮤니티 크롤러 목록
	private final BobaeDream bobaeDream;
	private final Clien clien;
	private final FmKorea fmKorea;
	private final MlbPark mlbPark;
	private final RuliWeb ruliWeb;
	private final TodayHumor todayHumor;
	private final HumorUniv humorUniv;
	private final Ppompu ppompu;

	// 뉴스 크롤러 목록
	private final News news;
	private final NewsUrlList newsUrlList; // news에 해당하는 url

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;


	// 이거는 CrawlerRecoveryManager도 쓰기 때문에, CommunityList에서 초기화 -> Mananger로 주입하는 전략. null 회피.
	private final List<AbstractCommunityCrawl> communityCrawlList = new ArrayList<>();

	// 초기화
	@PostConstruct
	public void init(){

		communityCrawlList.add(bobaeDream);
		communityCrawlList.add(clien);
		communityCrawlList.add(fmKorea);
		communityCrawlList.add(humorUniv); //WAF -> 직접 주입
		communityCrawlList.add(mlbPark);
		communityCrawlList.add(ppompu); //네트워크 연결 문제 -> 직접 주입
		communityCrawlList.add(ruliWeb);
		communityCrawlList.add(todayHumor);




	}

	public void startCommunityCrawler() {

		try {
			// 1. 커뮤니티 크롤링
			log.info("Start Community Crawling ~ ");

			for (AbstractCommunityCrawl e : communityCrawlList) {

				e.crawl(Status.Steady, -1); // 정상적인 작동을 하므로, Steady라고 주입을 한다.

				Thread.sleep(5000); // 네트워크 폭주를 방지하기 위한 설정.

			}

			// Cool down
			Thread.sleep(10000);

		}
		catch (InterruptedException e) {

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER_MANAGER)
				.type(Type.COMMUNITY)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			Thread.currentThread().interrupt(); // 예상치 못한 스레드 종료 신호를 받을 때, interrupt flag를 바꾸어 종료될 수 있도록 해야함. 근데 이런 경우 거의 없는데...

		}
	}

	public void startNewsCrawler(){

		log.info("Start News Crawling ~ ");

		//
		List<String> pressNameList = newsUrlList.getPressNameList();
		LinkedHashMap<String,String> linkedNewsList = newsUrlList.getNewsList();
		List<Integer> category =  newsUrlList.getCategory();

		try{

			for(String pressName : pressNameList) {

				String url = linkedNewsList.get(pressName);

				for (Integer i : category) {

					news.crawl(Status.Steady,url,pressName,i,-1);
					Thread.sleep(1500);

				}
			}

		}
		catch (InterruptedException e) {

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER_MANAGER)
				.type(Type.NEWS)
				.name(null)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			Thread.currentThread().interrupt(); // 예상치 못한 스레드 종료 신호를 받을 때, interrupt flag를 바꾸어 종료될 수 있도록 해야함. 근데 이런 경우 거의 없는데...
		}
		catch (Exception unexpectedException){

			String[] arr = unexpectedException.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER_MANAGER)
				.type(Type.NEWS)
				.name(null)
				.exceptionClass(unexpectedException.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

		}
	}


	// 불필요한 getter의 남용을 막기 위해서, @Getter 아니라 직접 작성을 함.
	public List<AbstractCommunityCrawl> getCommunityCrawlList(){ // @Getter쓰면 모든 필드 변수에 대해서 다 생성됨.
		return this.communityCrawlList;
	}

	public News getNews(){
		return this.news;
	}

	public NewsUrlList getNewsUrlList(){
		return this.newsUrlList;
	}

}
