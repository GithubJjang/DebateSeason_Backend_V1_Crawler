# 📌 DebateSeason 데이터 수집을 위한 Crawler

## 🧾 Overview

DebateSeason은 사용자에게 채팅 및 다양한 미디어 정보를 제공하는 서비스입니다.
본 프로젝트는 해당 기능을 지원하기 위해 **외부 웹 데이터를 안정적으로 수집하는 크롤러 시스템**을 구현한 것입니다.

단순한 데이터 수집을 넘어, **중단 상황에서도 복구 가능한 구조와 제한된 리소스 환경에서의 안정적인 운영**을 목표로 설계되었습니다.

---

## 🚀 Features

* 웹 페이지 기반 데이터 크롤링
* 수집 데이터 DB 저장 및 관리
* 크롤링 작업 단위별 체크포인트 기록
* 중단 이후에도 이어서 작업 가능한 복구 기능

---

## ⚙️ Tech Stack

* **Backend**: Java, Spring Boot
* **Database**: SQLite
* **Crawling**: Selenium
* **ORM**: Spring Data JPA

---

## 🏗️ Architecture & Key Design

### 🔄 체크포인트 기반 작업 재개

* 크롤링 작업 단위마다 **체크포인트를 기록**
* 시스템 중단(서버 재부팅 등) 발생 시
  → 마지막 처리 지점부터 작업 재개 가능
* 데이터 수집 과정의 **연속성과 안정성 확보**

---

### ⚡ 제한된 리소스 환경 최적화

* GCP e2 환경 (1 vCPU / 4GB RAM) 기준으로 설계
* 기존 **멀티 스레드 병렬 처리 → 단일 스레드 순차 처리로 변경**

  * 제한된 리소스 환경에서의 과부하 방지
  * 안정적인 장기 실행을 위한 구조 선택

---

### 🛡️ 스로틀링 기반 안정성 확보

* 요청 간 간격(Throttling) 적용
* CPU 및 메모리 사용량 제어
* 외부 사이트 차단 및 과도한 요청 방지

---

## ▶️ Usage

### 📊 데이터 수집 흐름

1. 크롤링 대상 웹 페이지 접근
2. 데이터 추출 및 가공
3. DB 저장
4. 체크포인트 기록
5. 중단 시 마지막 체크포인트 기준으로 재시작

---

## 📂 Project Structure

```
src/
 ├── domain/repository     # 도메인 Repository 계층
 ├── infrastructure/       # 엔티티 및 DB 모델
 ├── monitor/              # 크롤러 상태 모니터링 모듈
 ├── producer/             # 크롤링 실행 모듈
 └── webdriver/            # Selenium 기반 WebDriver 관리
```

---

## 🎯 Highlights

* 체크포인트 기반 설계로 **중단 이후에도 이어서 실행 가능한 안정적인 크롤링 구조**
* 제한된 클라우드 환경에서 **리소스 효율성을 고려한 실행 전략 선택**
* 스로틀링 적용으로 **시스템 안정성과 외부 서비스 보호**

---

## 👤 Author

* DebateSeason Team(Developed by GithubJjang)
