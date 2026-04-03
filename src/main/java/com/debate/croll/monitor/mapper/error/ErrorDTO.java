package com.debate.croll.monitor.mapper.error;

import com.debate.croll.producer.crawler.common.OriginClass;
import com.debate.croll.producer.crawler.common.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorDTO {

	private Long id;

	private OriginClass originClass; // 어떤 클래스에서 발생?

	private Type type; // 작업 타입은 무엇인가? news? community?

	private String exceptionClass; // 예외가 무엇인가?

	private String message; // 에러 메시지는 뭐지?

	private String name; // 발생 객체 이름은?

	private String createdAt;

}
