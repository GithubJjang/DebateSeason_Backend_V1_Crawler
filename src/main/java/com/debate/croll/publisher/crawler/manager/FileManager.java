package com.debate.croll.publisher.crawler.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.debate.croll.publisher.crawler.common.DirectoryUrl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileManager {

	// Refactoring
	// 1. 최신 디렉토리 가져오고, 그 안에서 최신 로그 가져오기
	public String getLatestLogFile(){

		// Dir 내부 풀 스캔하기. 만약 섹션별 디렉토리 내부가 전부 비어있다면, 처음 서버에 올린 것 -> 장애 복구를 하지 않는다.
		if(isEveryDirEmtpyInCrawlerLogs()){
			log.warn("The crawler first starts in this server");
			return null;
		}

		// 1. 최신 Dir 찾기
		File latestDir = getLatestDir();

		if(isFileCreatedToday(latestDir)){ // 오늘 만들어진 것이다 -> Dir 내부에서 최신 파일 찿기.

			// 3. 최신 Dir 내부에서 최신 파일 찾기
			File latestLogFile = getLatestLog(latestDir);

			if(latestLogFile==null){ // 디렉토리 초기화 직후, 종료된 경우
				log.error("The latestLogFile not found.");
				log.error("it may be shutdown right after update Dir");

				// 특수 케이스이므로, 다르게 예외 처리
				return null;
			}
			else { // 최신 파일이 있으니까, 확장자(.txt) 떼고 주자.

				return removeExtension(latestLogFile);
			}



		}
		else{ // latestDir이 오래되었으므로, 재복구를 할 수 없다.

			// 시간 -> 날짜로 가공.
			DateTimeProcessor dateTimeProcessor = new DateTimeProcessor();
			LocalDate modifiedDate = dateTimeProcessor.modifiedDateToLocalDate(latestDir);

			log.error("The latestDir is creatd at "+modifiedDate);
			log.error("but today is "+LocalDate.now());

			return null;

		}

	}

	// 2. 최신 Dir 찾기
	public File getLatestDir(){

		File dir = new File(DirectoryUrl.CRAWLER_LOGS);

		// 1. 최신 Dir 찾기
		File latestDir = null;

		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				latestDir = Arrays.stream(files)
					.filter(File::isDirectory)
					.max(Comparator.comparingLong(File::lastModified))
					.orElse(null);
			}
		}

		return latestDir;
	}

	// 3. 특정 Dir에서 최신 로그 찾기
	public File getLatestLog(File latestDir){

		File latestLogFile = null;

		if (latestDir != null && latestDir.exists() && latestDir.isDirectory()) {
			File[] logFiles = latestDir.listFiles(File::isFile);

			if (logFiles != null && logFiles.length > 0) {
				latestLogFile = Arrays.stream(logFiles)
					.max(Comparator.comparingLong(File::lastModified))
					.orElse(null);
			}
		}

		return latestLogFile;
	}


	// 4. 오늘 만들어진 파일( 또는 Dir )인가요?
	private boolean isFileCreatedToday(File f){

		// File에서 lastModified 데이터 가져오기
		LocalDateTime rawModifiedTime = LocalDateTime.ofInstant(
			Instant.ofEpochMilli(f.lastModified()),
			ZoneId.systemDefault()
		);

		// 파일 마지막 수정 시각 → 날짜
		LocalDate modifiedDate = rawModifiedTime.toLocalDate();

		// 오늘 날짜 (서버 기준)
		LocalDate today = LocalDate.now(ZoneId.systemDefault());

		// 오늘인지 비교
		return modifiedDate.isEqual(today); // 오늘 날짜다 -> true, 오래 전이다 -> false;

	}

	// 5. 확장자 제거하기
	private String removeExtension(File file){

		String fileName = file.getName();
		return fileName.substring(0, fileName.lastIndexOf("."));

	}

	// 6. crawler-logs 디렉토리 내부 풀 스캔
	private boolean isEveryDirEmtpyInCrawlerLogs(){ // 서버에 최초 가동 -> 장애 복구를 하지 않음.

		boolean isDirEmpty = true;

		File dir = new File(DirectoryUrl.CRAWLER_LOGS);

		if (dir.exists() && dir.isDirectory()) {

			File[] files = dir.listFiles(); // BobaeDream, Clien, ...

			if (files != null) {

				for (File f : files) {
					if (f.isDirectory()) {

						// 가져온 디렉토리 내부가 비었는지 확인하는 로직
						// 만약 최소 1개 이상 있는 경우라면, false줘서 break를 건다.
						File[] innerFiles = f.listFiles();

						if (innerFiles != null && innerFiles.length > 0) {
							isDirEmpty = false;
							break;
						}
					}
				}
			}
		}

		return isDirEmpty;

	}

	// Legacy
	// 1. 최신 파일 1개 불러오기. 이는 복구를 하기 위함이다.
	public String getSingleRecentFile(){

		String recentLog;

		recentLog = getSingleRecentFileFromCrawlerLogs(); // 디렉토리 풀 스캔

		return recentLog;
	}

	public String getSingleRecentFileFromCrawlerLogs(){
		File dir = new File(DirectoryUrl.CRAWLER_LOGS);// "C:\\crawler-logs"
		//File dir = new File("/home/tarto123z/crawler-logs");

		File latestFile = null;

		if(dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				latestFile = Arrays.stream(files)
					.filter(File::isFile)
					.max(Comparator.comparingLong(File::lastModified))
					.orElse(null);
			}
		}

		// 여기서 최신 파일 로그에 대한 검증이 필요하다.
		// 1. 디렉토리에 로그가 있나요?
		// 2. 있다면, 그 로그는 오래된 것은 아닌가요?

		if (latestFile != null) {

			log.info("Latest file: " + latestFile.getName());

			if(isCheckpointCreatedToday(latestFile)){ // 체크 포인트가 오늘 날짜라면, 그거 로그로 쓰고

				String fileName = latestFile.getName();
				return fileName.substring(0, fileName.lastIndexOf("."));

			}
			else
			{ // 그렇지 않다면, null을 준다. -> 오래된 로그 안쓴다.
				log.error("The log file is too old.");
				return null;

			}
		} else {
			log.error("No file found."); // 없는 것도 처리해야 함.
			return null;
		}
	}

	// 2. 파일 파싱하기
	public String[] splitFileName(String fileName){

		return fileName.split("-");
	}

	// 3. 파일 전체 불러오기.
	private File[] getAllFiles(){

		File dir = new File(DirectoryUrl.CRAWLER_LOGS); // C:\\offsetTest C:\\crawler-logs

		File[] files = dir.listFiles();

		if(files==null){
			throw new NullPointerException("존재하지 않는 디렉토리이거나, 접근을 할 수 없습니다");
		}
		else if(files.length==0){
			throw new RuntimeException("디렉토리가 비었습니다"); // 앞에서 catch하지 못하면 메소드 전체가 멈춘다.
		}
		else{
			return files;
		}

	}

	// 4. 파일 전체 갯수 세기
	public int countTotalProgressLogs(){

		return getAllFiles().length;

	}

	// 5. 파일에서 날짜 추출하기
	private boolean isCheckpointCreatedToday(File f){

		// File에서 lastModified 데이터 가져오기
		LocalDateTime rawModifiedTime = LocalDateTime.ofInstant(
			Instant.ofEpochMilli(f.lastModified()),
			ZoneId.systemDefault()
		);

		// 파일 마지막 수정 시각 → 날짜
		LocalDate modifiedDate = rawModifiedTime.toLocalDate();

		// 오늘 날짜 (서버 기준)
		LocalDate today = LocalDate.now(ZoneId.systemDefault());

		// 오늘인지 비교
		return modifiedDate.isEqual(today); // 오늘 날짜다 -> true, 오래 전이다 -> false;

	}

	// 6. 성공 파일 추출하기.
	public List<ProgressLogFormatter> extractSuccessFileInfo(){ // 파일 로그 중에서 성공한 부분만 가져오기.

		// 1. 파일을 오름차순으로 정렬을 한다.
		File[] files = getAllFiles(); // 이거는 순서를 보장하지 않으므로, 아래와 같이 조치를 해야함.

		Arrays.sort(files, Comparator.comparingLong(File::lastModified));


		List<ProgressLogFormatter> progressLogFormatterList = new ArrayList<>();

		// 2.
		DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for(File f : files){

			// File에서 오늘 날짜 추출하기. YYYY-MM-DD
			LocalDate fileDate = Instant.ofEpochMilli(f.lastModified())
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

			LocalDate today = LocalDate.now();


			ProgressLogFormatter progressLogFormatter = null; // null이 맞을까?

			if(fileDate.equals(today)){ // 가져온 로그 날짜 == 오늘 날짜

				String name = f.getName();

				LocalDateTime rawModifiedTime = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(f.lastModified()),
					ZoneId.systemDefault()
				);
				String modifiedDate = rawModifiedTime.format(formatter);


				progressLogFormatter = ProgressLogFormatter.builder()
					.name(name)
					.modifiedDate(modifiedDate)
					.build()
					;
			}

			progressLogFormatterList.add(progressLogFormatter);
		}

		return progressLogFormatterList;

	}

	public String calcProgress( // 현재 진행률 반환
		int total,
		int success,
		int fail
	){

		// 3.
		double rate = (success + fail) * 100.0 / total;

		String progress =
			(rate == Math.floor(rate))
				? String.valueOf((int) rate)
				: String.format("%.1f", rate);

		return progress;
	}

	// 7. 디렉토리 생성하기
	public void createDir(String url){

		try {

			Path path = Paths.get(url);	// "C:\\crawler-logs\\name"
			Files.createDirectories(path); // 디렉토리가 이미 있다고, 중복 생성되는 문제가 발생하지 않는다.

		} catch (IOException e) {
			// 에러 가능성은 1. 경로 설정 문제, 2. 접근 권한 문제,
			log.error("fail to create Dir. because "+ e.getMessage());
		}

	}

	public void updateDirCreatedDate(String url){

		try {

			Path dir = Paths.get(url); // "C:\\crawler-logs\\name"
			FileTime now = FileTime.from(Instant.now());
			Files.setLastModifiedTime(dir, now);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}