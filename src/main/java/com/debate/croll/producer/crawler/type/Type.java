package com.debate.croll.producer.crawler.type;

import lombok.Getter;

@Getter
public enum Type { // CRAWLER에서 어떤 부분에서 에러가 발생했는지에 대한 정보.
	DRIVER("DRIVER"),
	COMMUNITY("COMMUNITY"),
	NEWS("NEWS");

	private final String name;

	Type(String name) {
		this.name = name;
	}


}
