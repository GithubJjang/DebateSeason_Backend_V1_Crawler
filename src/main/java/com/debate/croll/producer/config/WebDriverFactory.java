package com.debate.croll.producer.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.community.list.Community;
import com.debate.croll.producer.crawler.community.url.CommunityUrlList;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class WebDriverFactory {

	private final ChromeOptions options; // 빈 주입을 한다.
	public WebDriver getWebDriver(){
		return new ChromeDriver(options);
	}

}
