package com.debate.croll.producer.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class WebDriverFactory {

	private final ChromeOptions options; // 빈 주입을 한다.
	public WebDriver getWebDriver(){
		return new ChromeDriver(options);
	}

}
