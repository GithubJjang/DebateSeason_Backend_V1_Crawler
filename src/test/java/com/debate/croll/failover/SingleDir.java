package com.debate.croll.failover;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import com.debate.croll.publisher.crawler.common.DirectoryUrl;
import com.debate.croll.publisher.crawler.manager.FileManager;

public class SingleDir {

	@Test
	public void init(){

		// 3000 건 쓰기
		for(int i=0; i<30000; i++){
			File file = new File(DirectoryUrl.CRAWLER_LOG_BASE_DIR+i);

			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Test
	public void clear(){

		File dir = new File(DirectoryUrl.CRAWLER_LOG_BASE_DIR);

		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();

			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						if (!file.delete()) {
							System.out.println("삭제 실패: " + file.getName());
						}
					}
				}
			}
		}

	}


	@Test
	public void findLatestFileinSingleDir(){

		long start = System.currentTimeMillis();

		findLatestFileName();

		long end = System.currentTimeMillis();

		long elapsedMs = end - start;

		System.out.println("걸린 시간: " + elapsedMs + " ms");


	}

	public void findLatestFileName(){

		File dir = new File(DirectoryUrl.CRAWLER_LOGS);

		File latestFile = null;

		if(dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();

			System.out.println("The files is Dir are "+files.length);

			if (files != null && files.length > 0) {
				latestFile = Arrays.stream(files)
					.filter(File::isFile)
					.max(Comparator.comparingLong(File::lastModified))
					.orElse(null);
			}
		}

		System.out.println("Latest FileName is "+latestFile);
	}

}
