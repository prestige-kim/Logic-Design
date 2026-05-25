# Two-Level Logic Minimization Program using Tabular Method

이 프로젝트는 Handong Global University 논리설계 보너스 과제인 **Quine-McCluskey 및 Prime Implicant (PI) Chart Reduction 알고리즘**을 구현하여 임의의 입력 변수 개수에 대해 최소 SOP(Sum of Products) 식을 도출하는 2단 논리 최적화(Two-Level Logic Minimization) 프로그램 개발을 위한 협업 계획서입니다.

> [!IMPORTANT]
> **핵심 제약 조건 (교수님 방침)**
> 1. **코드 생성 및 직접 작성 금지**: 이 프로그램의 소스 코드는 인공지능이 직접 생성할 수 없으며, 모든 코드는 사용자가 작성해야 합니다. 안티그래비티는 알고리즘 흐름, 데이터 구조, 의사코드(Pseudocode), 그리고 예외 케이스 검증 및 구술 면접 대비를 철저히 지원합니다.
> 2. **Python 제외**: Java, C, C++ 중 하나의 언어로 개발이 진행되어야 하며, macOS 환경(M4 MacBook Air) 및 IntelliJ/VS Code/Eclipse IDE를 지원합니다.
> 3. **부분 점수 없음**: 완벽한 동작 시 10점 가산, 실패 시 0점입니다. 예외 케이스가 없도록 극도로 촘촘하게 논리를 검증해야 합니다.

---

## User Review Required

> [!NOTE]
> **구현 언어 및 자료구조 선정**
> - **언어 선정**: Java 혹은 C++가 객체 지향 및 비트연산의 이점을 살리기에 적합합니다. 메모리 관리와 디버깅 편의성을 고려하면 Java가 객체 구조화에 우수하며, 하드웨어 수준의 빠른 비트 조작 및 메모리 최적화를 원한다면 C++가 훌륭한 대안입니다. 사용자의 선호 언어 확인이 필요합니다.
> - **자료구조 후보군**: Bitwise 연산(Bitmasking)을 활용할 것인지, 아니면 문자열과 객체 기반으로 상태를 관리할 것인지에 대한 설계적 결단이 필요합니다.

---

## Resolved Architecture Decisions

> [!TIP]
> **최종 결정된 설계 사양**
> 1. **입력 변수의 최대 개수 ($N \le 10$)**:
>    - 최대 Minterm 개수가 $2^{10} = 1024$개이므로 성능적 부담이 적습니다.
>    - 32-bit `int`를 이용한 **비트마스킹(Bitmasking)** 기법을 사용하여 각 항의 이진 표현 및 대시(`-`) 상태를 극도로 가볍고 효율적으로 관리합니다.
> 2. **순환 표(Cyclic Chart) 해결을 위한 Branching Approach 적용**:
>    - EPI 제거 및 Row/Column Domination 후에도 표가 축소되지 않고 남는 경우, 교수님 가이드라인에 따라 **Branching Approach (Branch-and-Bound)**를 활용하여 가능한 모든 조합 중 리터럴 수가 가장 적은 최소 SOP 식을 탐색하여 도출합니다.

---

## Proposed Changes

코드 직접 생성이 금지되어 있으므로, 우리의 변경 사항은 사용자가 소스 코드를 올바르게 작성할 수 있도록 안내하는 설계 문서 및 가이드를 생성하는 것입니다.

### Phase 1: 알고리즘 흐름 및 핵심 데이터 구조 정의
- **목표**: 비트마스킹 대 객체 지향 설계 비교 분석, Don't care 병합 시 처리 논리 확립, Row/Column Domination의 수학적 모델화.
- [NEW] [data_structure_design.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/data_structure_design.md) (설계 문서)

### Phase 2: 과제 제안서(Proposal) 초안 및 모듈 인터페이스 정의
- **목표**: 13주차 화요일 마감에 맞춘 Proposal 문서 작성 지원. 핵심 클래스(또는 구조체) 인터페이스 및 함수 프로토타입 작성, 구현 타임라인 구조화.
- [NEW] [proposal_draft.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/proposal_draft.md) (제안서 아웃라인 및 구조)

### Phase 3: 알고리즘 상세 설계 (의사코드 & 논리 검증)
- **목표**: Minterm 결합 Logic, PI Chart Reduction(EPI 식별, Row/Col Domination, Cyclic chart)의 정교한 의사코드 작성 및 시각적 예외 검증.
- [NEW] [pseudocode_and_logic.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/pseudocode_and_logic.md)

### Phase 4: 모의 구술 면접 및 보고서 구조화
- **목표**: Oral Test 대비 개념 검증 질문 리스트 구성, 코드가 막혔을 때의 추적 기법, 최적화 이론 설명 가이드 제공.
- [NEW] [oral_test_prep.md](file:///Users/proudchris/.gemini/antigravity-cli/brain/48de8e19-e686-4fe4-9526-d327ee423b34/scratch/oral_test_prep.md)

---

## Verification Plan

### Manual Verification
- **검증 시나리오 1: 표준 예제 검증**
  - Minterms: $m(0, 1, 2, 5, 6, 7, 8, 9, 10, 14)$, Don't cares: $d(3, 4, 11, 15)$ 입력 시 올바른 최소 SOP가 출력되는지 여부 검증.
- **검증 시나리오 2: Cyclic Table 검증**
  - Petrick's Method 또는 Branching 알고리즘이 필요한 순환 PI Chart 케이스(예: 2~3개 이상의 동등한 결합을 선택해야 하는 상황)에서의 올바른 해 도출 여부 검증.
- **검증 시나리오 3: Don't Care의 PI 포함 및 차트 제외 검증**
  - Don't care는 PI를 형성하는 데는 사용되되, EPI/PI Chart 상의 Column(커버해야 하는 대상)에서는 완전히 제외되는지 검증.
- **검증 시나리오 4: Row/Column Domination 무한 루프 검증**
  - 행(Row)과 열(Column) 지배 법칙 적용 시, 더 이상 줄어들지 않을 때 루프를 안전하게 탈출하고 분기(Branching)로 넘어가는지 검증.
