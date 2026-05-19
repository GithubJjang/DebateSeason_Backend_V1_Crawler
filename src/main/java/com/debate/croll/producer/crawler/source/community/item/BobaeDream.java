package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

			extractElement(driver,-1);
			Thread.sleep(1500);
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

	public void extractElement(WebDriver driver,int i) {

		String href = null;

		int count = 0;
		List<WebElement> rows;

		try{
			WebElement boardListWebElement = driver.findElement(By.cssSelector("#boardlist tbody"));
			rows = boardListWebElement.findElements(By.tagName("tr"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		for(WebElement td:rows){

			if(count>=8){
				break;
			}

			try {

				WebElement titleElement = td.findElement(By.className("bsubject"));
				String title = titleElement.getText();
				href = titleElement.getAttribute("href");

				WebElement dateElement = td.findElement(By.className("date"));
				String time = dateElement.getText();

				LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

				// 문자열에서 시와 분 파싱
				int hour = Integer.parseInt(time.split(":")[0]);
				int minute = Integer.parseInt(time.split(":")[1]);

				// 시:분만 21:42로 덮어쓰기, 초와 나노초는 유지
				LocalDateTime replaced = now.withHour(hour).withMinute(minute);

				MediaDTO mediaDTO = new MediaDTO(
					title,
					href,
					null,
					"사회",
					CommunityNameList.BobaeDream.getName(),
					Type.COMMUNITY.getName(),
					0,
					replaced.toString()
				);

				CheckPointDTO checkPointDTO = new CheckPointDTO(
					name,
					null,
					i,
					Type.COMMUNITY,
					LocalDateTime.now().toString(),
					Status.REBOOT
				);

				crawlerApplicationService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);// @Transactional 걸어서 media & checkpoint 안전하게 저장

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
			finally {
				count++;
				href=null;
			}
		}


	}

}
