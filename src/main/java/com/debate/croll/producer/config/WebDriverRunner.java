package com.debate.croll.producer.config;

import org.openqa.selenium.WebDriver;

import com.debate.croll.producer.crawler.community.list.Community;
import com.debate.croll.producer.crawler.community.url.CommunityUrlList;

public class WebDriverRunner { // WebDriver 실행 클래스. 왜냐하면, 실행 로직이 중복 되어서 1개로 관리하기 위함이다.

	public void run(WebDriver driver,String url){

		try{
			driver.get(url);
		}
		catch (Exception e){
			throw new RuntimeException("Driver 객체 오류 발생!");
		}

	}

}
