package com.debate.croll.producer.common;

import lombok.Getter;

@Getter
public enum Status {
	SUCCESS(200, "OK"),
	BAD_REQUEST(400, "Bad Request"),
	INTERNAL_ERROR(500, "Server Error");

	private final int code;
	private final String message;

	Status(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
