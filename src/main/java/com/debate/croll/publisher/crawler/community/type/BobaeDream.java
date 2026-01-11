package com.debate.croll.publisher.crawler.community.type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

import org.springframework.stereotype.Component;

import com.debate.croll.logger.common.crawler.Type;
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
import com.debate.croll.publisher.crawler.community.template.AbstractCommunityCrawl;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BobaeDream extends AbstractCommunityCrawl {

	//
	private final MediaRepository mediaRepository;
	private final ChromeOptions options;

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	private final String name = CommunityName.BobaeDream.name();
	private final String DirUrl = DirectoryUrl.CRAWLER_LOG_BASE_DIR+name;
	private int start = 1;

	// 디렉토리 생성 초기화를 하자.
	@PostConstruct
	public void init(){
		FileManager fileManager = new FileManager();
		fileManager.createDir(DirUrl);
	}

	@Override
	public String getCommunityName() {
		return this.name;
	}

	// 1. 정상적인 작동
	@Override
	public void crawl(Status status,int point) throws InterruptedException {

		WebDriver driver = null;

		// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
		if(status.name().equals("Reboot")){
			start = point;
		}

		try{

			log.info("do crawling ~ ");

			driver = super.setWebDriver(options,"bobaedream"); // 부모 클래스의 기능을 사용한다.
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			// Dir 업데이트
			FileManager fileManager = new FileManager();
			fileManager.updateDirCreatedDate(DirUrl);

			for (int i = start; i <= loop; i++) { // 총 5회 실행을 하면서, 매번 필요한 요소를 찾는다.
				extractElement(driver,i);
				Thread.sleep(1500); // bot 의심 피하기
			}

		}
		catch (ArrayIndexOutOfBoundsException e1){
			log.info("다음 커뮤니티로 넘어갑니다.");
		}
		catch (Exception e){

			//
			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(Type.DRIVER)
				.name(name)
				.exceptionClass(e.getClass().getName())
				.message(arr[0])
				.stackTrace(null)
				.createdAt(LocalDateTime.now())
				.build();

			crawlerErrorEventRepository.save(crawlerErrorEvent);

			//
			FailCounter.count();

		}
		finally {

			if (driver != null) {

				driver.quit();
				Thread.sleep(3000); // 의도적인 컨텍스트 스위칭 유발로, 다른 스레드 작업 처리를 위한 목적.
				log.info("successfully shut driver");

			}

		}

	}

	// 가독성을 위해서 분리.
	@Transactional // 리부팅 시 복구를 위해서, 매 트랜잭션 순간마다 기록을 한다.
	public void extractElement(WebDriver driver,int i) {

		try{

			WebElement webElement = driver.findElement(
				By.cssSelector("#boardlist > tbody > tr:nth-child(+" + i + ")"));

			WebElement titleElement = webElement.findElement(By.cssSelector("td.pl14 > a.bsubject"));

			String title = titleElement.getText();
			String href = titleElement.getAttribute("href");

			String time = webElement.findElement(By.cssSelector("td.date")).getText();

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			// 문자열에서 시와 분 파싱
			int hour = Integer.parseInt(time.split(":")[0]);
			int minute = Integer.parseInt(time.split(":")[1]);

			// 시:분만 21:42로 덮어쓰기, 초와 나노초는 유지
			LocalDateTime replaced = now.withHour(hour).withMinute(minute);

			Media boBaeDream = Media.builder()
				.title(title)
				.url(href)
				.src(null)
				.category("사회")
				.media("보배드림")
				.type("community")
				.count(0)
				.createdAt(replaced)
				.build();

			mediaRepository.save(boBaeDream);

			Record record = new Record(name,i, com.debate.croll.publisher.crawler.common.Type.Community);
			record.recordFile();

		} catch (Exception e){

			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(Type.COMMUNITY)
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
