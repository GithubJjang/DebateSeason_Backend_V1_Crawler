package com.debate.croll.heartbeat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeatManagerV1 {

	// Atomic하게 관리.
	private final AtomicInteger userCount = new AtomicInteger(0);

	// 스레드 풀.
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	// scheduledTask.
	ScheduledFuture<?> scheduledTask;

	private int operationTime = 0;

	private int killOperationTime = 0;

	public void increment(){

		if(userCount.incrementAndGet()==1){ // 만약 반환해서 처리를 할 경우, 동시성 문제가 발생할 수 있기 때문에 즉시 처리!
			operationTime++;

			scheduledTask = executor.scheduleAtFixedRate(

					()-> System.out.println("ping")
				,
				0, 1, TimeUnit.SECONDS
			); // 재시작 기능은 존재하지 않고, 재예약(재할당)만 존재한다.

		}
	}


	public void decrement(){

		int result = userCount.decrementAndGet(); // 동시에 접근을 하더라도, 원자적으로 처리를 해서 스레드마다 가지는 값은 각각 다르다.

		if(result<=0){ // 혹시 모를 음수에 대비해서, 방어 로직으로 작성.

			System.out.println("kill the Scheduler");
			killOperationTime++;

			userCount.set(0);

			if (scheduledTask != null) { // 오직 1개만 사라지도록 처리.
				scheduledTask.cancel(true);
				scheduledTask = null;
			}

		}

	}

	public int getUserCount(){
		return userCount.get();
	}

	//
	public int getOperationTime(){
		return operationTime;
	}

	public int getKillOperationTime(){
		return killOperationTime;
	}

	//
	public void shutdownScheduler(){

		if(scheduledTask!=null){
			System.out.println("successfully shutdown Scheduler");
			scheduledTask.cancel(true);
		}
		else{
			System.out.println("Scheduler Already Shutdown!");
		}
	}

}
