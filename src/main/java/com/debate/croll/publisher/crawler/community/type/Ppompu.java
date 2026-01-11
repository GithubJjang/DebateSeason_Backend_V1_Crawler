package com.debate.croll.publisher.crawler.community.type;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import com.debate.croll.publisher.crawler.common.Type;
import com.debate.croll.publisher.crawler.community.template.AbstractCommunityCrawl;
import com.debate.croll.publisher.crawler.common.Record;
import com.debate.croll.publisher.crawler.common.Status;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Ppompu extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	//private final ChromeOptions options;

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	private List<Media> ppompuList;

	private final String name = CommunityName.Ppompu.name();

	private final String DirUrl = DirectoryUrl.CRAWLER_LOG_BASE_DIR+name;
	private int start = 4;


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

		// 공통 Options
		WebDriverManager.chromedriver()
			.setup();

		// 랜덤 User-Agent 리스트
		String[] userAgents = {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
		};

		// 랜덤 User-Agent 선택
		Random rand = new Random();
		String userAgent = userAgents[rand.nextInt(userAgents.length)];

		ChromeOptions options = new ChromeOptions();

		options.addArguments("--blink-settings=imagesEnabled=false");
		options.addArguments("--headless=new"); // headless 모드
		options.addArguments("--user-agent=" + userAgent); // 랜덤 User-Agent 설정
		options.addArguments("--no-sandbox"); // 추가적인 권한 설정
		options.addArguments("--disable-dev-shm-usage"); // Docker 환경에서 공유 메모리 부족 문제 해결


		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)


		WebDriver driver = null;

		// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
		if(status.name().equals("Reboot")){
			start = point;
		}

		try{

			ppompuList = new ArrayList<>(); // 버퍼

			log.info("do crawling ~ ");

			driver = super.setWebDriver(options,"ppomppu");
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			// Dir 업데이트
			FileManager fileManager = new FileManager();
			fileManager.updateDirCreatedDate(DirUrl);

			for(int i=start; i<4+loop; i++){
				extractElement(driver,i);
				Thread.sleep(750);
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

				driver.quit();// 다 하고 무조건 자원 반납.

				Thread.sleep(3000); // 의도적인 컨텍스트 스위칭 유발로, 다른 스레드 작업 처리를 위한 목적.
				log.info("ppompuList -> aws-rds");
				for(Media e : ppompuList){
					mediaRepository.save(e);
				}

				ppompuList = null;

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

			String url = e.findElement(By.cssSelector("td.baseList-space.title > a")).getAttribute("href");

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


			// time 가공 - 연원일만 그대로 유지
			/* Legacy
			LocalDateTime now = LocalDateTime.now().withNano(0);

			int hour = now.getHour();
			int minute = now.getMinute();
			int second = now.getSecond();

			String hourMinuteSec = String.format("%02d:%02d:%02d", hour, minute, second);

			String updatedTimeString = now.toString().replace(hourMinuteSec, beforeTime);

			LocalDateTime localDateTime = LocalDateTime.parse(updatedTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			 */

			log.info("ppomPu: "+title);

			Media ppomPu = Media.builder()
				.title(title)
				.url(url)
				.src(null)
				.category("사회")
				.media("뽐뿌")
				.type("community")
				.count(0)
				.createdAt(localDateTime)
				.build();


			ppompuList.add(ppomPu); // 버퍼에 추가하기

			//mediaRepository.save(ppomPu);

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
