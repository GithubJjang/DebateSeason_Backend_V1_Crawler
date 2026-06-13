package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.item.template.AbstractCommunitySource;
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
import com.debate.croll.producer.crawler.common.Status;

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

		}
		finally {
			if (driver != null) {
				driver.quit();
				log.info("successfully shut driver");
			}
		}
	}

	public void extractElement(WebDriver driver,int startPoint) {

		String href = null;

		int crawlIndex = 0;
		if(startPoint!=-1){
			crawlIndex = startPoint;
		}

		List<WebElement> boardListElement;
		try{
			boardListElement = driver.findElements(By.cssSelector("tr[class^=\"view list_tr\"]"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		while (crawlIndex<8){

			try {
				WebElement e =boardListElement.get(crawlIndex);

				WebElement subjectElement = e.findElement(By.className("subject"));
				WebElement titleElement = subjectElement.findElement(By.tagName("a"));

				String title = titleElement.getText();
				href = titleElement.getAttribute("href");

				WebElement dateElement = e.findElement(By.className("date"));
				String strTime = dateElement.getText();

				DateTimeFormatter formatter =
					DateTimeFormatter.ofPattern("yy/MM/dd HH:mm");

				LocalDateTime dateTime =
					LocalDateTime.parse(strTime, formatter);

				MediaDTO mediaDTO = new MediaDTO(
					title,
					href,
					null,
					"정치",
					CommunityNameList.TodayHumor.getName(),
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
				href=null;
			}
		}

	}
}
