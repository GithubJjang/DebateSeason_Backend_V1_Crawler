package com.debate.croll.producer.common;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomDelay {

	public long getCommunityCrawlerDelay(){

		return ThreadLocalRandom.current().nextLong(3000, 5000);
	}

	public long getNewsCrawlerDelay(){
		return ThreadLocalRandom.current().nextLong(2000, 3000);
	}

}
