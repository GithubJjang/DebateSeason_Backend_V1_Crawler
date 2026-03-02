package com.debate.croll.crawler.community;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debate.croll.producer.config.WebDriverFactory;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.community.list.Community;
import com.debate.croll.producer.crawler.community.template.AbstractCommunityCrawl;
import com.debate.croll.producer.crawler.community.type.BobaeDream;
import com.debate.croll.producer.repository.ErrorRepository;
import com.debate.croll.producer.repository.MediaRepository;

@ExtendWith(MockitoExtension.class)// Mock을 실행하기 위한 세팅
public class CommunityTest { // 추상 클래스 AbstractCommunityCrawl을 테스트를 할 수 없으므로, 이를 상속한 객체를 대상으로 테스트를 진행한다.

	@Mock
	private MediaRepository mediaRepository;
	@Mock
	private ErrorRepository errorRepository;

	@Mock
	private WebDriverFactory webDriverFactory;


	@Test
	@DisplayName("커뮤니티 크롤링을 위한 객체 생성하기")
	public void createCommunity(){

		AbstractCommunityCrawl bobaeDream = new BobaeDream(
			mediaRepository, // DB의 media 테이블에 저장하기 위함.
			errorRepository,
			webDriverFactory
		);

	}

	@Test
	@DisplayName("커뮤니티 이름 반환하기")
	public void getCommunityName(){

		// Given
		AbstractCommunityCrawl bobaeDream = new BobaeDream(
			mediaRepository, // DB의 media 테이블에 저장하기 위함.
			errorRepository,
			webDriverFactory
		);

		// When
		assertThat(Community.BobaeDream.name()).isEqualTo(bobaeDream.getCommunityName());
	}

	@Test
	@DisplayName("Steady 상태(정상 상태)에서 크롤링하기")
	public void crawl() throws InterruptedException {

		// Given
		AbstractCommunityCrawl bobaeDream = spy(new BobaeDream(
			mediaRepository, // DB의 media 테이블에 저장하기 위함.
			errorRepository,
			webDriverFactory
		));

		// 외부 필드값
		Status status = Status.Steady;
		int defaultStart = 1;

		//When
		bobaeDream.crawl(status,1);

	}

}
