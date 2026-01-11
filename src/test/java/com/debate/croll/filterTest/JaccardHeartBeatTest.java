package com.debate.croll.filterTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class Data{
	Set<String> intersection;
	double accurate ;
}

class UseLessWordList{
	public static Set<String> filteredWordsList = new HashSet<>();
	
	static {
		filteredWordsList.add("벌레");
		filteredWordsList.add("李대통령");
		filteredWordsList.add("尹");
	}
}

public class JaccardHeartBeatTest {

	public static Data jaccardSimilarity(String[] listA, String[] listB) {

		Set<String> setA = new HashSet<>(List.of(listA));
		Set<String> setB = new HashSet<>(List.of(listB));

		// 교집합 크기
		Set<String> intersection = new HashSet<>(setA);
		intersection.retainAll(setB);

		// 합집합 크기
		Set<String> union = new HashSet<>(setA);
		union.addAll(setB);

		Data d = new Data();
		d.intersection = intersection;
		d.accurate = (double) intersection.size() / union.size();

		return d;
	}

	@Test
	public void testJaccard(){

		HashMap<String, Set<String>> keywordMap = new HashMap<>();

		// 여기선 불필요한 조사는 다 지운 상태
		String [] FilteredNews = {
			"교착 미 관세 협상 도요타 현대차",
			"가성비 경쟁 현대 기아 차 북미시장 한 일 격차",
			"여한구 통상 교섭본부장 방미 3 천 500 억달러 투자 세부 조율",
			"尹 내란 재판 10 회 연속 불출석 궐석재판 진행",
			"尹 내란재판 9회 불출석…중계 관련 신청 시점 검토중",
			"북측 적대 존중 체제 통일 행위 흡수 李대통령",
			"벌레 李대통령 대통령 국장 복귀 지능",
			"벌레 李대통령 2030 청년 타운홀 미팅 목소리"
		};

		int size = FilteredNews.length;
		int [] markingList = new int[size];


		Set<String> elseList = new HashSet<>();

		for(int i=0; i<size; i++){

			for(int j=i+1; j<size; j++){

				Data d = jaccardSimilarity(FilteredNews[i].split(" "),FilteredNews[j].split(" "));

				if(d.accurate>0){

					Set<String> set = d.intersection;

					for(String s : set){

						if(keywordMap.get(s)==null){
							// 새로 추가를 한다.
							Set<String> set2 = new HashSet<>();
							set2.add(FilteredNews[i]);
							set2.add(FilteredNews[j]);

							markingList[i] = -1;
							markingList[j] = -1;

							keywordMap.put(s,set2);

							elseList.add(FilteredNews[i]);
							elseList.add(FilteredNews[j]);
						}
						else{
							// 이미 있는 노드다.
							Set<String> set2 = keywordMap.get(s);
							set2.add(FilteredNews[j]);

							markingList[j] = -1;

							elseList.add(FilteredNews[j]);
						}


					}


					System.out.print(FilteredNews[i]+" & "+FilteredNews[j]);

					System.out.print("[ ");
					for(String s : set){
						System.out.print(s+" ");
					}
					System.out.println("]");

				}
			}
		}


		// 나머지 추가
		for(int i=0 ; i<size; i++){
			if(markingList[i]!=-1){
				elseList.add(FilteredNews[i]);
			}
		}

		keywordMap.put("etc",elseList);

		System.out.println();

		// 1. 주입을 한 후, 웹 뷰로 수정을 한다.
		Set<String> keySet = keywordMap.keySet();

		System.out.println("======");
		System.out.println();

		delete_UseLess_Category(keywordMap); // 1. 사전에 먼저 제거를 해준다.

		if(!keySet.isEmpty() && keySet.size()!=1){ // 길이가 1인 경우는 etc만 keySet은 남은 경우를 말한다.

			System.out.println("alarm");

			// 2. 알람을 받은 운영진은 확인을 한다.
			for(String s: keySet){
				System.out.println("key: "+s);
			}
			System.out.println();

			// 3. 쓸모없어 보이는 카데고리를 입력한다.
			String clickedWord = "임원 尹";

			String[] useLessWords = clickedWord.split(" ");

			// 4. 학습을 시킨다.
			// 이것만 영구적으로 기억 시킨다.
			UseLessWordList.filteredWordsList.addAll(Arrays.asList(useLessWords));

			// 5. 2차로 제거를 한번 더 해준다.
			delete_UseLess_Category(keywordMap);

			// 6. 확인
			Set<String> keys2 = keywordMap.keySet();
			for(String s : keys2){

				for(String s2: keywordMap.get(s)){
					System.out.print(s+" : ");
					System.out.println(s2);
				}

				System.out.println();

			}

		}

		else{
			System.out.println("by pass");
		}


	}

	public void delete_UseLess_Category( HashMap<String, Set<String>> keywordMap ){

		// 5. 그리고, 불필요한 카테고리는 삭제를 한다.
		for(String s: UseLessWordList.filteredWordsList){

			try{
				if (!keywordMap.get(s).isEmpty()){
					keywordMap.remove(s);
				}
			}
			catch (NullPointerException e){
				System.out.println("카테고리 ["+s+"]는 이미 삭제되었습니다.");
			}

		}
	}

}


