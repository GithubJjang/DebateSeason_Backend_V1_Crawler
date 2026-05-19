package com.debate.croll.producer.crawler.source.community.item;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.springframework.stereotype.Component;

import com.debate.croll.producer.common.ExceptionClassfier;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDtoFactory;
import com.debate.croll.producer.crawler.source.community.config.CommunityNameList;
import com.debate.croll.producer.crawler.source.community.template.AbstractCommunitySource;
import com.debate.croll.producer.crawler.source.community.config.CommunityUrlList;
import com.debate.croll.producer.crawler.dto.CheckPointDTO;
import com.debate.croll.producer.crawler.dto.error.CrawlerErrorDTO;
import com.debate.croll.producer.crawler.dto.MediaDTO;
import com.debate.croll.infrastructure.service.CrawlerApplicationService;
import com.debate.croll.infrastructure.service.ErrorService;
import com.debate.croll.producer.crawler.common.Type;
import com.debate.croll.webdriver.WebDriverFactory;
import com.debate.croll.webdriver.WebDriverRunner;
import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.source.community.config.CommunityConfig;
import com.debate.croll.producer.crawler.common.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Clien extends AbstractCommunitySource { // 이미지 없음


	private final CrawlerApplicationService crawlerApplicationService;
	private final ErrorService errorService;

	private final CrawlerErrorDtoFactory crawlerErrorDtoFactory;
	private final WebDriverFactory webDriverFactory;

	private final ExceptionClassfier exceptionClassfier;

	private final String name = CommunityNameList.Clien.name();

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
		if(status.name().equals(Status.REBOOT.getName())){
			start = point;
		}

		try{
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

			extractElement(driver,-1);
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

	public void extractElement(WebDriver driver,int i) {

		String href = null;

		int count = 0;
		List<WebElement> items;

		try{
			WebElement listElement = driver.findElement(By.className("list_content"));
			items = listElement.findElements(By.cssSelector(".list_item.symph_row"));
		}
		catch (NoSuchElementException e){
			throw new java.util.NoSuchElementException("items을 찾을 수 없습니다. 페이지 요소 변경이 의심됩니다.");
		}

		for(WebElement e : items){

			if(count>=8){
				break;
			}

			try {

				WebElement subjectElement = e.findElement(By.className("list_subject"));
				href = subjectElement.getAttribute("href");

				WebElement fixedSpan = subjectElement.findElement(By.className("subject_fixed"));

				String title = fixedSpan.getText();

				WebElement timeElement = e.findElement(By.className("timestamp"));
				String strTime = timeElement.getAttribute("textContent");

				DateTimeFormatter formatter =
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				LocalDateTime dateTime =
					LocalDateTime.parse(strTime, formatter)
						.truncatedTo(ChronoUnit.MINUTES);

				MediaDTO mediaDTO = new MediaDTO(
					title,
					href,
					null,
					"사회",
					CommunityNameList.Clien.getName(),
					Type.COMMUNITY.getName(),
					0,
					dateTime.toString()
				);

				CheckPointDTO checkPointDTO = new CheckPointDTO(
					name,
					null,
					i,
					Type.COMMUNITY,
					LocalDateTime.now().toString(),
					Status.REBOOT
				);

				crawlerApplicationService.saveMediaAndCheckPoint(mediaDTO,checkPointDTO);// @Transactional 걸어서 media & checkpoint 안전하게 저장

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

				//Sentry.captureException(e);
			}
			finally {
				count++;
				href=null;
			}
		}

	}
}
