package com.debate.croll.heartbeat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.publisher.monitor.sseEmitter.session.Session;

public class SessionContainerV1 {

	private final HeartBeatManagerV1 heartBeatManagerV1;

	public SessionContainerV1(){
		heartBeatManagerV1 = new HeartBeatManagerV1();
	}

	// 인스턴스에 종속되지 않음.
	public final static ConcurrentHashMap<String, Session> sessionContainer
		= new ConcurrentHashMap<>();

	public void addSession(String userId, Session session){

		// 아무것도 매핑되는 값이 없는 경우에, null을 반환하고, if문으로 중복 생성을 방지.
		if(sessionContainer.putIfAbsent(userId, session)==null){ // 버킷 단위 락이라서 ok
			heartBeatManagerV1.increment();
		}
		else{
			System.out.println("이미 존재하는 세션 : "+userId);
		}
	}


	public static synchronized boolean isSessionDisconnected(String userId){

		// 1. Sse연결을 끊고 ( 이렇게 직접 종료를 하지 않을 경우, 비정상적인 세션 종료에 의한 에러 발생.)

		Session session = sessionContainer.get(userId);

		if(session!=null){ // 세션이 끊어지지 않았다. false
			SseEmitter sseEmitter = session.getSseEmitter();
			sessionContainer.remove(userId); // remove는 MapEntry에서만 제거 -> value가 다른 객체에 참조 되고 있다면, 여전히 메모리 누수가 발생함...
			sseEmitter.complete(); // 명확하게 세션을 종료해야만 함!. 그리고, null에 대해서 실행을 하면 exception 발생

			return false;
		}
		else{ // session == null -> 세션이 끊어졌다.
			return true;
		}

	}


	// 결과 반환
	public int getSessionContainerSize(){ // 컨테이너에 저장된 사용자 수
		return sessionContainer.size();
	}

	public void findKeys(){

		Set<String> keys = sessionContainer.keySet();

		for(String key : keys){
			System.out.println("key : "+key);
		}

	}

	public HeartBeatManagerV1 getHeartBeartManagerV1(){
		return heartBeatManagerV1;
	}

	public void clearSessionContainer(){
		sessionContainer.clear();
	}


	//
	public int getUserCount(){
		return heartBeatManagerV1.getUserCount();
	}

	public int getOperationTime(){
		return heartBeatManagerV1.getOperationTime();
	}

	public void shutdownScheduler(){
		heartBeatManagerV1.shutdownScheduler();
	}

}
