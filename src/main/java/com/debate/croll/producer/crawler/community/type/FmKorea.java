package com.debate.croll.producer.crawler.community.type;

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
import com.debate.croll.producer.crawler.common.Record;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.community.template.AbstractCommunityCrawl;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class FmKorea extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	private final ErrorRepository errorRepository;
	private final WebDriverFactory webDriverFactory;
	private LocalDate today;

	private final String name = Community.FmKorea.name();

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
				.createdAt(dateTime.toString())
				.build();

			mediaRepository.save(fmKorea);

			Record record = new Record(name,i, Type.Community);
			record.recordFile();

		}
		catch (Exception e){

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
