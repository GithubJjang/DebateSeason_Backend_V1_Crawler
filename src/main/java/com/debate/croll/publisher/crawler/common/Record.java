package com.debate.croll.publisher.crawler.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import com.debate.croll.publisher.crawler.manager.FileManager;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class Record {

	private String name;
	private int point;
	private Type type;
	private String category="";
	
	// 1. Community 경우
	public Record(String name,int point,Type type){
		this.name=name; // 엠엘비파크
		this.point=point; // 2
		this.type=type; // Community


	}

	// 2. News인 경우
	public Record(String name,int category,int point,Type type){
		this.name=name; // 조선일보
		this.category=String.valueOf(category); // 101
		this.point=point; // 2
		this.type=type; // News
	}

	// ex) Reboot-Clien-1-Community
	// 이를 .txt로 저장을 한 다음에, 리부트를 할 경우, 불러와서 다음 꺼를 실행을 해야하는데...
	@Override
	public String toString() {
		// ex) 커뮤니티 : Reboot-BobaeDream-3(point)-Community
		// ex) 뉴스    : Reboot-조선일보-2(point)-News-101

		if(this.type.name().equals("Community")){
			return Status.Reboot+"-"+this.name+"-"+this.point + "-" +type.name();
		}
		else{
			return Status.Reboot+"-"+this.name+"-"+this.point + "-" +type.name()+"-"+this.category;
		}


	}



	public void recordFile(){

		// 굳이 내용을 쓸 필요가 있을까? 어차피 제목에 쓰면 되는데...
		String logFile = this +".txt";

		File file = null;

		if(type==Type.Community){

			file = new File(DirectoryUrl.CRAWLER_LOG_BASE_DIR+name+"\\"+logFile); // "C:\\crawler-logs\\clien\\~.txt"

		}
		else if(type==Type.News){

			file = new File(DirectoryUrl.NEWS_LOG_BASE_DIR+logFile); // "C:\\crawler-logs\\News\\~.txt"

		}
		// ... 밑에 혹시나 추가 type이 있을 수도 있음. 따라서 else if로 만듦.



		//File file = new File(DirectoryUrl.LOCAL_FILE+logFile); // offsetTest, crawler-logs, "C:\\crawler-logs\\"
		//File file = new File("/home/tarto123z/crawler-logs/"+name);

		try {

			if(file.createNewFile()){ // 새로 로그를 찍을 경우

				log.info("create file : "+logFile);
			}
			else{ // 이미 로그가 찍힌 경우 -> 날짜만 갱신을 한다.

				//Path source = Paths.get(DirectoryUrl.LOCAL_FILE+name); // offsetTest, crawler-logs, "C:\\crawler-logs\\"

				// 1.
				log.warn("file already exists : "+logFile);

				if(type==Type.Community){

					Path source = Paths.get(DirectoryUrl.CRAWLER_LOG_BASE_DIR+name+"\\"+logFile);
					Files.setLastModifiedTime(source, FileTime.fromMillis(System.currentTimeMillis()));

				}
				else if(type==Type.News){

					Path source = Paths.get(DirectoryUrl.NEWS_LOG_BASE_DIR+logFile);
					Files.setLastModifiedTime(source, FileTime.fromMillis(System.currentTimeMillis()));

				}


			}

		}
		catch (IOException e){

			// 이 경우는 1. 디렉토리 x, 2. 접근 권한 제한.
			log.error("fail to create file : "+logFile);
			log.error(e.getMessage());

		}
	}
}
