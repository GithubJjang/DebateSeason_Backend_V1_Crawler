package com.debate.croll.komoran;

import java.util.List;

import org.junit.jupiter.api.Test;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

public class AnalyzeTest {

	@Test
	public void testFilter(){
		// Mecab-ko도 고려를 했으나, gradle 추가한다고 되는게 아니라서 관리하기가 설정,관리가 매우 까다롭다.
		// 그리고, 에러 생기면 누가 관리???

		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		String strToAnalyze = "무인기 날린 사람";

		/*
		String content = "13일 윤석열 전 대통령의 내란 우두머리 혐의에 대한 조은석 특별검사팀의 사형 구형을 끝으로, 12·3 비상계엄을 둘러싼 주요 피고인들의 1심 변론 절차가 모두 마무리됐다. 이달 중순부터 다음달까지는 핵심 내란 피고인들의 1심 선고가 잇따른다. 헌법재판소 탄핵재판을 통해 위헌·위법성이 인정된 윤 전 대통령의 비상계엄 선포를 내란죄로 단죄할 것인지 사법부가 첫 답을 내놓을 시간이 다가온 것이다.\n"
			+ "\n"
			+ "당장 오는 16일 서울중앙지법 형사35부(재판장 백대현)는 고위공직자범죄수사처의 체포영장 집행을 방해한 혐의(특수공무집행방해) 등으로 기소된 윤 전 대통령 사건을 선고한다. 조은석 특별검사팀이 윤 전 대통령에게 구형한 형량은 징역 10년이다.\n"
			+ "\n"
			+ "12·3 비상계엄에 가담한 이들을 내란죄로 처벌이 가능한지 가늠할 수 있는 첫 판단은 오는 21일 예정된 한덕수 전 국무총리의 1심 선고(형사33부, 재판장 이진관)에서 나온다. 한 전 총리는 내란 우두머리 방조 등 혐의로 기소됐는데, 유죄가 선고되면 계엄의 법적 성격을 내란으로 공인하는 첫 판결이 된다. 내란 주요 피고인들 사건에도 가이드라인이 될 전망이어서, 언론사 단전·단수 지시 혐의(내란 중요임무 종사)로 기소돼 오는 2월12일 1심 선고가 나오는 이상민 전 행정안전부 장관 사건(형사32부, 재판장 류경진)에도 영향을 미칠 것으로 보인다. 한 전 총리와 이 전 장관에게는 각각 징역 15년이 구형됐다."
			+ "내란죄 본류 사건인 윤 전 대통령과 군경 지휘부 등 8명의 1심 선고일은 오는 2월19일이다. 전날 서울중앙지법 형사25부(재판장 지귀연) 심리로 열린 결심공판에서 특검팀은 윤 전 대통령에게 사형을, 김용현 전 국방부 장관과 노상원 전 정보사령관에게 각각 무기징역과 징역 30년을 구형했다. 박억수 특검보는 전날 최후의견 진술에서 “전두환·노태우 세력보다 더 엄정하게 단죄함으로써 대한민국이 형사사법 시스템을 통해 스스로 헌정질서를 수호할 수 있음을 보여줘야 할 것”이라고 했다. 윤 전 대통령의 경우 전례가 없는 현직 대통령의 내란 행위라는 점에서 대통령의 비상대권 행사가 어느 선까지 허용되는지 법원이 처음으로 법률적 판단을 제시하게 된다. 지귀연 재판장은 이 사건의 역사적 무게를 의식한 듯 “오직 헌법과 법률 증거에 따라 판단하겠다”고 밝혔다.\n"
			+ "\n"
			+ "윤 전 대통령은 내란 및 체포 방해 사건 외에도 6건의 형사사건 재판을 동시에 받고 있다. 평양 무인기 침투 작전 지시 혐의(일반이적) 사건은 지난 12일 재판이 시작됐고, 한 전 총리 재판에서 “계엄 국무회의를 미리 계획했다”고 위증한 혐의 사건 등은 공판준비가 진행 중이다."
			+ "참여연대는 이날 성명을 내어 “재판부는 윤석열과 내란범들에게 조속히 중형을 선고함으로써 헌법 수호 의지를 분명히 천명하고, 민주주의를 지켜낸 주권자들의 명령에 응답해야 한다”고 촉구했다."
			;

		 */

		KomoranResult analyzeResultList = komoran.analyze(strToAnalyze);

		System.out.println(analyzeResultList.getPlainText());

		List<Token> tokenList = analyzeResultList.getTokenList();
		for (Token token : tokenList) {
			System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(), token.getEndIndex(), token.getMorph(), token.getPos());
		}



	}
}
