package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDate;
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
public class FmKorea extends AbstractCommunitySource {


	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.FmKorea.name();

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
		String src = null;

		int crawlIndex = 0;
		if(startPoint!=-1){
			crawlIndex = startPoint;
		}

		List<WebElement> items;
		try{
			WebElement bestWidget = driver.findElement(
				By.cssSelector(".fm_best_widget._bd_pc")
			);

			items =
				bestWidget.findElements(By.cssSelector("ul li"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		while (crawlIndex<8) {

			try {

				WebElement e = items.get(crawlIndex);

				WebElement titleElement = e.findElement(By.className("title"));

				String title =
					titleElement.getAttribute("data-original-title");

				href = titleElement.findElement(By.tagName("a")).getAttribute("href");

				WebElement timeElement = e.findElement(By.className("regdate"));
				String timeString = LocalDate.now() + " " + timeElement.getText();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // Create a DateTimeFormatter with the appropriate pattern
				LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter); // Parse the string to a LocalDateTime object

				try{
					WebElement imgElement = e.findElement(By.tagName("img"));
					src = imgElement.getAttribute("src");
				}
				catch (NoSuchElementException noImageException){
					src = null;
				}


				MediaDTO mediaDTO = new MediaDTO(
					title,
					href,
					src,
					"정치",
					CommunityNameList.FmKorea.getName(),
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
			}
			finally {
				crawlIndex++;
				href=null;
				src = null;
			}
		}

	}
}
