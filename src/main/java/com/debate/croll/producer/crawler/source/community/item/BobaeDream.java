package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


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
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;

import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BobaeDream extends AbstractCommunitySource { // 이미지 없음

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.BobaeDream.name();

	private int start = 1;

	@Override
	public String getCommunityName() {
		return this.name;
	}

	// 1. 정상적인 작동
	@Override
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

			for (int i = start; i <= CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++) { // 총 5회 실행을 하면서, 매번 필요한 요소를 찾는다.
				extractElement(driver,i);
				Thread.sleep(1500);
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

	// 가독성을 위해서 분리.
	@Transactional // 리부팅 시 복구를 위해서, 매 트랜잭션 순간마다 기록을 한다.
	public void extractElement(WebDriver driver,int i) {

		String href = null;

		try{

			WebElement webElement = driver.findElement(
				By.cssSelector("#boardlist > tbody > tr:nth-child(+" + i + ")"));

			WebElement titleElement = webElement.findElement(By.cssSelector("td.pl14 > a.bsubject"));
			href = titleElement.getAttribute("href");
			String title = titleElement.getText();
			String time = webElement.findElement(By.cssSelector("td.date")).getText();
			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			// 문자열에서 시와 분 파싱
			int hour = Integer.parseInt(time.split(":")[0]);
			int minute = Integer.parseInt(time.split(":")[1]);

			// 시:분만 21:42로 덮어쓰기, 초와 나노초는 유지
			LocalDateTime replaced = now.withHour(hour).withMinute(minute);

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				null,
				"사회",
				CommunityNameList.BobaeDream.getName(),
				Type.COMMUNITY.getName(),
				0,
				replaced.toString()
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

		} catch (Exception exception){

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
