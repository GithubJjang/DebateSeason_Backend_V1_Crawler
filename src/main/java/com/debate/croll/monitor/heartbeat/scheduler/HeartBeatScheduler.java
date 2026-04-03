package com.debate.croll.monitor.heartbeat.scheduler;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.debate.croll.monitor.heartbeat.sender.template.HeartBeatSender;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class HeartBeatScheduler {

	// Atomic하게 관리.
	private final AtomicInteger userCount = new AtomicInteger(0);

	// 스레드 풀.
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final HeartBeatSender heartBeatSender;

	// scheduledTask.
	ScheduledFuture<?> scheduledTask;

	public void increment(){

		if(userCount.incrementAndGet()==1){ // 만약 반환해서 처리를 할 경우, 동시성 문제가 발생할 수 있기 때문에 즉시 처리!

			scheduledTask = executor.scheduleAtFixedRate(
				()-> heartBeatSender.sendHeartbeat(this),
				0, 3, TimeUnit.SECONDS
			); // 재시작 기능은 존재하지 않고, 재예약(재할당)만 존재한다.

		}
	}

	public void decrement(){

		log.info("증가 이전 값 : "+userCount);

		int result = userCount.decrementAndGet();

		if(result<=0){
			log.info("kill the scheduler");
			userCount.set(0);

			if (scheduledTask != null) {
				scheduledTask.cancel(true);
				scheduledTask = null;
			}

		}

		log.info("현재 값 : "+userCount);

	}

}
