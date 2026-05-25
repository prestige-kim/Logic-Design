# [Phase 2] 논리설계 보너스 과제 제안서 (Proposal) 초안

이 문서는 **Handong Global University 논리설계 보너스 과제(Two-Level Logic Minimizer)**의 13주차 화요일 제출을 위한 공식 제안서 초안입니다. 교수님의 평가 가이드라인에 맞추어 프로젝트의 목표, 핵심 알고리즘, 자료구조 설계, 그리고 개발 일정을 구조화했습니다.

---

## 1. 프로젝트 개요 및 목표 (Introduction & Objective)

본 프로젝트는 임의의 부울 함수(Boolean Function)의 Minterm과 Don't Care 조건을 입력받아, **Quine-McCluskey 방법**과 **PI Chart Reduction 알고리즘**을 활용하여 가장 간소화된 **SOP(Sum-of-Products) 식**을 도출하는 2단 논리 최적화 프로그램 개발을 목표로 합니다.

### 핵심 목표:
* **완전성(Completeness)**: 0점 혹은 10점의 가산점 조건에 맞추어 어떠한 예외 케이스(Don't Care 처리, Cyclic Table 등)에서도 완벽하게 올바른 간소화 식을 도출합니다.
* **Java 기반 순수 OOP 설계**: Java 언어의 객체 지향적 강점과 자료구조를 활용해 유지보수 및 디버깅이 쉬운 모듈화된 코드를 작성합니다.
* **고성능 비트 연산 기법 도입**: 변수 개수를 최대 $N \le 10$으로 제한하여, 비트마스크(Bitmask) 연산을 통한 초고속 병합 및 검증 메커니즘을 구현합니다.

---

## 2. 핵심 알고리즘 및 설계 핵심 (Algorithm & System Design)

### 2.1. 비트마스크(Bitmasking) 기반 Implicant 표현 및 병합법
변수의 개수가 $N \le 10$이므로, 문자열 처리 대신 32-bit 정수형(`int`) 비트 연산으로 Implicant를 표현하여 메모리와 연산 속도를 극대화합니다.

* **자료구조 정의**:
  * `value` (int): 확정된 변수의 비트 값 (예: `0` 또는 `1`). 대시(`-`)인 자리는 `0`으로 처리합니다.
  * `mask` (int): 대시(`-`)의 위치를 나타내는 비트 맵. 대시가 있는 자리는 `1`, 고정값 자리는 `0`으로 표현합니다.
  * *예시 ($N=4$)*: `0-10` $\rightarrow$ `mask = 0100` (이진수), `value = 0010` (이진수)

* **병합 조건 (`canCombineWith`)**:
  두 Implicant $A$와 $B$는 다음 조건을 만족할 때 병합할 수 있습니다:
  1. 대시의 위치가 동일해야 함: `A.mask == B.mask`
  2. 대시가 없는 자리 중 단 한 비트만 달라야 함: `Integer.bitCount(A.value ^ B.value) == 1`

* **병합 수행 (`combine`)**:
  병합된 새로운 Implicant는 다음과 같이 비트 연산으로 즉시 생성됩니다:
  * `newMask = A.mask | (A.value ^ B.value)` (서로 달랐던 자리가 새로운 대시 `1`이 됨)
  * `newValue = A.value & ~diff` (새로운 대시 자리를 `0`으로 마스킹)

---

### 2.2. PI Chart Reduction 및 Branching Approach (Branch-and-Bound)

1. **EPI (Essential Prime Implicant) 추출**: 
   * 단 하나의 PI에 의해서만 커버되는 Minterm 열을 찾아 필수 주임플리컨트(EPI)로 선정합니다.
   * 선택된 EPI 행과 그것이 커버하는 모든 Minterm 열을 표에서 제거합니다.
2. **Row & Column Domination (행/열 지배)**:
   * **열 지배**: 다른 열에 비해 더 좁은 범위의 PI에게만 커버되는 '깐깐한 열'이 존재하면, 이를 완벽히 포함하는 '더 넓게 커버되는 열(지배 열)'을 지웁니다.
   * **행 지배**: 더 많은 Minterm을 커버할 수 있는 '우수한 행'이 열세인 행을 지배하므로, 피지배 행을 삭제하여 표를 축소합니다.
3. **Branching Approach (순환 표 해결)**:
   * EPI 제거와 지배 법칙을 적용한 후에도 표가 더 이상 줄어들지 않고 순환 형태(Cyclic Table)로 남는 경우, **Branch-and-Bound(가지치기 분기 탐색)**을 적용합니다.
   * 남아있는 PI 중 하나를 강제로 '선택하는 분기'와 '선택하지 않는 분기'로 쪼개어 각각 재귀적으로 해를 탐색합니다.
   * 최종적으로 **[1] 커버된 Minterm의 완결성**, **[2] SOP 식의 리터럴 개수 최소화**, **[3] 곱의 항(Product Term) 개수 최소화** 조건을 기준으로 가장 최적의 해를 반환합니다.

---

## 3. 소프트웨어 아키텍처 (Software Architecture)

### 3.1. 핵심 모듈 구성
* **`Implicant`**: 개별 항의 정보를 비트마스크 형태로 지니고 있는 기본 데이터 객체.
* **`QuineMcCluskey`**: Minterm과 Don't Care 리스트를 그룹화하고, 더 이상 병합이 불가능할 때까지 반복 결합하여 Prime Implicant(PI) 목록을 만드는 결합 엔진.
* **`PIChart`**: PI와 커버 대상 Minterm 간의 매핑 테이블을 구현하고, EPI 추출, 행/열 지배, Branching 알고리즘을 종합 수행하는 간소화 솔버.
* **`AppMain`**: 입출력(CLI) 및 파일 입력을 담당하고 전체 파이프라인을 구동하는 진입점.

---

## 4. 개발 일정 및 역할 분담 (Timeline & Milestones)

| 주차 / 일정 | 개발 단계 | 세부 작업 내용 | 완료 여부 |
| :--- | :--- | :--- | :---: |
| **12주차 (현재)** | **Phase 1: 설계 확정** | - Java 비트마스크 기반 OOP 아키텍처 설계 수립<br>- 예외적 테스트 케이스 리스트업 | **완료 (100%)** |
| **13주차 초반** | **Phase 2: 제안서 제출** | - 본 제안서 문서(Proposal) 최종 정리 및 피드백 반영<br>- 과제 제안서 정식 제출 | **진행 중** |
| **13주차 중반** | **Phase 3: 코어 개발** | - `Implicant` 및 `QuineMcCluskey` 결합 로직 구현<br>- `PIChart` 및 EPI 추출, Domination 로직 구현 | 대기 |
| **14주차 초반** | **Phase 3: Branching 구현** | - Cyclic Table 처리를 위한 Branch-and-Bound 재귀 알고리즘 구현 | 대기 |
| **14주차 후반** | **Phase 3: 테스트 & 검증** | - Don't Care 배제 및 무한 루프 검증 테스트<br>- 20개 이상의 엣지 케이스 교차 검증 수행 | 대기 |
| **15주차 (최종)** | **Phase 4: 보고서 및 Oral 대비**| - 최종 보고서 작성 및 프로젝트 패키징 완료<br>- 구술 면접(Oral Test) 예상 질문 모의 질의응답 | 대기 |
