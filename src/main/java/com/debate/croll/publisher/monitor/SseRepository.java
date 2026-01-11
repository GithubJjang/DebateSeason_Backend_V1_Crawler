package com.debate.croll.publisher.monitor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.manager.CrawlerErrorProcessor;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.monitor.response.CrawlerExecutionStats;
import com.debate.croll.publisher.monitor.response.CrawlerStatusResponse;
import com.debate.croll.publisher.monitor.response.ResponseState;

import com.debate.croll.publisher.monitor.sseEmitter.factory.SseEmitterFactory;
import com.debate.croll.publisher.monitor.sseEmitter.session.Session;
import com.debate.croll.publisher.monitor.sseEmitter.session.SessionContainer;
import com.debate.croll.publisher.crawler.manager.FileManager;
import com.debate.croll.publisher.crawler.manager.ProgressLogFormatter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class SseRepository {

	// 새로 고침을 할 때, 누적 카운트가 되는 것이 사고. <- 연결이 끊어지는 것이 아니다.
	// 그리고, 전체 값이 0으로 롤백이 되는 것도 문제이다.
	private final SessionContainer sessionContainer;
	private final SseEmitterFactory sseEmitterFactory;

	//
	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	public SseEmitter save(String userId){

		FileManager fileManager = new FileManager();

		// 0. 전체 파일 갯수
		int total = fileManager.countTotalProgressLogs(); // 전체 파일 로그 개수

		// 1. 성공 로그들 불러오기 { 이름1 : 생성날짜1, 이름2 : 생성날짜2, ... }
		List<ProgressLogFormatter> progressLogList = fileManager.extractSuccessFileInfo();
		int successLogSize = progressLogList.size(); // 진행 로그의 사이즈 = 사용자가 가질 Offset

		// 2. 에러 로그 불러오기 (H2)
		List<CrawlerErrorEvent> findTodayErrors = crawlerErrorEventRepository.findTodayErrors();
		int errorLogSize = findTodayErrors.size();

		// SseEmitter 설정
		SseEmitter sseEmitter = sseEmitterFactory.create(userId);

		// 2. SseSession 만들기
		Session session = new Session(sseEmitter,successLogSize,errorLogSize);
		sessionContainer.addSession(userId, session);

		// 3. 전체, 성공, 실패, 진행률 객체
		CrawlerExecutionStats executionStats = CrawlerExecutionStats.builder()
			.total(total)
			.success(successLogSize)
			.fail(errorLogSize)
			.progress(fileManager.calcProgress(total,successLogSize,errorLogSize))
			.build();

		// 추가로, 현재 에러 집계
		CrawlerErrorProcessor crawlerErrorProcessor = new CrawlerErrorProcessor();
		Map<String,Integer> exceptionTypeCountMap =  crawlerErrorProcessor.countExceptionClass(findTodayErrors);

		// 5. 응답객체
		CrawlerStatusResponse crawlerStatusResponse = CrawlerStatusResponse.builder()
			.responseState(ResponseState.INIT) // 상태값
			.exceptionTypeCountMap(exceptionTypeCountMap) // 예외 유형 갯수
			.state(executionStats) // 전체, 성공, 실패, 진행률 집계 객체
			.build();

		try {
			sseEmitter.send(crawlerStatusResponse);
		} catch (IOException e) {

			throw new RuntimeException(e);
		}

		return sseEmitter;

	}

}
