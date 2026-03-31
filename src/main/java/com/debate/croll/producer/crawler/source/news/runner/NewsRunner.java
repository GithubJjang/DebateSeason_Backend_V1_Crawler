package com.debate.croll.producer.crawler.source.news.runner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.request.CheckPointDTO;
import com.debate.croll.producer.crawler.request.ErrorDTO;
import com.debate.croll.producer.crawler.request.MediaDTO;
import com.debate.croll.producer.crawler.service.CrawlerService;
import com.debate.croll.producer.crawler.service.ErrorService;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.monitor.FailCounter;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsRunner {

	private final CrawlerService crawlerService;
	private final ErrorService errorService;

	private final WebDriverFactory webDriverFactory;


	public void crawl(Status status, String url, String name, Integer categoryNumber, Integer index) {

		int startIndex = 1;

		WebDriverRunner runner = new WebDriverRunner();
		WebDriver driver = webDriverFactory.getWebDriver(); // 매번 새로운 드라이버를 제공받는다.

		// 1. driver 예외를 잡기 위한 처리.
		try{

			// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
			if(status.name().equals(Status.REBOOT.getName())){
				startIndex = index;
			}

			runner.run(driver,url + "?sid=" + categoryNumber.toString());

			for (int j = startIndex; j <= 2; j++) { // 범위를 넘어서면 실행이 되지 않는다.
				extractElement2(driver,name,categoryNumber,j);
				Thread.sleep(1500);// 분까지 겹치는 경우를 방지해서 일부러 1초 기다림
			}

		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			ErrorDTO.CreateErrorDTO errorDTO = new ErrorDTO.CreateErrorDTO(
				OriginClass.CRAWLER,
				Type.DRIVER,
				name,
				e.getClass().getName(),
				arr[0],
				null,
				LocalDateTime.now().toString()
			);

			errorService.save(errorDTO);

			//
			FailCounter.count();

		}
		finally {

			if (driver != null) {
				log.info("Done News Crawling ~ ");
				driver.quit();
			}

		}

	}


	public void extractElement2(WebDriver driver, String name, Integer categoryNumber, int childIndex){

		// i는 category 번호 : 100,101,102,104
		// 웹 페이지에서 Element를 가져올 때, 에러를 잡기 위한 설정.

		try{

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			// 1. Link
			WebElement aTag = driver.findElement(
				By.cssSelector("#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + childIndex
					+ ") > a.press_edit_news_link._es_pc_link"));
			String href = aTag.getAttribute("href");

			// 2. title
			WebElement titleElement = driver.findElement(By.cssSelector(
				"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + childIndex
					+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_text > span.press_edit_news_title"));
			String title = titleElement.getText();

			// 3. img
			String src;
			try {
				WebElement imgElement = driver.findElement(By.cssSelector(
					"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + childIndex
						+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_thumb > img"));
				src = imgElement.getAttribute("src");
			} catch (NoSuchElementException e) { // 없으면 image는 null이다.
				src = null;
			}

			// 4. time
			WebElement timeElement = driver.findElement(By.cssSelector(
				"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + childIndex
					+ ") > a > span.press_edit_news_text > span.r_ico_b.r_modify"));
			String outdpated = timeElement.getText();

			LocalDateTime time = null;
			// 분전(min),시간전(hour),일전(day)

			if (outdpated.contains("분전")) {
				outdpated = outdpated.replace("분전", "");
				time = now.minusMinutes(Integer.parseInt(outdpated));
			} else if (outdpated.contains("시간전")) {
				outdpated = outdpated.replace("시간전", "");
				time = now.minusHours(Integer.parseInt(outdpated));

			} else if (outdpated.contains("일전")) {
				outdpated = outdpated.replace("일전", "");
				time = now.minusDays(Integer.parseInt(outdpated));

			}

			// 카테고리 넣기
			// 정치 https://media.naver.com/press/422?sid=100
			// 경제 https://media.naver.com/press/422?sid=101
			// 사회 https://media.naver.com/press/422?sid=102
			// IT https://media.naver.com/press/422?sid=105
			String categoryName;

			categoryName = switch (categoryNumber) {
				case 100 -> "정치";
				case 101 -> "경제";
				case 102 -> "사회";
				case 104 -> "세계";
				default -> null; // 또는 "" / 필요하면 기본값
			};


			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				src,
				categoryName,
				name,
				Type.NEWS.getName(),
				0,
				time.toString()
			);

			CheckPointDTO.CreateCheckPointDTO checkPointDTO = new CheckPointDTO.CreateCheckPointDTO(
				name,
				categoryNumber,
				childIndex,
				Type.NEWS,
				LocalDateTime.now().toString(),
				Status.REBOOT
			);

			crawlerService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);

		}
		catch (Exception e){


			String[] arr = e.getMessage().split("\\n");

			ErrorDTO.CreateErrorDTO errorDTO = new ErrorDTO.CreateErrorDTO(
				OriginClass.CRAWLER,
				Type.NEWS,
				name,
				e.getClass().getName(),
				arr[0],
				null,
				LocalDateTime.now().toString()
			);

			errorService.save(errorDTO);


			FailCounter.count();

		}

	}
}
