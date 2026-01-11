package com.debate.croll.publisher.crawler.news;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import com.debate.croll.logger.common.crawler.OriginClass;
import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.crawler.common.CommunityName;
import com.debate.croll.publisher.crawler.common.DirectoryUrl;
import com.debate.croll.publisher.crawler.manager.FileManager;
import com.debate.croll.publisher.domain.entity.Media;
import com.debate.croll.publisher.monitor.FailCounter;
import com.debate.croll.publisher.repository.MediaRepository;
import com.debate.croll.publisher.crawler.common.Record;
import com.debate.croll.publisher.crawler.common.Status;
import com.debate.croll.publisher.crawler.common.Type;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class News {

	private final MediaRepository mediaRepository;
	private final ChromeOptions options;

	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	//private final String name = CommunityName.News.name();

	private final String DirUrl = DirectoryUrl.NEWS_LOG_BASE_DIR;

	@PostConstruct
	public void init(){
		FileManager fileManager = new FileManager();
		fileManager.createDir(DirUrl);
	}

	public void crawl(Status status, String url, String name, Integer i, int point) {

		// i : 카테고리
		// j : 인덱스
		// point : 뉴스를 긁어오는 시작점. 카테고리와 별개.

		int startIndex = 1;


		WebDriver driver = null; // 매번 새로 생성된 후, 다하고 버려야 한다. -> 일회용

		// 1. driver 예외를 잡기 위한 처리.
		try{

			// 예기치 못한 장애로 인해서, 리부팅 시 발동되는 조건
			if(status.name().equals("Reboot")){
				startIndex = point;
			}

			driver = new ChromeDriver(options); // driver 생성 실패 시, 에러를 잡기 위함.
			driver.get(url + "?sid=" + i.toString());

			// Dir 업데이트
			FileManager fileManager = new FileManager();
			fileManager.updateDirCreatedDate(DirUrl);

			for (int j = startIndex; j <= 2; j++) {

				extractElement2(driver,name,i,j);
				Thread.sleep(1000);// 분까지 겹치는 경우를 방지해서 일부러 1초 기다림

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

		}
		finally {

			if (driver != null) {
				log.info("Done News Crawling ~ ");
				driver.quit();
			}

		}

	}


	public void extractElement2(WebDriver driver, String name, Integer i, int j){

		// i는 category 번호 : 100,101,102,104
		// 웹 페이지에서 Element를 가져올 때, 에러를 잡기 위한 설정.

		try{

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			// 1. Link
			WebElement aTag = driver.findElement(
				By.cssSelector("#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
					+ ") > a.press_edit_news_link._es_pc_link"));
			String href = aTag.getAttribute("href");

			// 2. title
			WebElement titleElement = driver.findElement(By.cssSelector(
				"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
					+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_text > span.press_edit_news_title"));
			String title = titleElement.getText();

			// 3. img
			String image;
			try {
				WebElement imgElement = driver.findElement(By.cssSelector(
					"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
						+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_thumb > img"));
				image = imgElement.getAttribute("src");
			} catch (NoSuchElementException e) { // 없으면 image는 null이다.
				image = null;
			}

			// 4. time
			WebElement timeElement = driver.findElement(By.cssSelector(
				"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
					+ ") > a > span.press_edit_news_text > span.r_ico_b.r_modify"));
			String outdpated = timeElement.getText();

			LocalDateTime time = null;
			// 분전(min),시간전(hour),일전(day)

			if (outdpated.contains("분전")) {
				outdpated = outdpated.replace("분전", "");
				time = now.minusMinutes(Integer.parseInt(outdpated));
			} else if (outdpated.contains("시간전")) {
				outdpated = outdpated.replace("시간전", "");
				time = now.minusHours(Integer.parseInt(outdpated));

			} else if (outdpated.contains("일전")) {
				outdpated = outdpated.replace("일전", "");
				time = now.minusDays(Integer.parseInt(outdpated));

			}

			// 카테고리 넣기
			// 정치 https://media.naver.com/press/422?sid=100
			// 경제 https://media.naver.com/press/422?sid=101
			// 사회 https://media.naver.com/press/422?sid=102
			// IT https://media.naver.com/press/422?sid=105
			String categoryName;

			categoryName = switch (i) {
				case 100 -> "정치";
				case 101 -> "경제";
				case 102 -> "사회";
				case 104 -> "세계";
				default -> null; // 또는 "" / 필요하면 기본값
			};

			Media news = Media.builder()
				.title(title)
				.url(href)
				.src(image)
				.category(categoryName)
				.media(name)
				.type("news")
				.count(0)
				.createdAt(time)
				.build();
			mediaRepository.save(news);

			// i = category
			// j = point
			Record record = new Record(name,i,j, Type.News);
			record.recordFile();

		}
		catch ( Exception e ){


			String[] arr = e.getMessage().split("\\n");

			CrawlerErrorEvent crawlerErrorEvent = CrawlerErrorEvent.builder()
				.OriginClass(OriginClass.CRAWLER)
				.type(com.debate.croll.logger.common.crawler.Type.NEWS)
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

	}
}
