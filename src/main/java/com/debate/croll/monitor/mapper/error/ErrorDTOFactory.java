package com.debate.croll.monitor.mapper.error;

import com.debate.croll.infrastructure.entity.ErrorEntity;

public class ErrorDTOFactory {

	public ErrorDTO ErrorEntityToErrorDTO(ErrorEntity errorEntity){

		return ErrorDTO.builder()
			.id(errorEntity.getId())
			.originClass(errorEntity.getOriginClass())
			.type(errorEntity.getType())
			.exceptionClass(errorEntity.getExceptionClass())
			.message(errorEntity.getMessage())
			.name(errorEntity.getName())
			.createdAt(errorEntity.getCreatedAt())
			.build();

	}
}
