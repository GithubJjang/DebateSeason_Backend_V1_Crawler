package com.debate.croll.publisher.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.debate.croll.logger.repository.CrawlerErrorEventRepository;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Component
public class FailCounter {

	private final CrawlerErrorEventRepository crawlerErrorEventRepository;

	public final static AtomicInteger currentFailCount = new AtomicInteger(0);

	public static int preFailCount = 0; // 에러 로그 전송을 제어하기 위한 조치.


	@PostConstruct
	public void initCountFail(){ // 세션이 사라져도 얼만큼 실패를 했는지에 대해서 복구를 할 수 있다.

		Long recoveryFailCount = crawlerErrorEventRepository.countTodayErrors();
		currentFailCount.set(recoveryFailCount.intValue());

		preFailCount = currentFailCount.get();//

	}

	public static void count(){ // 에러가 발생할 때마다 카운트롤 한다.
		currentFailCount.incrementAndGet();
	}

	public static int get(){
		return currentFailCount.get();
	}

	public static boolean comparePreFailCountToCurrentFailCount(){

		if(preFailCount == currentFailCount.get()){ //
			return true;
		}
		else{
			return false;
		}

	}

	public static void updatePreFailCount(){
		preFailCount = currentFailCount.get();
	}

}
