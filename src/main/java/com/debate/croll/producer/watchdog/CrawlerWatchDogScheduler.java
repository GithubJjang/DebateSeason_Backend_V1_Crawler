package com.debate.croll.producer.watchdog;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debate.croll.infrastructure.service.MediaService;
import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.watchdog.service.WatchDogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CrawlerWatchDogScheduler {

	// 빈을 관리하기 위한 인터페이스
	private final ConfigurableApplicationContext context;
	private final MediaApiRequestMonitor mediaApiRequestMonitor;
	private final WatchDogService watchDogService;

	private String preUpdatedAt = null;// 미디어 생성일자
	private LocalDateTime preTime = null;
	private boolean isMediaApiActivated = false;

	private int count = 0;

	@Scheduled(fixedDelay = 60000) // 1분마다 실행
	public void checkProgressStatus(){ // 현 진행 상태를 확인하는데, 만약 3분 동안 동일한 id와 created_at이라면, 프로세스 재시작을 한다.(500에러 시, driver 반납 안해서)

		Status curStatus = null;

		// 1. MediaApi 활성 상태 감지.
		LocalDateTime curTime = mediaApiRequestMonitor.getTime();

		if(curTime!=null){ // MediaApi 연결인 상태

			if(preTime==null || !curTime.equals(preTime)){ // 최초 활성 상태

				log.warn("Extractor로부터 연결 수신중");
				preTime=curTime;
				isMediaApiActivated = true;
			}
			else { // 연결이 끊어진 상태(preTime==curTime인 상태)
				log.warn("Extractor와 연결 중단");
				mediaApiRequestMonitor.setInit();//
				isMediaApiActivated = false;
			}

		}
		else { // curTime = null -> 최초 실행을 의미한다.
			log.warn("Extractor와 연결되지 않음");
			isMediaApiActivated = false;
		}

		// 2. CheckPoint 상태 감지.
		Optional<CrawlerProgressDto> progress  = watchDogService.checkStatus(); // 상태 감지

		if(progress.isPresent()){

			CrawlerProgressDto progressDto = progress.get();
			curStatus = progressDto.getStatus();

			if(preUpdatedAt==null){ // 처음 상태 초기화

				log.warn("Crawler_WatchDogScheduler 상태 초기화");
				preUpdatedAt = progressDto.getUpdatedAt();
				count=0;
			}
			else{
				// 현재 상태
				String curUpdatedAt = progressDto.getUpdatedAt();

				if(preUpdatedAt.equals(curUpdatedAt)){ // 시간이 동일하다 -> 크롤링 작동 중단.
					log.warn("Crawler 작업 중단");
					count++;
				}
				else{ // 시간이 다르다 -> 크롤링 현재 진행중.
					log.warn("Crawler 스크래핑 진행중");
					preUpdatedAt = curUpdatedAt;
					count=0;
				}
			}
		}
		else { // 만약 첫 시작부터 에러 발생한다면, 항상 null임
			log.error("처음부터 고장");
			count++;
		}

		// 3. 상태 확인
		if(!isMediaApiActivated && count>=3){

			int exitCode;

			if (curStatus == Status.DONE){ // -> 정상 종료 exit
				log.error("성공적으로 크롤링 프로세스를 종료합니다.");
				exitCode = SpringApplication.exit(context, () -> 0);
			}
			else {
				log.error("500에러가 의심이 됩니다. 재부팅을 시작합니다.");
				exitCode = SpringApplication.exit(context, () -> 1);
			}
				
			System.exit(exitCode);
		}

	}
}
