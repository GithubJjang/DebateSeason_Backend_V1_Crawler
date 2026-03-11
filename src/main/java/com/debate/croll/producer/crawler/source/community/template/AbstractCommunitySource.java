package com.debate.croll.producer.crawler.source.community.template;

import org.openqa.selenium.WebDriver;

import com.debate.croll.producer.crawler.common.Status;

public abstract class AbstractCommunitySource { // 클래스의 일관성을 유지하기 위함.


	// 커뮤니티 이름 반환 하기.
	public abstract String getCommunityName();

	// 이거는 커뮤니티마다 세부 내용이 다 다르다.
	// state는 1) 정상 or 2) 리부트인지에 따라 다르다.
	public abstract void crawl(Status status,int point) throws InterruptedException;

	public abstract void extractElement(WebDriver driver,int i); // 실제 웹 브라우저에 접근해서 element를 가져온다.

}
