package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.infrastructure.service.CrawlerApplicationService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TodayHumor extends AbstractCommunitySource { // 에러발생

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.TodayHumor.name();
	private int start = 2;

	@Override
	public String getCommunityName() {
		return this.name;
	}

	public void crawl(Status status,int point) throws InterruptedException {

		// WebDriver 객체 생성
		WebDriverRunner runner = new WebDriverRunner();

		WebDriver driver = webDriverFactory.getWebDriver();
		String url = CommunityUrlList.getUrl(name); // readOnly이기 때문에 thread-safe핟.

		runner.run(driver,url);

		// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
		if(status.name().equals(Status.REBOOT.getName())){
			start = point;
		}

		try{
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			for (int i = start; i < 2 + CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++) {
				extractElement(driver,i);
				Thread.sleep(1500); // 의심을 피하기 위한 설정.
			}
		}
		catch (Exception exception){

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER,
				Type.DRIVER,
				name,
				null);

			errorService.save(errorDTO);

		}
		finally {

			if (driver != null) {

				driver.quit();
				log.info("successfully shut driver");
				Thread.sleep(3000); // 의도적인 컨텍스트 스위칭 유발로, 다른 스레드 작업 처리를 위한 목적.
			}

		}



	}

	@Transactional
	public void extractElement(WebDriver driver,int i) {

		String href = null;

		try{
			// body > div.whole_box > div > div > table > tbody > tr:nth-child(2)
			// body > div.whole_box > div > div > table > tbody > tr:nth-child(2) > td.subject > a
			WebElement webElement = driver.findElement(
				By.cssSelector("body > div.whole_box > div > div > table > tbody > tr:nth-child(" + i + ")"));

			WebElement hrefElement = webElement.findElement(By.cssSelector("td.subject > a"));
			String title = hrefElement.getText();
			href = hrefElement.getAttribute("href"); // 주소 url

			String dateElement = webElement.findElement(By.cssSelector("td.date")).getText(); // 25/05/06, 16:08 -> 25-05-06 16:08

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm"); // 입력 포맷: yy/MM/dd HH:mm

			LocalDateTime dateTime = LocalDateTime.parse(dateElement.trim(), inputFormatter); // LocalDateTime으로 바로 변환 (25/05/06 → 2025-05-06)

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				null,
				"정치",
				CommunityNameList.TodayHumor.getName(),
				Type.COMMUNITY.getName(),
				0,
				dateTime.toString()
			);

			CheckPointDTO.CreateCheckPointDTO checkPointDTO = new CheckPointDTO.CreateCheckPointDTO(
				name,
				null,
				i,
				Type.COMMUNITY,
				LocalDateTime.now().toString(),
				Status.REBOOT
			);
			crawlerApplicationService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);
		}
		catch (Exception exception){

			boolean isUniqueConstraintViolation = exceptionClassfier.isUniqueConstraintViolation(exception);
			if(isUniqueConstraintViolation){ // 데이터 중복으로 발생한 에러라면, href를 저장하지 않는다. 그렇지 않다면, url이라도 저장해서 recovery를 해서 누락을 막자.
				href = null;
			}

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER,
				Type.COMMUNITY,
				name,
				href);

			errorService.save(errorDTO);

			//Sentry.captureException(e);

		}
	}
}
