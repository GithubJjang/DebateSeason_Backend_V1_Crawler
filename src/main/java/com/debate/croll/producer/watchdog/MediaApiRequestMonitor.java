package com.debate.croll.producer.watchdog;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MediaApiRequestMonitor {

	private final AtomicReference<LocalDateTime> lastRequestedAt =
		new AtomicReference<>(null);

	public void markRequested() { // 새 요청이면, 시간을 초기화
		lastRequestedAt.set(LocalDateTime.now());
	}

	public LocalDateTime getTime(){
		return lastRequestedAt.get();
	}

	public void setInit(){
		lastRequestedAt.set(null);
	}

}
