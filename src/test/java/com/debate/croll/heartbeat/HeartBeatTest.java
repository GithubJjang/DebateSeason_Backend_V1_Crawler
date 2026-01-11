package com.debate.croll.heartbeat;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.debate.croll.publisher.monitor.sseEmitter.session.Session;

public class HeartBeatTest {

	@RepeatedTest(10)
	public void concurrentAccessSafely() throws InterruptedException { // 동시어 접근시, 동시성 테스트

		ExecutorService executorService = Executors.newFixedThreadPool(6);

		//
		int executeCount = 5000000;

		SessionContainerV1 sessionContainerV1 = new SessionContainerV1();
		CountDownLatch countDownLatch = new CountDownLatch(executeCount);

		//
		for(int i=0; i<5000000; i++){

			String userId = "userId"+i;

			executorService.execute(
				()->{
					Session session = new Session(new SseEmitter(),0,0); // 세션 만들기
					sessionContainerV1.addSession(userId,session); // 사용자와 함께 저장
					countDownLatch.countDown();
				}
			);
		}

		countDownLatch.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다

		assertThat(sessionContainerV1.getSessionContainerSize()).isEqualTo(executeCount);// 5,000,000
		assertThat(sessionContainerV1.getUserCount()).isEqualTo(executeCount);// 5,000,000
		assertThat(sessionContainerV1.getSessionContainerSize()).isEqualTo(sessionContainerV1.getUserCount()); // 5,000,000
		assertThat(sessionContainerV1.getOperationTime()).isEqualTo(1);

		System.out.println("The SessionContainer Size is "+ sessionContainerV1.getSessionContainerSize());
		System.out.println("The UserCount is "+ sessionContainerV1.getUserCount());
		System.out.println("The Opeation time is "+sessionContainerV1.getOperationTime());

		sessionContainerV1.clearSessionContainer(); // 사용자를 저장하는 ConcurrentHashMap 초기화.
		sessionContainerV1.shutdownScheduler();
	}


	@RepeatedTest(10)
	public void concurrentDeleteSafely() throws InterruptedException {

		// 1. 사용자 5,000,000명 초기화 작업 하기.
		ExecutorService executorService = Executors.newFixedThreadPool(6);

		//
		int executeCount = 5000000;

		SessionContainerV1 sessionContainerV1 = new SessionContainerV1();
		HeartBeatManagerV1 heartBeatManagerV1 = sessionContainerV1.getHeartBeartManagerV1();

		CountDownLatch countDownLatch = new CountDownLatch(executeCount);

		//
		for(int i=0; i<5000000; i++){

			String userId = "userId"+i;

			executorService.execute(
				()->{
					Session session = new Session(new SseEmitter(),0,0); // 세션 만들기
					sessionContainerV1.addSession(userId,session); // 사용자와 함께 저장
					countDownLatch.countDown();
				}
			);
		}

		countDownLatch.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다
		executorService.shutdown();
		System.out.println("Inserting session done!");

		// 2. 사용자 5,000,000명 삭제 작업 수행하기.
		System.out.println("start kill all Session");
		int executeCount2 = 5000000;
		ExecutorService executorService2 = Executors.newFixedThreadPool(6);
		CountDownLatch countDownLatch2 = new CountDownLatch(executeCount2);

		for(int i=0; i<executeCount2; i++){

			String userId = "userId"+i;

			executorService2.execute(
				()->{

					disconnectSession(userId, heartBeatManagerV1);
					countDownLatch2.countDown();

				}
			);
		}

		countDownLatch2.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다

		System.out.println();
		System.out.println("The SessionContainer Size is "+ sessionContainerV1.getSessionContainerSize());
		System.out.println("The UserCount is "+ sessionContainerV1.getUserCount());
		System.out.println("The kill Operation Time is "+ heartBeatManagerV1.getKillOperationTime());

		sessionContainerV1.clearSessionContainer(); // 사용자를 저장하는 ConcurrentHashMap 초기화.
		sessionContainerV1.shutdownScheduler();

	}

	public void disconnectSession(String userId, HeartBeatManagerV1 heartBeatManagerV1){

		// false는 세션이 안 끊어진 경우 -> 따라서, decrement를 실행
		if(SessionContainerV1.isSessionDisconnected(userId)==false){
			heartBeatManagerV1.decrement(); // atomic하게 -1
		}

	}

	/*
	@Test
	public void concurrentAccessWithDelete() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(6);

		// 1. 선은 1,000,000을 주입한다.
		SessionContainerV1 sessionContainerV1 = new SessionContainerV1();
		HeartBeartManagerV1 heartBeartManagerV1 = sessionContainerV1.getHeartBeartManagerDemo();

		int executeCount = 1000000;
		CountDownLatch countDownLatch1 = new CountDownLatch(executeCount);

		// 사용자 저장소
		List<String> userList = Collections.synchronizedList(new ArrayList<>());

		for(int i=0; i<executeCount; i++){

			String userId = "userId"+i;

			executorService.execute(
				()->{
					Session session = new Session(new SseEmitter(),null);
					sessionContainerV1.addSession(userId,session);
					countDownLatch1.countDown();

					userList.add(userId);
				}
			);
		}

		countDownLatch1.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다
		executorService.shutdown();      // 2️⃣ executor 종료


		System.out.println("--- Done add ---");

		// 2. 먼저 들어온 0~999,999는 제거하고, 1,000,000 ~ 1,999,999는 추가한다( 동시에 )
		int executeCount2 = 1000000;

		CountDownLatch countDownLatch2 = new CountDownLatch(executeCount2);

		ExecutorService executorService2 = Executors.newFixedThreadPool(6);


		for(int i =0; i<executeCount2; i++){

			String deleteUserId = userList.get(i);
			String addUserId = "userId"+(i+executeCount2);

			executorService2.execute(
				()->{

					if(SessionContainerV1.isSessionDisconnected(deleteUserId)==false){
						heartBeartManagerV1.decrement(); // atomic하게 -1
					}
					countDownLatch2.countDown();


				}
			);

			executorService2.execute(
				()->{
					Session session = new Session(new SseEmitter(),null);
					sessionContainerV1.addSession(addUserId,session);
					countDownLatch1.countDown();

					userList.add(addUserId);
				}
			);
		}

		countDownLatch2.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다
		executorService2.shutdown();

		System.out.println("The SessionContainer Size is "+ sessionContainerV1.getSessionContainerSize());
		System.out.println("The UserCount is "+ sessionContainerV1.getUserCount());
	}

	@Test
	public void bounceBack() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(20);

		// 1. 선은 1,000,000을 주입한다.
		SessionContainerV1 sessionContainerV1 = new SessionContainerV1();
		HeartBeartManagerV1 heartBeartManagerV1 = sessionContainerV1.getHeartBeartManagerDemo();


		int executeCount0 = 10;


		CountDownLatch countDownLatch0 = new CountDownLatch(executeCount0);
		for(int i=10; i<20; i++){


			String myName = ""+i;

			executorService.execute( // 비동기 실행 1.
				()->{

					System.out.println("i am inserting now. plz wait.");

					Session session = new Session(new SseEmitter(),null);
					sessionContainerV1.addSession(myName,session);
					countDownLatch0.countDown();

					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			);

		}
		System.out.println("i am starting now");

		for(int a=0; a<10; a++){

			int executeCount = 1000000;
			CountDownLatch countDownLatch1 = new CountDownLatch(executeCount);

			// 사용자 저장소
			List<String> userList = Collections.synchronizedList(new ArrayList<>());

			for(int i=0; i<executeCount; i++){

				String userId = "userId"+i;

				executorService.execute( // 비동기 실행 2.
					()->{
						Session session = new Session(new SseEmitter(),null);
						sessionContainerV1.addSession(userId,session);
						countDownLatch1.countDown();

						userList.add(userId);
					}
				);
			}

			countDownLatch1.await(); // executeCount만큼 다 셀 때까지, 먼저 끝난 스레드도 함께 대기를 한다

			int executeCount2 = 1000000;
			CountDownLatch countDownLatch2 = new CountDownLatch(executeCount2);

			// 2. 1,000,000 전원 제거
			for(String userId : userList){

				executorService.execute( // 비동기 실행 3.
					()->{

						if(SessionContainerV1.isSessionDisconnected(userId)==false){
							heartBeartManagerV1.decrement(); // atomic하게 -1
						}
						countDownLatch2.countDown();


					}
				);

			}

			String userName = ""+a;

			executorService.execute(
				()->{
					Session session = new Session(new SseEmitter(),null);
					sessionContainerV1.addSession(userName,session);
					countDownLatch1.countDown();

					userList.add(userName);
				}
			);

			System.out.println("The SessionContainer Size is "+ sessionContainerV1.getSessionContainerSize());
			System.out.println("The UserCount is "+ sessionContainerV1.getUserCount());

			countDownLatch2.await();
			countDownLatch1.await();

			System.out.println();

		}

		countDownLatch0.await();

		System.out.println("===< Final >===");

		System.out.println("The Final SessionContainer Size is "+ sessionContainerV1.getSessionContainerSize());
		System.out.println("The Final UserCount is "+ sessionContainerV1.getUserCount());

		sessionContainerV1.findKeys();

	}

	 */

}
