package com.debate.croll.producer.crawler.community.type;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class RuliWeb extends AbstractCommunityCrawl {

	private final MediaRepository mediaRepository;
	private final ErrorRepository errorRepository;
	private final WebDriverFactory webDriverFactory;

	private final String name = Community.RuliWeb.name();

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
			log.info("Reboot-"+name+" : "+ start);
			start = point;
		}

		try{

			log.info("do crawling ~ ");

			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			log.info("find element");

			int loop = CommunityConfig.loop;

			for(int i=start; i<=loop; i++){

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
			// #best_body > table > tbody > tr:nth-child(1) > td.subject > a
			WebElement element1 = driver.findElement(
				By.cssSelector("#best_body > table > tbody > tr:nth-child(" + i + ")"));

			WebElement titleElement = element1.findElement(
				By.cssSelector("tr:nth-child(" + i + ") > td.subject > a"));

			/* Legacy
			// #best_body > table > tbody > tr:nth-child(1) > td.time
			WebElement timeElement = driver.findElement(By.cssSelector("td.time"));

			// time 가공
			LocalDateTime now = LocalDateTime.now().withNano(0);

			int hour = now.getHour();
			int minute = now.getMinute();
			int second = now.getSecond();

			String beforeTime = timeElement.getText() + ":" + second;

			String hourMinuteSec = hour
				+ ":"
				+ minute
				+ ":"
				+ second;

			String updatedTimeString = now.toString().replace(hourMinuteSec, beforeTime);

			LocalDateTime localDateTime = LocalDateTime.parse(updatedTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			 */

			// #best_body > table > tbody > tr:nth-child(1) > td.time
			WebElement timeElement = driver.findElement(By.cssSelector("td.time"));

			// 1) 화면에서 시간 문자열 가져오기 (예: "12:36")
			String timeText = timeElement.getText().trim();  // "12:36" 가정

			// 2) "HH:mm" 형식으로 파싱
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime parsedTime = LocalTime.parse(timeText, timeFormatter);  // 12:36

			// 3) 오늘 날짜 + 파싱한 시간으로 LocalDateTime 생성
			LocalDate today = LocalDate.now();   // 필요하면 ZoneId.of("Asia/Seoul") 고려
			LocalDateTime localDateTime = LocalDateTime.of(today, parsedTime);

			// 4) 초(second)는 지금 기준으로 맞추고 싶으면 이렇게
			int second = LocalTime.now().getSecond();
			localDateTime = localDateTime
				.withSecond(second)
				.withNano(0);

			Media RuliWeb = Media.builder()
				.title(titleElement.getText())
				.url(titleElement.getAttribute("href"))
				.src(null)
				.category("사회")
				.media("루리웹")
				.type("community")
				.count(0)
				.createdAt(localDateTime.toString())
				.build();

			mediaRepository.save(RuliWeb);

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
