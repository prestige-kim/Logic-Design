# Two-Level Logic Minimizer (Quine-McCluskey & PI Chart Reduction)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

본 프로젝트는 **한동대학교(Handong Global University) 논리설계 보너스 과제**로 개발된 **2단 논리 간소화(Two-Level Logic Minimizer) 프로그램**입니다. 부울 함수(Boolean Function)의 Minterm과 Don't Care 조건을 입력받아, **Quine-McCluskey 방법** 및 **PI Chart Reduction 알고리즘**을 활용하여 논리적으로 가장 최적화된 최소 SOP(Sum-of-Products) 식을 완벽하게 도출합니다.

---

## 🚀 핵심 설계 사양 (Key Specifications)

1. **초고속 비트마스킹(Bitmasking) 최적화 ($N \le 10$)**
   * 입력 변수의 개수를 $N \le 10$으로 설정하여, 각 Implicant를 32-bit `int`형 비트 연산으로 O(1)의 속도로 병합 및 상태 판단하도록 설계했습니다.
   * `mask` (대시 `-` 위치 표시 비트)와 `value` (고정 비트 값) 필드를 통해 메모리 오버헤드와 연산 속도를 획기적으로 개선했습니다.

2. **완벽한 PI Chart Reduction**
   * **EPI(Essential Prime Implicant) 추출**: 단 하나의 PI에 의해서만 커버되는 Minterm을 감지하여 필수 주임플리컨트로 자동 선정합니다.
   * **Row & Column Domination(행/열 지배 법칙)**: 표를 반복해서 수학적으로 간소화하여 연산 규모를 최대로 축소합니다.

3. **Branch-and-Bound 기반 순환 표(Cyclic Table) 해결**
   * EPI 제거와 지배 법칙만으로 더 이상 줄어들지 않고 순환 관계에 빠진 표(Cyclic Table)에 대해 **Branching Approach**를 적용하여 모든 조합을 탐색합니다.
   * **평가 기준**: [1] 곱의 항(Product Terms) 수 최소화 $\rightarrow$ [2] 리터럴(Literals) 수 최소화를 만족하는 최적의 최종 해를 완전 탐색해 냅니다.

4. **엄격한 Don't Care 처리**
   * Don't care 조건은 Quine-McCluskey 병합(PI 추출) 시에는 활용되나, 최종 PI Chart의 열(Column) 구성 시에는 완벽히 배제되어 불필요한 조건 커버에 의한 비최적화를 원천 방지합니다.

---

## 📁 프로젝트 폴더 구조 (Project Structure)

```text
design/
├── .idea/                      # IntelliJ 프로젝트 설정 폴더
├── docs/                       # 설계 및 시험 대비 핵심 가이드 문서
│   ├── data_structure_design.md  # Java OOP 기반 비트마스크 자료구조 설계서
│   ├── proposal_draft.md         # 과제 제안서(Proposal) 공식 초안
│   ├── pseudocode_and_logic.md   # 병합/Reduction/Branching 상세 자바 의사코드
│   └── oral_test_prep.md         # 구술 면접(Oral Test) 예상 질문 및 보고서 뼈대
├── src/                        # 소스 코드 디렉토리 (Java 클래스 구현 예정)
│   ├── AppMain.java            # 입출력 CLI 진입점
│   ├── Implicant.java          # 개별 항(Term) 정보 객체
│   ├── QuineMcCluskey.java     # Hamming Weight 기반 PI 추출 엔진
│   └── PIChart.java            # 표 축소 및 Branching 솔버
├── README.md                   # 프로젝트 개요 (본 문서)
└── .gitignore                  # Git 빌드 출력물 및 개인 설정 제외 파일
```

---

## 🛠️ 설치 및 실행 방법 (How to Build & Run)

### 요구사항 (Requirements)
* **Java Development Kit (JDK)**: 17 버전 이상 추천
* **IDE**: IntelliJ IDEA (추천) 또는 Eclipse

### 실행 방법 (CLI)
1. 리포지토리를 복사합니다:
   ```bash
   git clone https://github.com/prestige-kim/Logic-Design.git
   cd Logic-Design
   ```
2. Java 소스 코드를 컴파일하고 실행합니다 (코드 구현 완료 후):
   ```bash
   javac -d bin src/*.java
   java -cp bin AppMain
   ```

---

## 🎓 한동대학교 논리설계 보너스 과제 방침 준수
* **AI 직접 코드 작성 금지**: 이 프로그램의 소스 코드는 인공지능이 직접 생성하지 않았으며, 설계 및 예외 논리 검증의 안내를 바탕으로 **학생이 직접 작성**합니다.
* **부분점수 없음**: 완벽한 연산 및 모든 예외 상황(Cyclic, Don't Care) 검증을 통과하도록 극도로 촘촘하게 설계되었습니다.
