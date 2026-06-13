package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
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

import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.item.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Ppompu extends AbstractCommunitySource {

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.Ppompu.name();

	@Override
	public String getCommunityName() {
		return this.name;
	}

	public void crawl(int startPoint){

		// WebDriver 객체 생성
		WebDriverRunner runner = new WebDriverRunner();

		WebDriver driver = webDriverFactory.getWebDriver();
		String url = CommunityUrlList.getUrl(name); // readOnly이기 때문에 thread-safe핟.

		runner.run(driver,url);

		try{
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			extractElement(driver,startPoint);
		}
		catch (Exception exception){

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER,
				Type.DRIVER,
				name,
				null);

			errorService.save(errorDTO);

			//Sentry.captureException(e);

		}
		finally {
			if (driver != null) {
				driver.quit();
				log.info("successfully shut driver");
			}
		}
	}

	public void extractElement(WebDriver driver, int startPoint) {

		String href = null;
		String src = null;

		int crawlIndex = 0;
		if(startPoint!=-1){
			crawlIndex = startPoint;
		}

		List<WebElement> items;
		try {
			items = driver.findElements(By.cssSelector(".baseList.bbs_new1"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		while (crawlIndex<8) {

			try {
				WebElement e = items.get(crawlIndex);

				WebElement thumbElement = e.findElement(By.className("baseList-thumb"));
				href = thumbElement.getAttribute("href");

				WebElement imageElement = thumbElement.findElement(By.tagName("img"));
				src = imageElement.getAttribute("src");

				List<WebElement> titleElement = e.findElements(By.className("baseList-title"));
				String title = titleElement.get(1).getText();

				WebElement timeElement =
					e.findElements(
						By.cssSelector(".baseList-space.board_date")
					).get(0);
				String strTime = timeElement.getText();

				LocalDate today = LocalDate.now();
				LocalTime time = LocalTime.parse(strTime);

				LocalDateTime dateTime =
					LocalDateTime.of(today, time);

				MediaDTO mediaDTO = new MediaDTO(
					title,
					href,
					src,
					"정치",
					CommunityNameList.Ppompu.getName(),
					Type.COMMUNITY.getName(),
					0,
					dateTime.toString()
				);

				CheckPointDTO checkPointDTO = new CheckPointDTO(
					name,
					null,
					crawlIndex+1,
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
			finally {
				crawlIndex++;
				href = null;
				src = null;
			}

		}
	}
}
