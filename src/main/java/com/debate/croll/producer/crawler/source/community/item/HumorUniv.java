package com.debate.croll.producer.crawler.source.community.item;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.item.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class HumorUniv extends AbstractCommunitySource {

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.HumorUniv.name();

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
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
			extractElement(driver,startPoint); // 호환을 위해서 유지.
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

		List<WebElement> webElements;

		try{
			webElements = driver.findElements(By.className("post_item"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		while (crawlIndex<8){

			try {
				WebElement e = webElements.get(crawlIndex);

				// 1. href
				WebElement rawHref = e.findElement(
					By.cssSelector("a.list_body_href.post_link")
				);
				href = rawHref.getAttribute("href");

				// 2. title
				WebElement titleSpan = e.findElement(
					By.cssSelector("span[id^='title_chk_pds']")
				);
				String title = titleSpan.getText(); // title 가져오기

				// 3. src
				WebElement rawSrc = e.findElement(By.className("img"));
				src = rawSrc.getAttribute("src");

				// 4. time
				WebElement rawTime = e.findElement(
					By.cssSelector("div > span.extra")
				);
				String timeText = rawTime.getText();

				String[] timeElements = timeText.split(" ");

				String dayOfTheWeek = timeElements[0];

				LocalDate today = LocalDate.now();
				LocalDate targetDate = null;

				DayOfWeek targetDay = null;

				if (dayOfTheWeek.contains("월")) {
					targetDay = DayOfWeek.MONDAY;
				} else if (dayOfTheWeek.contains("화")) {
					targetDay = DayOfWeek.TUESDAY;
				} else if (dayOfTheWeek.contains("수")) {
					targetDay = DayOfWeek.WEDNESDAY;
				} else if (dayOfTheWeek.contains("목")) {
					targetDay = DayOfWeek.THURSDAY;
				} else if (dayOfTheWeek.contains("금")) {
					targetDay = DayOfWeek.FRIDAY;
				} else if (dayOfTheWeek.contains("토")) {
					targetDay = DayOfWeek.SATURDAY;
				} else if (dayOfTheWeek.contains("일")) {
					targetDay = DayOfWeek.SUNDAY;
				}

				if (targetDay != null) {
					int diff = targetDay.getValue() - today.getDayOfWeek().getValue();
					targetDate = today.plusDays(diff);
				}

				String[] hm = timeElements[1].split(":");

				int hour = Integer.parseInt(hm[0]);
				int minute = Integer.parseInt(hm[1]);

				LocalDateTime dateTime =
					targetDate != null ? targetDate.atTime(hour, minute) : null;

				MediaDTO mediaDTO = new MediaDTO(
					title,
					href, // 링크
					src, // 이미지
					"정치",
					CommunityNameList.HumorUniv.getName(),
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
