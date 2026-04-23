package com.debate.croll.producer.crawler.common;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomDelay {

	public long getCrawlerDelay(){

		return ThreadLocalRandom.current().nextLong(3000, 5000);
	}

}
