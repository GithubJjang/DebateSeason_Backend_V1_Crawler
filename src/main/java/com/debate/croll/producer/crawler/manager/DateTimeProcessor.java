package com.debate.croll.producer.crawler.manager;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateTimeProcessor {

	public LocalDate modifiedDateToLocalDate(File file){

		long lastModified = file.lastModified();

		LocalDate modifiedDate =
			Instant.ofEpochMilli(lastModified)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		return modifiedDate;
	}

}
