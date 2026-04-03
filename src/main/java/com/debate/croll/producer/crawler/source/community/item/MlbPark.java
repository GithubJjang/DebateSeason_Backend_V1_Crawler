package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.ErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.infrastructure.service.CrawlerApplicationService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MlbPark extends AbstractCommunitySource {

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final WebDriverFactory webDriverFactory;

	private final String name = CommunityNameList.MlbPark.name();
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

			WebElement webElement = driver.findElement(By.cssSelector(
				"#container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child("
					+ i + ")"));

			//String id = webElement.findElement(By.cssSelector("td:nth-child(1)")).getText();

			WebElement titleElement = webElement.findElement(By.cssSelector("td:nth-child(2) > a"));
			String title = titleElement.getText();

			String href = titleElement.getAttribute("href");

			//String date = webElement.findElement(By.cssSelector("td:nth-child(4) > span")).getText();

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
				title,
				href,
				null,
				"정치",
				CommunityNameList.MlbPark.getName(),
				Type.COMMUNITY.getName(),
				0,
				now.toString()
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
		catch (Exception e) {

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

			//Sentry.captureException(e);

		}


	}

}
