package com.debate.croll.publisher.crawler.community.type;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
import com.debate.croll.publisher.crawler.common.Type;
import com.debate.croll.publisher.crawler.community.template.AbstractCommunityCrawl;
import com.debate.croll.publisher.crawler.common.Status;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class FmKorea extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	private final ChromeOptions options;

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;
	private LocalDate today;

	private final String name = CommunityName.FmKorea.name();

	private final String DirUrl = DirectoryUrl.CRAWLER_LOG_BASE_DIR+name;

	private int start = 1;

	@PostConstruct
	public void init(){
		FileManager fileManager = new FileManager();
		fileManager.createDir(DirUrl);
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

			driver = super.setWebDriver(options,"fmkorea");
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			// Dir 업데이트
			FileManager fileManager = new FileManager();
			fileManager.updateDirCreatedDate(DirUrl);

			// 오늘 YYYY-MM-DD
			today = LocalDate.now();

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

				today = null; // 크롤링 작업을 끝나고, 날짜를 갱신한다.

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

			String image = null;

			// 이미지가 null이면 null인 상태로 넘어간다.
			try {
				WebElement imgElement = driver.findElement(By.cssSelector(
					"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i
						+ ") > div > a:nth-child(2) > img"));

				image = imgElement.getAttribute("src");
			} catch (NoSuchElementException e) {// 이미지가 없는 경우, NoSuchElementException 발생.

			}

			String timeString = today + " " + timeElement.getText();

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);

			Media fmKorea = Media.builder()
				.title(titleElement.getText())
				.url(titleElement.getAttribute("href"))
				.src(image)
				.category("정치")
				.media("에펨코리아")
				.type("community")
				.count(0)
				.createdAt(dateTime)
				.build();

			mediaRepository.save(fmKorea);

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
