package com.debate.croll.crawler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debate.croll.producer.crawler.CrawlerScheduler;
import com.debate.croll.producer.crawler.manager.CrawlerRunner;
import com.debate.croll.producer.repository.ErrorRepository;

@ExtendWith(MockitoExtension.class)// Mock을 실행하기 위한 세팅
public class CrawlerSchedulerTest {

	@Mock
	private CrawlerRunner crawlerRunner;
	@Mock
	private ErrorRepository errorRepository; // 에러를 저장하기 위한 레포지토리.
	@InjectMocks
	private CrawlerScheduler crawlerScheduler; // Mock을 주입한다.

	@Test
	@DisplayName("크롤러용 스케줄러 실행")
	public void crawl(){

		// scheduler.crawl을 실행하면,
		crawlerScheduler.crawl();

		// crawl() 메소드 내부에 아래 메소드들이 실제로 실행이 되는지 확인을 한다.
		verify(crawlerRunner).startCommunityCrawler();
		verify(crawlerRunner).startNewsCrawler();

	}

}
