package com.debate.croll.publisher.monitor.heartbeat.heartbeatSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.logger.domain.entity.CrawlerErrorEvent;
import com.debate.croll.logger.manager.CrawlerErrorProcessor;
import com.debate.croll.logger.repository.CrawlerErrorEventRepository;
import com.debate.croll.publisher.monitor.FailCounter;
import com.debate.croll.publisher.monitor.heartbeat.HeartBeatScheduler;
import com.debate.croll.publisher.monitor.heartbeat.heartbeatSender.template.HeartBeatSender;
import com.debate.croll.publisher.monitor.response.CrawlerExecutionStats;
import com.debate.croll.publisher.monitor.response.CrawlerStatusResponse;
import com.debate.croll.publisher.monitor.response.ResponseState;
import com.debate.croll.publisher.monitor.sseEmitter.session.Session;
import com.debate.croll.publisher.monitor.sseEmitter.session.SessionContainer;
import com.debate.croll.publisher.crawler.manager.FileManager;
import com.debate.croll.publisher.crawler.manager.ProgressLogFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseHeartbeatSender implements HeartBeatSender { // 어차피 유틸리티 클래스인데, 매번 새로 생성할 필요가 있을까???

	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	public void sendHeartbeat(HeartBeatScheduler heartBeatScheduler){

		FileManager fileManager = new FileManager();

		// 1. { userId, SseSession } ,사용자마다 각자 다른 로그 정보들을 가지고 있다.
		ConcurrentHashMap<String, Session> sessionContainer = SessionContainer.getSessionContainer();

		if (sessionContainer.isEmpty()) {

			log.info("연결된 사용자가 없습니다.");
			return;
		}
		else{

			// 2. userId 목록 가져오기.
			Set<String> userIdSet = sessionContainer.keySet();

			// 3. 지금 에러 + 성공 목록 스냅샷.(기준) -> 성능을 고려해서
			List<CrawlerErrorEvent> findTodayErrors; // null로 두면 sse 에러 발생함. 따라서 빈 배열로 두어야 함. 그리고 DB에선 PK로 정렬해서 순서가 일정.
			List<ProgressLogFormatter> realTimeSuccessLogList = fileManager.extractSuccessFileInfo();

			if(FailCounter.comparePreFailCountToCurrentFailCount()==false){ // 신규 에러가 발생한 상태. 따라서 DB에서 가져온다.

				findTodayErrors = crawlerErrorEventRepository.findTodayErrors();

				FailCounter.updatePreFailCount(); // preFailCount를 업데이트한다.
			}
			else{
				findTodayErrors = new ArrayList<>();
			}

			//
			int total = fileManager.countTotalProgressLogs(); // 전체 로그는 고정값
			int successLogSize = realTimeSuccessLogList.size(); // 성공 개수
			int errorLogSize = findTodayErrors.size(); // 실패 개수

			// 4.
			for(String userId : userIdSet){ // 사용자마다 별도로 전송을 해준다.

				Session session = sessionContainer.get(userId);
				SseEmitter personalSseEmitter = session.getSseEmitter();

				// 각 세션별로 Offset을 이용해서 변경분만 전송을 하자.
				Map<String,String> dirtyCheckedSuccessLogMap = dirtyCheckingSuccessLog(session,realTimeSuccessLogList); // 어차피 append-only라서 문제 x
				Map<String,Integer> dirtyCheckedErrorLogMap = dirtyCheckingErrorLog(session,findTodayErrors); // 에러 로그 집계

				// 전체, 성공, 실패, 진행률 객체
				CrawlerExecutionStats executionStats = CrawlerExecutionStats.builder()
					.total(total)
					.success(successLogSize)
					.fail(errorLogSize)
					.progress(fileManager.calcProgress(total,successLogSize,errorLogSize))
					.build();

				// 응답객체
				CrawlerStatusResponse crawlerStatusResponse =
					new CrawlerStatusResponse(ResponseState.DIRTY,dirtyCheckedSuccessLogMap,dirtyCheckedErrorLogMap,executionStats);

				try {
					// 변경된 부분만 전송을 한다.
					personalSseEmitter.send(crawlerStatusResponse);

				}
				catch (IOException e) { // 브라우저 종료하면, 활성화 3

					log.error("SseHeartbeatSender.IOException operates");

					// false는 세션이 안 끊어진 경우 -> 따라서, decrement를 실행
					if(SessionContainer.isSessionDisconnected(userId)==false){
						heartBeatScheduler.decrement(); // atomic하게 -1
					}

				}

			}

		}
	}

	public Map<String,String> dirtyCheckingSuccessLog(
		Session session,
		List<ProgressLogFormatter> successLogList
		)
	{
		// 반환할 데이터 자료구조.
		Map<String,String> dirtyMap = new LinkedHashMap<>();

		int start = session.getProgressLogOffset();
		int end = successLogList.size();

		// 딱 변경분만 전송을 하게끔 수정.
		for(int i=start; i<end; i++){

			ProgressLogFormatter progressLogFormatter = successLogList.get(i);

			String name = progressLogFormatter.getName();
			String date = progressLogFormatter.getModifiedDate();

			dirtyMap.put(name,date);
		}

		session.setProgressLogOffset(end); // 그리고, Offset을 다음 가져올 포인터로 이동을 시킨다.

		return dirtyMap;



	}

	public Map<String,Integer> dirtyCheckingErrorLog(
		Session session,
		List<CrawlerErrorEvent> findTodayErrors){

		// 사용자가 가진 errorLog 목록과 방금 저장된 errorLog 목록 비교해서, 변경분만 반환을 해준다.

		int start = session.getErrorLogOffset();
		int end = findTodayErrors.size();

		// dirtyPage에 데이터 추가하기.
		List<CrawlerErrorEvent> dirtyPage = new ArrayList<>();

		for(int i=start; i<end; i++){
			dirtyPage.add(findTodayErrors.get(i));
		}

		session.setErrorLogOffset(end); // 그리고, Offset을 다음 가져올 포인터로 이동을 시킨다.

		CrawlerErrorProcessor crawlerErrorProcessor = new CrawlerErrorProcessor();

		return crawlerErrorProcessor.countExceptionClass(dirtyPage);

	}

}
