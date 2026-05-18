package com.debate.croll.producer.crawler.source.community.item;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.infrastructure.service.CrawlerApplicationService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class HumorUniv extends AbstractCommunitySource {

	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final int init = 3;

	private final String name = CommunityNameList.HumorUniv.name();
	private int start = 0;

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
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
			extractElement(driver,-1); // 호환을 위해서 유지.

			Thread.sleep(1500); // 의심을 피하기 위한 설정.

		}
		catch (Exception exception){

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER,
				Type.DRIVER,
				name,
				null);

			errorService.save(errorDTO);

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

		String href = null;
		String src = null;

		try {

			List<WebElement> webElements = driver.findElements(By.className("post_item"));

			int count = 0;

			for(WebElement e : webElements){

				if(count==8){
					break;
				}

				// 1. href
				WebElement rawHref = e.findElement(
					By.cssSelector("a.list_body_href.post_link")
				);
				href = rawHref.getAttribute("href");

				// 2. title
				WebElement titleSpan = e.findElement(
					By.cssSelector("span[id^='title_chk_pds']")
				);
				String title = titleSpan.getText(); // title 가져오기

				// 3. src
				WebElement rawSrc = e.findElement(By.className("img"));
				src = rawSrc.getAttribute("src");

				// 4. time
				WebElement rawTime = e.findElement(
					By.cssSelector("div > span.extra")
				);
				String timeText = rawTime.getText();

				String[] timeElements = timeText.split(" ");

				String dayOfTheWeek = timeElements[0];

				LocalDate today = LocalDate.now();
				LocalDate targetDate = null;

				DayOfWeek targetDay = null;

				if (dayOfTheWeek.contains("월")) {
					targetDay = DayOfWeek.MONDAY;
				} else if (dayOfTheWeek.contains("화")) {
					targetDay = DayOfWeek.TUESDAY;
				} else if (dayOfTheWeek.contains("수")) {
					targetDay = DayOfWeek.WEDNESDAY;
				} else if (dayOfTheWeek.contains("목")) {
					targetDay = DayOfWeek.THURSDAY;
				} else if (dayOfTheWeek.contains("금")) {
					targetDay = DayOfWeek.FRIDAY;
				} else if (dayOfTheWeek.contains("토")) {
					targetDay = DayOfWeek.SATURDAY;
				} else if (dayOfTheWeek.contains("일")) {
					targetDay = DayOfWeek.SUNDAY;
				}

				if (targetDay != null) {
					int diff = targetDay.getValue() - today.getDayOfWeek().getValue();
					targetDate = today.plusDays(diff);
				}

				String[] hm = timeElements[1].split(":");

				int hour = Integer.parseInt(hm[0]);
				int minute = Integer.parseInt(hm[1]);

				LocalDateTime dateTime =
					targetDate != null ? targetDate.atTime(hour, minute) : null;

				MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
					title,
					href, // 링크
					src, // 이미지
					"정치",
					CommunityNameList.HumorUniv.getName(),
					Type.COMMUNITY.getName(),
					0,
					dateTime.toString()
				);

				CheckPointDTO.CreateCheckPointDTO checkPointDTO = new CheckPointDTO.CreateCheckPointDTO(
					name,
					null,
					i,
					Type.COMMUNITY,
					LocalDateTime.now().toString(),
					Status.REBOOT
				);

				crawlerApplicationService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);

				count ++;
			}

			// Legacy
			// WebElement webElement = driver.findElement(
			// 	By.cssSelector("#list_body > ul > a:nth-child(" + (init + i * 2) + ")"));
			//
			// // href
			// href = driver.findElement(
			// 	By.cssSelector("#list_body > ul > a:nth-child(" + (init + i * 2) + ")")).getAttribute("href");
			//
			// // title
			// WebElement idElement = webElement.findElement(By.cssSelector("li"));
			// String id = idElement.getAttribute("id");
			//
			// String numberOnly = id.replaceAll("[^0-9]", ""); // 숫자가 아닌 문자를 모두 제거
			// String title = driver.findElement(By.cssSelector("#title_chk_pds-" + numberOnly)).getText(); //#title_chk_pds-1366802
			//
			//
			// // src
			// String src = null;//이미지
			// try{
			// 	src = driver.findElement(
			// 			By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(1) > div > img"))
			// 			.getAttribute("src");
			// }
			// catch (NoSuchElementException e){
			//
			// }
			//
			// // time
			// String time = driver.findElement(
			// 	By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(2) > div > span.extra")).getText();
			//
			// // 2. 시간 부분만 추출
			// String timePart = time.split(" ")[1]; // "07:31"
			//
			// // 3. 시:분 파싱
			// DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			// LocalTime parsedTime = LocalTime.parse(timePart, timeFormatter);
			//
			// // 4. 현재 날짜에 시:분만 교체
			// LocalDateTime now = LocalDateTime.now();
			// LocalDateTime updatedDateTime = now
			// 	.withHour(parsedTime.getHour())
			// 	.withMinute(parsedTime.getMinute())
			// 	.withSecond(0)
			// 	.withNano(0);





			// MediaDTO.CreateMediaDTO mediaDTO = new MediaDTO.CreateMediaDTO(
			// 	title,
			// 	href,
			// 	src,
			// 	"정치",
			// 	CommunityNameList.HumorUniv.getName(),
			// 	Type.COMMUNITY.getName(),
			// 	0,
			// 	updatedDateTime.toString()
			// );
			//
			// CheckPointDTO.CreateCheckPointDTO checkPointDTO = new CheckPointDTO.CreateCheckPointDTO(
			// 	name,
			// 	null,
			// 	i,
			// 	Type.COMMUNITY,
			// 	LocalDateTime.now().toString(),
			// 	Status.REBOOT
			// );
			//
			// crawlerApplicationService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);

		}
		catch (Exception exception){

			boolean isUniqueConstraintViolation = exceptionClassfier.isUniqueConstraintViolation(exception);
			if(isUniqueConstraintViolation){ // 데이터 중복으로 발생한 에러라면, href를 저장하지 않는다. 그렇지 않다면, url이라도 저장해서 recovery를 해서 누락을 막자.
				href = null;
			}

			CrawlerErrorDTO errorDTO = crawlerErrorDtoFactory.createErrorDto(
				exception,
				OriginClass.CRAWLER,
				Type.COMMUNITY,
				name,
				href);

			errorService.save(errorDTO);

		}

	}

}
