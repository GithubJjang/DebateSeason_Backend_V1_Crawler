package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;

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
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
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

	private int start = 4;

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
			for(int i=start; i<4+CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++){
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

			//Sentry.captureException(e);

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
	public void extractElement(WebDriver driver, int i) {

		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(4)
		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(5)

		String href = null;

		try {
			//#revolution_main_table > tbody > tr:nth-child(11) > td:nth-child(2) > img.baseList-img
			WebElement e = driver.findElement(By.cssSelector("body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child("+i+")"));

			// href & title
			href = e.findElement(By.cssSelector("td.baseList-space.title > a")).getAttribute("href");
			String title = e.findElement(By.cssSelector("td.baseList-space.title > div > div > a:nth-child(2)")).getText();

			// image
			String src = null;
			try{
				WebElement imageElement = e.findElement(By.cssSelector("td.baseList-space.title > a > img"));
				src = imageElement.getAttribute("src") != null ? imageElement.getAttribute("src") : null;
			}
			catch (NoSuchElementException exception){

			}

			// time
			String beforeTime = e.findElement(By.cssSelector("td:nth-child(5)")).getText();

			// time 가공
			LocalDateTime now = LocalDateTime.now().withNano(0);

			// beforeTime 파싱 (hh:mm:ss)
			String[] parts = beforeTime.split(":");
			int hh = Integer.parseInt(parts[0]);
			int mm = Integer.parseInt(parts[1]);
			int ss = Integer.parseInt(parts[2]);

			// 날짜는 today 유지, 시간만 교체
			LocalDateTime localDateTime = now
				.withHour(hh)
				.withMinute(mm)
				.withSecond(ss);

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				src,
				"정치",
				CommunityNameList.Ppompu.getName(),
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
