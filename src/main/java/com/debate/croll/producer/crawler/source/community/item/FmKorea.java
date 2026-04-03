package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.ErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.producer.service.CrawlerApplicationService;
import com.debate.croll.producer.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.monitor.util.FailCounter;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class FmKorea extends AbstractCommunitySource {


	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final WebDriverFactory webDriverFactory;

	private final String name = CommunityNameList.FmKorea.name();

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

			for (int i = start; i <= CommunityConfig.COMMUNITY_CRAWL_LIMIT; i++) {
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

		try{

			WebElement titleElement = driver.findElement(By.cssSelector(
				"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i + ") > div > h3 > a"));
			WebElement timeElement = driver.findElement(By.cssSelector(
				"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i
					+ ") > div > div:nth-child(5) > span.regdate"));

			String src = null;// 이미지

			// 이미지가 null이면 null인 상태로 넘어간다.
			try {
				WebElement imgElement = driver.findElement(By.cssSelector(
					"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i
						+ ") > div > a:nth-child(2) > img"));

				src = imgElement.getAttribute("src");
			} catch (NoSuchElementException e) {// 이미지가 없는 경우, NoSuchElementException 발생.

			}

			String timeString = LocalDate.now() + " " + timeElement.getText();

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);

			String title = titleElement.getText();
			String href = titleElement.getAttribute("href");

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				src,
				"정치",
				CommunityNameList.FmKorea.getName(),
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
