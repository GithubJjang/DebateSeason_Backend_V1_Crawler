package com.debate.croll.transfer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.debate.croll.publisher.monitor.sseEmitter.session.Session;

import com.debate.croll.publisher.crawler.manager.FileManager;
import com.debate.croll.publisher.crawler.manager.ProgressLogFormatter;

public class SseHeartbeatSenderV2 {

	public void sendHeartbeat(){

		FileManager fileManager = new FileManager();

		// 1. { userId, SseSession } ,사용자마다 각자 다른 로그 정보들을 가지고 있다.
		ConcurrentHashMap<String, Session> sessionContainer = SessionContainerV2.sessionContainer;
		System.out.println("sendHeartbeat Method");

		try{

			if (sessionContainer.isEmpty()) {

				System.out.println("연결된 사용자가 없습니다.");
			}
			else{

				// 2. userId 목록 가져오기.
				Set<String> userIdSet = sessionContainer.keySet();// Read-Only

				List<ProgressLogFormatter> realTimeSuccessLogList = fileManager.extractSuccessFileInfo();

				System.out.println("run0");
				// 3.
				for(String userId : userIdSet){ // 사용자마다 별도로 전송을 해준다.
					System.out.println("run1");
					Session session = sessionContainer.get(userId);

					// 각 세션별로 Offset을 이용해서 변경분만 전송을 하자.
					Map<String,String> dirtyCheckedSuccessLogMap = dirtyCheckingSuccessLog(session,realTimeSuccessLogList); // 어차피 append-only라서 문제 x

					printOut(dirtyCheckedSuccessLogMap,session,userId);
				}

			}

		}
		catch (Exception e){
			System.out.println(e.getMessage());
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

	public void printOut(Map<String,String> dirtyCheckedSuccessLogMap,Session session,String userId){

		Set<String> keys = dirtyCheckedSuccessLogMap.keySet();

		System.out.println("userId : "+userId+" offset : "+session.getProgressLogOffset());

		for(String s : keys){
			System.out.println(s);
		}

	}

}
