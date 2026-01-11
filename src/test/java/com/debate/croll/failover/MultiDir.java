package com.debate.croll.failover;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

import com.debate.croll.publisher.crawler.common.DirectoryUrl;
import com.debate.croll.publisher.crawler.manager.FileManager;

public class MultiDir {

	@Test
	public void createDir() throws InterruptedException {
		for(int i=0; i<7; i++){
			FileManager fileManager = new FileManager();
			fileManager.createDir(DirectoryUrl.CRAWLER_LOG_BASE_DIR+i);
			Thread.sleep(1000);
		}
	}


	@Test
	public void init() throws InterruptedException {

		// 0~6까지는 42개
		for(int t=0; t<6; t++){

			for(int i=0; i<=42*30; i++){

				File file = new File(DirectoryUrl.CRAWLER_LOG_BASE_DIR+t+"\\"+i);

				try {
					file.createNewFile();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}

			}
		}

		Thread.sleep(1000);
		// 7번은 748개
		for(int i=0; i<748*30; i++){

			File file = new File(DirectoryUrl.CRAWLER_LOG_BASE_DIR+6+"\\"+i);

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
				for (File subDir : files) {

					if (subDir.isDirectory()) {
						clearDirectory(subDir);
					}
				}
			}
		}

	}

	private void clearDirectory(File directory) {
		File[] children = directory.listFiles();

		if (children == null) return;

		for (File child : children) {

			if (child.isDirectory()) {
				// 하위 디렉토리는 재귀로 먼저 비움
				clearDirectory(child);
				// 하위 디렉토리 자체는 유지하므로 delete() 안 함
			} else {
				// 파일만 삭제
				if (!child.delete()) {
					System.out.println("파일 삭제 실패: " + child.getAbsolutePath());
				}
			}
		}
	}





	@Test
	public void findLatestFileinMultiDir(){

		long start = System.currentTimeMillis();

		findLatestFileName();

		long end = System.currentTimeMillis();

		long elapsedMs = end - start;

		System.out.println("걸린 시간: " + elapsedMs + " ms");


	}

	public void findLatestFileName(){

		// 1. 최신 Dir 가져오기
		FileManager fileManager = new FileManager();
		File latestDir = fileManager.getLatestDir();

		//System.out.println("Latest Dir is "+latestDir.getName());

		// 2. 1.에서 최신 로그 가져오기.
		File latestFile = null;

		if(latestDir.exists() && latestDir.isDirectory()) {
			File[] files = latestDir.listFiles();

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
