package com.debate.croll.producer.crawler.mapper.checkpoint;

import com.debate.croll.producer.crawler.common.Status;
import com.debate.croll.producer.crawler.type.Type;

public interface CheckPointProjection { // CheckPoint테이블에서 필요한 것만 가져온다.

	String getName();
	Integer getSubKey();
	Integer getCrawlIndex();

	Type getType();

	Status getStatus();
}
