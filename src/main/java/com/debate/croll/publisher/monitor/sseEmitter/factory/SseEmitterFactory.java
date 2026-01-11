package com.debate.croll.publisher.monitor.sseEmitter.factory;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.publisher.monitor.heartbeat.HeartBeatScheduler;
import com.debate.croll.publisher.monitor.sseEmitter.session.SessionContainer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class SseEmitterFactory {

	private final HeartBeatScheduler heartBeatScheduler;

	public SseEmitter create(String userId){

		// SseEmitter 설정
		SseEmitter sseEmitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유효

		// 하위 명시된 이벤트들은 더미 데이터를 수신 이후, 연결 중단시 발동을 한다.
		sseEmitter.onCompletion(() ->{ // 브라우저 종료하면, 활성화 1

				log.error("onCompletion : "+userId);
				disconnectSession(userId);
			}
		);

		sseEmitter.onTimeout(() -> {

			log.error("onTimeout : "+userId);
			disconnectSession(userId);

		});

		sseEmitter.onError(e -> { // 브라우저 종료하면, 활성화 2

			log.error("onError : "+userId);
			disconnectSession(userId);

		});

		return sseEmitter;

	}

	private void disconnectSession(String userId){

		// false는 세션이 안 끊어진 경우 -> 따라서, decrement를 실행
		if(SessionContainer.isSessionDisconnected(userId)==false){ // 연결 끊을 때는 synchronized
			log.error("decrement 실행 : "+userId);
			heartBeatScheduler.decrement(); // atomic하게 -1
		}

	}
}
