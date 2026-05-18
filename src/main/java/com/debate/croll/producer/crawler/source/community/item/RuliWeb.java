package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.infrastructure.service.CrawlerApplicationService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RuliWeb extends AbstractCommunitySource {

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.RuliWeb.name();
	private int start = 1;

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
			for(int i=start; i<=CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++){
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
				Thread.sleep(3000); // 의도적인 컨텍스트 스위칭 유발로, 다른 스레드 작업 처리를 위한 목적.
				log.info("successfully shut driver");
			}

		}



	}

	@Transactional
	public void extractElement(WebDriver driver,int i) {

		String href = null;

		try {

			// href & title
			WebElement element1 = driver.findElement(
				By.cssSelector("#best_body > table > tbody > tr:nth-child(" + i + ")")); // #best_body > table > tbody > tr:nth-child(1) > td.subject > a

			WebElement titleElement = element1.findElement(
				By.cssSelector("tr:nth-child(" + i + ") > td.subject > a"));

			href = titleElement.getAttribute("href");
			String title = titleElement.getText();

			// time
			WebElement timeElement = driver.findElement(By.cssSelector("td.time")); // #best_body > table > tbody > tr:nth-child(1) > td.time

			// 1) 화면에서 시간 문자열 가져오기 (예: "12:36")
			String timeText = timeElement.getText().trim();  // "12:36" 가정

			// 2) "HH:mm" 형식으로 파싱
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime parsedTime = LocalTime.parse(timeText, timeFormatter);  // 12:36

			// 3) 오늘 날짜 + 파싱한 시간으로 LocalDateTime 생성
			LocalDate today = LocalDate.now();   // 필요하면 ZoneId.of("Asia/Seoul") 고려
			LocalDateTime localDateTime = LocalDateTime.of(today, parsedTime);

			// 4) 초(second)는 지금 기준으로 맞추고 싶으면 이렇게
			int second = LocalTime.now().getSecond();
			localDateTime = localDateTime
				.withSecond(second)
				.withNano(0);

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				null,
				"정치",
				CommunityNameList.RuliWeb.getName(),
				Type.COMMUNITY.getName(),
				0,
				localDateTime.toString()
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
