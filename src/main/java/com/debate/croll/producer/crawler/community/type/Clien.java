package com.debate.croll.producer.crawler.community.type;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import com.debate.croll.producer.crawler.common.Record;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.community.template.AbstractCommunityCrawl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Clien extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	private final ErrorRepository errorRepository;
	private final WebDriverFactory webDriverFactory;

	private final String name = Community.Clien.name();
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

		try{

			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(2)
			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1)
			WebElement webElement =
				driver.findElement(By.cssSelector(
					"body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child("
						+ i + ")"));

			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title
			WebElement hrefElement = webElement.findElement(By.cssSelector("div.list_title"));

			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title > a.list_subject
			String url = hrefElement.findElement(By.cssSelector("a.list_subject")).getAttribute("href");
			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title > a.list_subject > span.subject_fixed
			String title = hrefElement.findElement(By.cssSelector("a.list_subject > span.subject_fixed")).getText();

			// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_time > span > span
			String time = webElement.findElement(By.cssSelector("div.list_time > span")).getText();

			LocalDate today = LocalDate.now();

			String reformTime = time.replace("-", ":");

			String date = today + " " + reformTime;

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

			Media Clien = Media.builder()
				.title(title)
				.url(url)
				.src(null)// 이미지
				.category("사회")
				.media("클리앙")
				.type("community")
				.count(0)
				.createdAt(dateTime.toString())
				.build();

			mediaRepository.save(Clien);

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
