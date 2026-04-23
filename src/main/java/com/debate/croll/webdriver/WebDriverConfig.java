package com.debate.croll.webdriver;

import java.util.Collections;
import java.util.Random;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.github.bonigarcia.wdm.WebDriverManager;

@Configuration
public class WebDriverConfig {

	@Bean
	@Scope("prototype")// 매번 새롭게 생성이 된다.
	public ChromeOptions chromeOptions(){

		// 공통 Options
		WebDriverManager.chromedriver()
			.setup();

		// 랜덤 User-Agent 리스트
		String[] userAgents = {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.85 Safari/537.36",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.70 Safari/537.36",
		};

		// 랜덤 User-Agent 선택
		Random rand = new Random();
		String userAgent = userAgents[rand.nextInt(userAgents.length)];

		ChromeOptions options = new ChromeOptions();

		//options.addArguments("--blink-settings=imagesEnabled=false"); 이미지 로딩 비활성화
		options.addArguments("--headless=new"); // headless 모드
		options.addArguments("--user-agent=" + userAgent); // 랜덤 User-Agent 설정
		options.addArguments("--no-sandbox"); // 추가적인 권한 설정
		options.addArguments("--disable-dev-shm-usage"); // Docker 환경에서 공유 메모리 부족 문제 해결
		options.addArguments("--remote-allow-origins=*"); // 원격 접속 허용을 하는 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)

		// webdriver임을 숨기는 설정(중요)
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);

		return options;
	}

}
