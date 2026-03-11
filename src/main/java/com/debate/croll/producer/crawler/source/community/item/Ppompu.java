package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.request.CheckPointDTO;
import com.debate.croll.producer.crawler.request.ErrorDTO;
import com.debate.croll.producer.crawler.request.MediaDTO;
import com.debate.croll.producer.crawler.service.CrawlerService;
import com.debate.croll.producer.crawler.service.ErrorService;
import com.debate.croll.producer.crawler.type.Type;
import com.debate.croll.producer.webdriver.WebDriverFactory;
import com.debate.croll.producer.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.type.OriginClass;
import com.debate.croll.producer.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.common.DirectoryUrl;
import com.debate.croll.producer.entity.MediaEntity;
import com.debate.croll.producer.monitor.FailCounter;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Ppompu extends AbstractCommunitySource {

	private final CrawlerService crawlerService;
	private final ErrorService errorService;

	private final WebDriverFactory webDriverFactory;

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

			log.info("do crawling ~ ");

			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			for(int i=start; i<4+loop; i++){
				extractElement(driver,i);
				//Thread.sleep(1000);
			}

		}
		catch (ArrayIndexOutOfBoundsException e1){
			log.info("다음 커뮤니티로 넘어갑니다.");
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
	public void extractElement(WebDriver driver, int i) {

		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(4)
		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(5)

		try {
			//#revolution_main_table > tbody > tr:nth-child(11) > td:nth-child(2) > img.baseList-img
			WebElement e = driver.findElement(By.cssSelector("body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child("+i+")"));

			//WebElement imageElement = e.findElement(By.cssSelector("td.baseList-space.title > a > img"));
			//String image = imageElement.getAttribute("src") != null ? imageElement.getAttribute("src") : null;

			String href = e.findElement(By.cssSelector("td.baseList-space.title > a")).getAttribute("href");

			String title = e.findElement(By.cssSelector("td.baseList-space.title > div > div > a:nth-child(2)")).getText();

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
				null,
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
