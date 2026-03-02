package com.debate.croll.producer.crawler.community.type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.config.WebDriverFactory;
import com.debate.croll.producer.config.WebDriverRunner;
import com.debate.croll.producer.crawler.community.url.CommunityUrlList;
import com.debate.croll.producer.crawler.type.OriginClass;
import com.debate.croll.producer.entity.Error;
import com.debate.croll.producer.repository.ErrorRepository;
import com.debate.croll.producer.config.CommunityConfig;
import com.debate.croll.producer.crawler.community.list.Community;
import com.debate.croll.producer.crawler.common.DirectoryUrl;
import com.debate.croll.producer.entity.Media;
import com.debate.croll.producer.monitor.FailCounter;
import com.debate.croll.producer.repository.MediaRepository;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.common.Record;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.community.template.AbstractCommunityCrawl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class MlbPark extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	private final ErrorRepository errorRepository;
	private final WebDriverFactory webDriverFactory;

	private final String name = Community.MlbPark.name();

	private final String DirUrl = DirectoryUrl.CRAWLER_LOG_BASE_DIR+name;
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
		if(status.name().equals("Reboot")){
			start = point;
		}

		try{

			log.info("do crawling ~ ");

			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			for (int i = start; i <= loop; i++) {

				extractElement(driver,i);
				Thread.sleep(1500); // 의심을 피하기 위한 설정.
			}



		}
		catch (ArrayIndexOutOfBoundsException e1){
			log.info("다음 커뮤니티로 넘어갑니다.");
		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			Error error = Error.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(com.debate.croll.producer.crawler.type.Type.DRIVER)
				.name(name)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorRepository.save(error);

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

			WebElement webElement = driver.findElement(By.cssSelector(
				"#container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child("
					+ i + ")"));

			//String id = webElement.findElement(By.cssSelector("td:nth-child(1)")).getText();

			WebElement titleElement = webElement.findElement(By.cssSelector("td:nth-child(2) > a"));
			String title = titleElement.getText();

			String href = titleElement.getAttribute("href");

			//String date = webElement.findElement(By.cssSelector("td:nth-child(4) > span")).getText();

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			Media mlbPark = Media.builder()
				.title(title)
				.url(href)
				.src(null)// 이미지 원래 없음.
				.category("사회")
				.media("엠엘비파크")
				.type("community")
				.count(0)
				.createdAt(now.toString())
				.build();

			mediaRepository.save(mlbPark);

			Record record = new Record(name,i, Type.Community);
			record.recordFile();

		}
		catch (Exception e) {

			String[] arr = e.getMessage().split("\\n");

			Error error = Error.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(com.debate.croll.producer.crawler.type.Type.COMMUNITY)
				.name(name)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now().toString())
				.build();

			errorRepository.save(error);

			//
			FailCounter.count();

			//Sentry.captureException(e);

		}


	}

}
