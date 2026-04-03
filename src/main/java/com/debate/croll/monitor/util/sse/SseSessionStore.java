package com.debate.croll.monitor.util.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.SynthUI;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.monitor.manager.ErrorEventManager;
import com.debate.croll.monitor.mapper.error.ErrorDTOFactory;
import com.debate.croll.monitor.response.crawler.CrawlerExecutionStats;
import com.debate.croll.monitor.response.crawler.CrawlerStatusResponse;
import com.debate.croll.monitor.response.crawler.ResponseState;
import com.debate.croll.monitor.mapper.error.ErrorDTO;
import com.debate.croll.monitor.util.sse.session.SseSession;
import com.debate.croll.monitor.util.sse.session.SseEmitterFactory;

import com.debate.croll.producer.repository.CheckPointJpaRepository;
import com.debate.croll.producer.repository.ErrorJpaRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class SseSessionStore {

	// 새로 고침을 할 때, 누적 카운트가 되는 것이 사고. <- 연결이 끊어지는 것이 아니다.
	// 그리고, 전체 값이 0으로 롤백이 되는 것도 문제이다.
	private final CheckPointJpaRepository checkPointJpaRepository;
	private final ErrorJpaRepository errorJpaRepository;

	private final SessionManager sessionManager;
	private final SseEmitterFactory sseEmitterFactory;

	private final ErrorEventManager errorEventManager;

	public SseEmitter save(String userId){

		ErrorDTOFactory errorDTOFactory = new ErrorDTOFactory();

		List<ErrorDTO> todayErrorList = errorJpaRepository.findTodayErrors().stream()
			.map(errorDTOFactory::ErrorEntityToErrorDTO)
			.toList();

		int lastErrorIndex = 0; // 다음 에러 증분을 가져오기 위한 index

		Long countTodaySuccess = checkPointJpaRepository.countTodaySuccessCheckPoint();
		int countTodayError = todayErrorList.size(); // 전체 에러
		if(!todayErrorList.isEmpty()){
			ErrorDTO lastEntity = todayErrorList.get(todayErrorList.size()-1);
			lastErrorIndex = Math.toIntExact(lastEntity.getId());
		}

		int total = countTodaySuccess.intValue() + countTodayError;


		// 2. SseSession 만들기
		SseEmitter sseEmitter = sseEmitterFactory.create(userId);
		SseSession sseSession = new SseSession(sseEmitter,lastErrorIndex); // 아무에러도 없으면 lastError = 0
		sessionManager.addSession(userId, sseSession);

		// 3. 전체, 성공, 실패, 진행률 객체
		CrawlerExecutionStats executionStats = CrawlerExecutionStats.builder()
			.total(total)
			.success(countTodaySuccess.intValue())
			.error(countTodayError)
			.errorList(todayErrorList)
			.build();

		Map<String,Integer> exceptionTypeCountMap = errorEventManager.countExceptionClass(todayErrorList);

		// 5. 응답객체
		CrawlerStatusResponse crawlerStatusResponse = CrawlerStatusResponse.builder()
			.responseState(ResponseState.INIT) // 상태값
			.exceptionMap(exceptionTypeCountMap) // 예외 유형 갯수
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
