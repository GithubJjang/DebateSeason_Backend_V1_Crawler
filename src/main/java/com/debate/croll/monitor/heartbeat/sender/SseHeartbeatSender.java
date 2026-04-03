package com.debate.croll.monitor.heartbeat.sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.monitor.manager.ErrorEventManager;
import com.debate.croll.monitor.mapper.error.ErrorDTOFactory;
import com.debate.croll.monitor.mapper.error.ErrorDTO;

import com.debate.croll.producer.repository.CheckPointJpaRepository;
import com.debate.croll.producer.repository.ErrorJpaRepository;

import com.debate.croll.monitor.heartbeat.scheduler.HeartBeatScheduler;
import com.debate.croll.monitor.heartbeat.sender.template.HeartBeatSender;
import com.debate.croll.monitor.response.crawler.CrawlerExecutionStats;
import com.debate.croll.monitor.response.crawler.CrawlerStatusResponse;
import com.debate.croll.monitor.response.crawler.ResponseState;
import com.debate.croll.monitor.util.sse.session.SseSession;
import com.debate.croll.monitor.util.sse.SessionManager;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseHeartbeatSender implements HeartBeatSender { // 어차피 유틸리티 클래스인데, 매번 새로 생성할 필요가 있을까???

	private final CheckPointJpaRepository checkPointJpaRepository;
	private final ErrorJpaRepository errorJpaRepository;

	private final ErrorEventManager errorEventManager;

	public void sendHeartbeat(HeartBeatScheduler heartBeatScheduler){

		// 1. { userId, SseSession } ,사용자마다 각자 다른 로그 정보들을 가지고 있다.
		ConcurrentHashMap<String, SseSession> sessionContainer = SessionManager.getSessionContainer();

		if (!sessionContainer.isEmpty()) { // 사용자가 있다면, 로그를 전송한다.

			Long countTodaySuccess = checkPointJpaRepository.countTodaySuccessCheckPoint();
			Long countTodayError = errorJpaRepository.countTodayErrors();
			int total = countTodaySuccess.intValue() + countTodayError.intValue();

			// 현재 error 상태
			ErrorDTOFactory errorDTOFactory = new ErrorDTOFactory();

			//
			List<ErrorDTO> errorList = new ArrayList<>();
			errorList.add(
				ErrorDTO.builder()
					.id(0L)
					.build()
			);

			// 뒤에 추가
			errorList.addAll(
				errorJpaRepository.findTodayErrors().stream()
					.map(errorDTOFactory::ErrorEntityToErrorDTO)
					.toList()
			);

			// 4. user에게 로그 전송하기
			Set<String> userIdSet = sessionContainer.keySet(); // userId 목록 가져오기.

			for(String userId : userIdSet){ // 사용자마다 별도로 전송을 해준다.

				SseSession sseSession = sessionContainer.get(userId);
				SseEmitter sseEmitter = sseSession.getSseEmitter();

				// 각 세션별로 Offset을 이용해서 변경분만 전송을 하자.
				Map<String,Integer> incrementalErrorMap = getIncrementalErrorPage(sseSession,errorList); // 에러 로그 집계
				List<ErrorDTO> incrementalErrorList = getIncrementalErrorList(sseSession,errorList); // 에러 증분만 전송.

				// 전체, 성공, 실패, 진행률 객체
				CrawlerExecutionStats executionStats = CrawlerExecutionStats.builder()
					.total(total)
					.success(countTodaySuccess.intValue())
					.error(countTodayError.intValue())
					.errorList(incrementalErrorList)
					.build();

				// 응답객체
				CrawlerStatusResponse crawlerStatusResponse =
					new CrawlerStatusResponse(ResponseState.DIRTY,incrementalErrorMap,executionStats);

				try {
					// 변경된 부분만 전송을 한다.
					sseEmitter.send(crawlerStatusResponse);

				}
				catch (IOException e) { // 브라우저 종료하면, 활성화 3

					log.error("SseHeartbeatSender.IOException operates");

					// false는 세션이 안 끊어진 경우 -> 따라서, decrement를 실행
					if(!SessionManager.isSessionDisconnected(userId)){
						heartBeatScheduler.decrement(); // atomic하게 -1
					}

				}

			}
		}
	}

	public List<ErrorDTO> getIncrementalErrorList(SseSession sseSession,
		List<ErrorDTO> errorList){

		int errorLogOffset = sseSession.getErrorLogOffset();
		boolean take = false;

		List<ErrorDTO> incrementalErrorList = new ArrayList<>();
		for(ErrorDTO e : errorList){

			// 상태값 변환
			if(e.getId()==errorLogOffset){
				take = true;
				continue;
			}

			// 값 담기
			if(take){
				incrementalErrorList.add(e);
				sseSession.setErrorLogOffset(e.getId().intValue());
			}

		}

		return incrementalErrorList;
	}


	public Map<String,Integer> getIncrementalErrorPage(
		SseSession sseSession,
		List<ErrorDTO> errorList){

		int errorLogOffset = sseSession.getErrorLogOffset();
		boolean take = false;

		List<ErrorDTO> incrementalErrorPages = new ArrayList<>();
		for(ErrorDTO e : errorList){

			// 상태값 변환
			if(e.getId()==errorLogOffset){
				take = true;
				continue;
			}

			// 값 담기
			if(take){
				incrementalErrorPages.add(e);
			}

		}

		return errorEventManager.countExceptionClass(incrementalErrorPages);
	}


}
