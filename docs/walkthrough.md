# HGU Logic Design 보너스 과제 설계 및 마이그레이션 결과 보고서 (Walkthrough)

본 문서는 **VS Code에서 IntelliJ로 성공적으로 세션 컨텍스트를 이전(Migration)**하고, 사용자님의 중대한 아키텍처적 의사결정을 반영하여 **Java 기반 2단 논리 간소화(Two-Level Logic Minimizer) 프로그램의 핵심 설계 및 대비 자료**를 완성한 전체 과정을 요약한 보고서입니다.

---

## 1. 주요 달성 사항 및 마이그레이션 내역

### 1.1. 세션 동기화 (VS Code $\rightarrow$ IntelliJ)
* 이전 VS Code 세션(`28d4d8bf-b4a2-41c5-a1b8-85bb1076d59e`)에서 축적되었던 모든 구현 계획서와 자료구조 초안을 안전하게 현재 활성 IntelliJ 세션(`48de8e19-e686-4fe4-9526-d327ee423b34`)으로 병합 및 복구했습니다.

### 1.2. 핵심 아키텍처 의사결정 반영
* **최대 변수 개수 ($N=10$)**: Minterm 수가 최대 1024개로 비트 연산에 가장 적합한 크기이므로, 32-bit `int`를 이용한 **초고속 비트마스킹(Bitmasking)** 기법을 활용하기로 결정했습니다.
* **순환 표 해결법**: 필수 주임플리컨트(EPI) 제거 및 지배 법칙 적용 후에도 잔여 표가 남는 순환 구조(Cyclic Table)에서, 교수님 PDF 개념 설명에 따라 **Branch-and-Bound(가지치기 분기 완전 탐색) 알고리즘**을 적용하기로 확정했습니다.

---

## 2. 설계 가이드 산출물 안내 (마일스톤 완료)

사용자께서 직접 코드를 안전하게 짜고 면접을 통과하실 수 있도록, 아티팩트 및 스크래치 폴더에 아래의 **4종 완결판 설계 패키지 문서**를 생성해 두었습니다.

````carousel
# [1] Java OOP 자료구조 설계서
[data_structure_design.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/data_structure_design.md)
* Java의 객체지향적 특성을 살린 `Implicant`, `QuineMcCluskey`, `PIChart` 클래스의 모듈 구조 정의.
* $N \le 10$에서 `mask`와 `value` 비트 필드를 이용해 대시(`-`) 상태와 병합을 구현하는 핵심 방식 설명.

<!-- slide -->
# [2] 제안서 (Proposal) 초안
[proposal_draft.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/proposal_draft.md)
* 13주차 화요일 마감인 공식 과제 제안서 형식의 초안.
* 비트마스킹 수학적 모델링, Branching 탐색 규칙, 그리고 학기 말까지의 개발 타임라인 설계 수록.

<!-- slide -->
# [3] 알고리즘 의사코드 & 예외 검증
[pseudocode_and_logic.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/pseudocode_and_logic.md)
* 자바 코드로 바로 옮겨 담을 수 있는 수준의 정교한 의사코드 제공:
  * `Implicant.canCombineWith()` 및 `combine()` 비트연산 로직
  * `QuineMcCluskey`의 Hamming Weight 기반 다차 결합 반복 루프
  * `PIChart`의 EPI 추출, Row/Column Domination 판별 로직
  * Cyclic Table 탈출을 위한 재귀적 `solveBranching()` 완전 탐색 로직
* Don't care 제외 규칙 및 순환 표 검증을 위한 핵심 테스트 케이스 설명.

<!-- slide -->
# [4] 구술 시험(Oral) 대비 & 보고서 뼈대
[oral_test_prep.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/oral_test_prep.md)
* 가산점 획득의 핵심인 1:1 구술면접(Oral Test) 예상 꼬리 질문 5개와 교수님이 감탄할 수준의 완벽한 모범 답변 제공.
* 과제 마감 때 최종 제출할 리포트의 체계적인 목차 아웃라인 구성.
````

---

## 3. 종합 태스크 완료 현황 (`task.md` 진행 상태)

* **`[x]` Phase 1**: 알고리즘 흐름 및 핵심 데이터 구조 정의
* **`[x]` Phase 2**: 과제 제안서(Proposal) 초안 작성 (13주차 화요일 마감)
* **`[x]` Phase 3**: 구현 단계별 논리 검증 및 테스트 케이스 설계
* **`[x]` Phase 4**: 최종 보고서 구조화 및 구술 테스트(Oral Test) 예상 질문 모의 면접

모든 설계적 의무와 검증 문서 작업이 완벽하게 완료되었습니다!

---

## 4. 사용자 실행 가이드 (IntelliJ 개발 시작)

1. **IntelliJ 프로젝트 열기**: 현재 활성화되어 있는 `/Users/proudchris/Desktop/논리설계/design` 폴더가 IntelliJ로 정상적으로 잡혀 있습니다.
2. **Java 클래스 생성**: `src` 폴더에 `Implicant.java`, `QuineMcCluskey.java`, `PIChart.java`, `AppMain.java`를 생성합니다.
3. **코드 작성**: 설계서의 아키텍처와 상세한 의사코드([pseudocode_and_logic.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/pseudocode_and_logic.md))를 모방하여 자바 클래스 내에 코드를 직접 채워 나가시면 됩니다!
