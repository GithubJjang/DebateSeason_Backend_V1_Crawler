package com.debate.croll.publisher.monitor.sseEmitter.session;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.publisher.monitor.heartbeat.HeartBeatScheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class SessionContainer {

	private final HeartBeatScheduler heartBeatScheduler;

	// { userId : SseSession }
	// 만약 서버가 종료되면, 연결된 모든 사용자 다 끊어짐. 따라서 세션을 유지할 필요가 없다.
	// static으로 하지 않는다면, isSessionDisconnected가 non-static이 됨. 그런데 이 메소드는 SseHeartbeatSender에서도 써야하는데 순환참조 발생.
	public final static ConcurrentHashMap<String, Session> sessionContainer
		= new ConcurrentHashMap<>();

	public void addSession(String userId, Session session){

		// 아무것도 매핑되는 값이 없는 경우에, null을 반환하고, if문으로 중복 생성을 방지.
		if(sessionContainer.putIfAbsent(userId, session)==null){ // 버킷 단위 락이라서 ok
			heartBeatScheduler.increment();
		}
		else{
			log.error("이미 존재하는 세션 : "+userId);
		}
	}

	// disconnectSession만 static으로 하자. 왜냐하면 SseHeartbeatSender에 주입을 할 경우 순환 참조가 되고, 또 disconnect 때문에 굳이 전체를 DI해서 복잡하게 할 필요가 있나???
	public static synchronized boolean isSessionDisconnected(String userId){

		// 1. Sse연결을 끊고 ( 이렇게 직접 종료를 하지 않을 경우, 비정상적인 세션 종료에 의한 에러 발생.)

		Session session = sessionContainer.get(userId);

		if(session!=null){ // 세션이 끊어지지 않았다. false
			log.error("세션이 아직 살아있음 : "+userId);
			SseEmitter sseEmitter = session.getSseEmitter();
			sessionContainer.remove(userId); // remove는 MapEntry에서만 제거 -> value가 다른 객체에 참조 되고 있다면, 여전히 메모리 누수가 발생함...
			sseEmitter.complete(); // 명확하게 세션을 종료해야만 함!. 그리고, null에 대해서 실행을 하면 exception 발생

			return false;
		}
		else{ // session == null -> 세션이 끊어졌다.
			return true;
		}

	}

	public static ConcurrentHashMap<String, Session> getSessionContainer(){
		return sessionContainer;
	}
}
