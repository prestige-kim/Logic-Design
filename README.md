# Two-Level Logic Minimizer (Quine-McCluskey & PI Chart Reduction)

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

본 프로젝트는 **한동대학교(Handong Global University) 논리설계 보너스 과제**로 개발되는 **2단 논리 간소화(Two-Level Logic Minimizer) 프로그램**입니다. 부울 함수(Boolean Function)의 Minterm과 Don't Care 조건을 입력받아, **Quine-McCluskey 방법** 및 **PI Chart Reduction 알고리즘**을 활용하여 논리적으로 가장 최적화된 최소 SOP(Sum-of-Products) 식을 완벽하게 도출합니다.

---

## 🚀 핵심 설계 사양 (Key Specifications)

1. **직관적인 문자열(String) 기반 설계 ($N \le 10$)**
   * 입력 변수의 개수를 $N \le 10$으로 설정하여 연산 시간 부담이 극히 적으므로, 복잡하고 디버깅이 어려운 비트 연산 대신 직관적인 **이진 문자열(String)** 연산을 핵심 표현법으로 채택했습니다.
   * 각 항(Term)은 `"01-0"`, `"0-10"` 같은 이진 문자열로 표현되어 사람이 한눈에 중간 계산 상태를 읽고 정확하게 검증할 수 있습니다.

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
│   ├── data_structure_design.md  # Java OOP 기반 문자열 자료구조 설계서
│   ├── proposal_draft.md         # 과제 제안서(Proposal) 공식 초안
│   ├── pseudocode_and_logic.md   # 병합/Reduction/Branching 상세 자바 의사코드
│   ├── oral_test_prep.md         # 구술 면접(Oral Test) 예상 질문 및 보고서 뼈대
│   ├── implementation_plan.md    # 최종 결정된 설계 사양이 포함된 개발 계획서
│   ├── task.md                   # 단계별 태스크 달성 상태 현황판
│   └── walkthrough.md            # 마이그레이션 및 설계 결과 요약서
├── src/                        # 소스 코드 디렉토리 (Java 클래스 구현)
│   ├── Main.java               # 입출력 CLI 및 프로그램 실행 진입점
│   ├── Implicant.java          # 개별 항(Term) 정보 및 단일 결합 객체
│   ├── QuineMcCluskey.java     # Hamming Weight 기반 그룹화 및 PI 추출 엔진
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
   java -cp bin Main
   ```

---

## 🎓 한동대학교 논리설계 보너스 과제 방침 준수
* **AI 직접 코드 작성 금지**: 이 프로그램의 소스 코드는 인공지능이 직접 생성하지 않았으며, 설계 및 예외 논리 검증의 안내를 바탕으로 **학생이 직접 작성**합니다.
* **부분점수 없음**: 완벽한 연산 및 모든 예외 상황(Cyclic, Don't Care) 검증을 통과하도록 극도로 촘촘하게 설계되었습니다.
