package com.debate.croll.producer.crawler.common;

import lombok.Getter;

@Getter
public enum Status {
	STEADY("STEADY"),
	REBOOT("REBOOT"),
	DONE("DONE");

	private final String name;

	Status(String name) {
		this.name = name;
	}
}
