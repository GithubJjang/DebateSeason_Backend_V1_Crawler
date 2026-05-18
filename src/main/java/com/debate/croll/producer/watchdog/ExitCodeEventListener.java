package com.debate.croll.producer.watchdog;

import java.io.IOException;

import org.springframework.boot.ExitCodeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExitCodeEventListener {

	@EventListener
	public void onExitCodeEvent(ExitCodeEvent event) {

		// 0 or 1과 상관없이 종료 시그널이 발생하면, 무조건 Kill chromedriver
		try {
			log.info("Kill all left chromedriver");

			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
			Runtime.getRuntime().exec("taskkill /F /IM chrome.exe /T");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
