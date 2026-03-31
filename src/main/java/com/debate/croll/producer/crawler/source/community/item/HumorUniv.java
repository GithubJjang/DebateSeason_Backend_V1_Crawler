package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.monitor.FailCounter;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class HumorUniv extends AbstractCommunitySource {

	private final CrawlerService crawlerService;
	private final ErrorService errorService;

	private final WebDriverFactory webDriverFactory;

	private final int init = 3;

	private final String name = CommunityNameList.HumorUniv.name();
	private int start = 0;

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
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
			for (int i = start; i < CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++) {
				extractElement(driver,i);
				Thread.sleep(1500); // 의심을 피하기 위한 설정.
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
	public void extractElement(WebDriver driver,int i) {

		try {

			WebElement webElement = driver.findElement(
				By.cssSelector("#list_body > ul > a:nth-child(" + (init + i * 2) + ")"));

			//
			WebElement idElement = webElement.findElement(By.cssSelector("li"));
			String id = idElement.getAttribute("id");

			String numberOnly = id.replaceAll("[^0-9]", ""); // 숫자가 아닌 문자를 모두 제거
			String title = driver.findElement(By.cssSelector("#title_chk_pds-" + numberOnly)).getText();
			//#title_chk_pds-1366802

			String src = null;//이미지
			try{
				src = driver.findElement(
						By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(1) > div > img"))
						.getAttribute("src");
			}
			catch (NoSuchElementException e){

			}




			String time = driver.findElement(
				By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(2) > div > span.extra")).getText();

			// href
			String href = driver.findElement(
				By.cssSelector("#list_body > ul > a:nth-child(" + (init + i * 2) + ")")).getAttribute("href");

			// 2. 시간 부분만 추출
			String timePart = time.split(" ")[1]; // "07:31"

			// 3. 시:분 파싱
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime parsedTime = LocalTime.parse(timePart, timeFormatter);

			// 4. 현재 날짜에 시:분만 교체
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime updatedDateTime = now
				.withHour(parsedTime.getHour())
				.withMinute(parsedTime.getMinute())
				.withSecond(0)
				.withNano(0);

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				src,
				"정치",
				CommunityNameList.HumorUniv.getName(),
				Type.COMMUNITY.getName(),
				0,
				updatedDateTime.toString()
			);

			CheckPointDTO.CreateCheckPointDTO checkPointDTO = new CheckPointDTO.CreateCheckPointDTO(
				name,
				null,
				i,
				Type.COMMUNITY,
				LocalDateTime.now().toString(),
				Status.REBOOT
			);

			crawlerService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);

		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			ErrorDTO.CreateErrorDTO errorDTO = new ErrorDTO.CreateErrorDTO(
				OriginClass.CRAWLER,
				Type.COMMUNITY,
				name,
				e.getClass().getName(),
				arr[0],
				null,
				LocalDateTime.now().toString()
			);

			errorService.save(errorDTO);

			//
			FailCounter.count();

			//Sentry.captureException(e);
		}

	}

}
