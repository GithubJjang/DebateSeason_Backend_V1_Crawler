package com.debate.croll.producer.common;

import org.springframework.stereotype.Component;

@Component
public class ExceptionClassfier { // title이 중복인지 아닌지 판별을 한다. 만약 중복이 아니라면, url을 허용한다.

	public boolean isUniqueConstraintViolation(Exception exception) {

		// 타입 확인
		if(exception instanceof org.springframework.orm.jpa.JpaSystemException){

			// 에러 message를 얻고,
			String[] arr = exception.getMessage().split("\\n");
			String message = arr[0];

			// UniqueConstraintViolation인지 확인을 한다.
			if(message != null &&
				(message.contains("UNIQUE constraint failed")||message.contains("SQLITE_CONSTRAINT_UNIQUE"))){
				return true;
			}
			else {
				return false;
			}

		}
		return false;
	}
}
