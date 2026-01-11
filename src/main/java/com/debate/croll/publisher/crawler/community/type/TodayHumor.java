package com.debate.croll.publisher.crawler.community.type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import org.springframework.stereotype.Component;

import com.debate.croll.logger.common.crawler.OriginClass;
import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.config.CommunityConfig;
import com.debate.croll.publisher.crawler.common.CommunityName;
import com.debate.croll.publisher.crawler.common.DirectoryUrl;
import com.debate.croll.publisher.crawler.manager.FileManager;
import com.debate.croll.publisher.domain.entity.Media;
import com.debate.croll.publisher.monitor.FailCounter;
import com.debate.croll.publisher.repository.MediaRepository;
import com.debate.croll.publisher.crawler.common.Record;
import com.debate.croll.publisher.crawler.common.Status;
import com.debate.croll.publisher.crawler.common.Type;
import com.debate.croll.publisher.crawler.community.template.AbstractCommunityCrawl;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TodayHumor extends AbstractCommunityCrawl { // 에러발생

	private final MediaRepository mediaRepository;
	private final ChromeOptions options;

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	private final String name = CommunityName.TodayHumor.name();

	private final String DirUrl = DirectoryUrl.CRAWLER_LOG_BASE_DIR+name;
	private int start = 2;


	@PostConstruct
	public void init(){
		FileManager fileManager = new FileManager();
		fileManager.createDir(DirUrl );
	}

	@Override
	public String getCommunityName() {
		return this.name;
	}

	public void crawl(Status status,int point) throws InterruptedException {

		WebDriver driver = null;

		// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
		if(status.name().equals("Reboot")){
			start = point;
		}

		try{

			log.info("do crawling ~ ");

			driver = super.setWebDriver(options,"todayhumor");
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			// Dir 업데이트
			FileManager fileManager = new FileManager();
			fileManager.updateDirCreatedDate(DirUrl);

			for (int i = start; i < 2 + loop; i++) {

				extractElement(driver,i);
				Thread.sleep(1500); // 의심을 피하기 위한 설정.

			}

		}
		catch (ArrayIndexOutOfBoundsException e1){
			log.info("다음 커뮤니티로 넘어갑니다.");
		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(com.debate.croll.logger.common.crawler.Type.DRIVER)
				.name(name)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			//
			FailCounter.count();

			//Sentry.captureException(e);

		}
		finally {

			if (driver != null) {

				driver.quit();
				log.info("successfully shut driver");
				Thread.sleep(3000); // 의도적인 컨텍스트 스위칭 유발로, 다른 스레드 작업 처리를 위한 목적.
			}

		}



	}

	@Transactional
	public void extractElement(WebDriver driver,int i) {

		try{

			// body > div.whole_box > div > div > table > tbody > tr:nth-child(2)
			// body > div.whole_box > div > div > table > tbody > tr:nth-child(2) > td.subject > a

			WebElement webElement = driver.findElement(
				By.cssSelector("body > div.whole_box > div > div > table > tbody > tr:nth-child(" + i + ")"));

			WebElement hrefElement = webElement.findElement(By.cssSelector("td.subject > a"));
			String title = hrefElement.getText();
			String url = hrefElement.getAttribute("href");

			String img = null; // 이미지 null 처리

			// 25/05/06, 16:08 -> 25-05-06 16:08
			String dateElement = webElement.findElement(By.cssSelector("td.date")).getText();

			// 입력 포맷: yy/MM/dd HH:mm
			DateTimeFormatter inputFormatter =
				DateTimeFormatter.ofPattern("yy/MM/dd HH:mm");

			// LocalDateTime으로 바로 변환 (25/05/06 → 2025-05-06)
			LocalDateTime dateTime = LocalDateTime.parse(dateElement.trim(), inputFormatter);


			/* Legacy
			String[] datelist = dateElement.split(" ");// 25/05/06, 16:08 -> 25-05-06 16:08

			String dateparts = datelist[0];// 25/05/06

			String[] dateparts2 = dateparts.split("/");

			String year = "20" + dateparts2[0] + "-";// 2025-
			String month = dateparts2[1] + "-";// 05-
			String day = dateparts2[2] + " ";// 06

			// 2025-05-06(공백s)
			String joineddate = year + month + day;

			// 16:08
			String timeparts = datelist[1];// 16:08

			// 2025-05-06 16:08
			String date = joineddate + timeparts;

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

			 */

			Media todayHumor = Media.builder()
				.title(title)
				.url(url)
				.src(img)
				.category("사회")
				.media("오늘의유머")
				.type("community")
				.count(0)
				.createdAt(dateTime)
				.build();

			mediaRepository.save(todayHumor);

			Record record = new Record(name,i, Type.Community);
			record.recordFile();


		}
		catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(com.debate.croll.logger.common.crawler.Type.COMMUNITY)
				.name(name)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			//
			FailCounter.count();

			//Sentry.captureException(e);

		}
	}
}
