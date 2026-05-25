# [Phase 3] Quine-McCluskey 및 PI Chart Reduction 상세 의사코드 및 검증 논리

이 문서는 사용자가 Java로 완벽히 컴파일하고 동작시킬 수 있도록, 극도로 자세하게 설계된 **의사코드(Pseudocode)와 알고리즘 명세서**입니다. 교수님의 방침(AI 코드 생성 금지)에 따라 모든 논리 흐름을 의사코드와 수학적 흐름으로 표현하여 사용자가 직접 코딩하는 데 최고의 설계도가 되도록 작성했습니다.

---

## 1. `Implicant` 클래스의 비트 연산 설계

Java 객체 내에서 각 항(Implicant)을 문자열 대신 32-bit `int`형 비트 연산으로 매우 가볍고 직관적으로 다룹니다.

### 1.1. 비트 필드 명세
* `numVars`: 변수 개수 ($N \le 10$)
* `mask`: 대시(`-`)의 위치를 나타내는 10비트 맵. (대시 위치는 `1`, 고정 위치는 `0`)
* `value`: 고정된 변수의 비트 값. (대시 위치는 `0`으로 일관되게 채움)
* `minterms`: 이 항이 포함하는 본래의 Minterm 번호들 (`List<Integer>`)

### 1.2. 핵심 메서드 의사코드

#### ① 결합 조건 검증 (`canCombineWith(Implicant other)`)
두 항의 대시 위치가 완전히 일치하고, 고정된 비트 영역에서 단 한 자리만 다른지 비트 연산으로 판별합니다.
```java
public boolean canCombineWith(Implicant other) {
    // 1. 대시의 위치(mask)가 서로 다르면 결합 불가능
    if (this.mask != other.mask) {
        return false;
    }
    
    // 2. 두 값의 차이를 XOR로 구함
    int diff = this.value ^ other.value;
    
    // 3. XOR 결과에서 켜진 비트 개수가 정확히 1개(Hamming Distance == 1)인지 확인
    // Integer.bitCount() 함수 사용
    return Integer.bitCount(diff) == 1;
}
```

#### ② 병합 수행 (`combine(Implicant other)`)
```java
public Implicant combine(Implicant other) {
    // 두 항이 다른 단 하나의 비트 자리를 찾음
    int diff = this.value ^ other.value;
    
    // 새로운 mask: 기존 mask에 다른 비트 자리를 대시(1)로 추가
    int newMask = this.mask | diff;
    
    // 새로운 value: 다른 비트 자리를 0으로 일관되게 마스킹
    int newValue = this.value & ~diff;
    
    // 두 Implicant의 Minterm 리스트를 합치고 오름차순 정렬
    List<Integer> newMinterms = new ArrayList<>(this.minterms);
    newMinterms.addAll(other.minterms);
    Collections.sort(newMinterms);
    
    return new Implicant(this.numVars, newMask, newValue, newMinterms);
}
```

---

## 2. `QuineMcCluskey` 결합 엔진 설계

### 2.1. 전체 PI 추출 알고리즘 흐름
Minterm들과 Don't care들을 1의 개수(Hamming Weight)에 따라 분할하고 결합 루프를 실행합니다.

```java
public List<Implicant> runQuineMcCluskey() {
    // 1. 초기 0차(Column 0) 리스트 생성 (Minterms + Don't Cares)
    List<Implicant> currentColumn = new ArrayList<>();
    for (int m : minterms) currentColumn.add(new Implicant(numVars, 0, m, m));
    for (int d : dontCares) currentColumn.add(new Implicant(numVars, 0, d, d));
    
    List<Implicant> primeImplicants = new ArrayList<>();
    
    // 2. 더 이상 병합이 일어나지 않을 때까지 Column 단위로 루프 수행
    while (!currentColumn.isEmpty()) {
        // Hamming Weight(대시를 제외한 1의 개수)로 그룹화
        Map<Integer, List<Implicant>> groups = groupByHammingWeight(currentColumn);
        List<Implicant> nextColumn = new ArrayList<>();
        Set<Implicant> combinedThisRound = new HashSet<>();
        
        // 인접 그룹 간 비교 (그룹 i 와 그룹 i+1)
        for (int i = 0; i < numVars; i++) {
            List<Implicant> groupA = groups.getOrDefault(i, Collections.emptyList());
            List<Implicant> groupB = groups.getOrDefault(i + 1, Collections.emptyList());
            
            for (Implicant a : groupA) {
                for (Implicant b : groupB) {
                    if (a.canCombineWith(b)) {
                        Implicant combined = a.combine(b);
                        
                        // 중복 생성 방지
                        if (!nextColumn.contains(combined)) {
                            nextColumn.add(combined);
                        }
                        combinedThisRound.add(a);
                        combinedCombinedThisRound.add(b);
                    }
                }
            }
        }
        
        // 병합에 한 번도 참여하지 않은(isUsed == false) Implicant들은 Prime Implicant(PI)로 확정
        for (Implicant imp : currentColumn) {
            if (!combinedThisRound.contains(imp)) {
                // 중복 PI 방지하여 최종 리스트에 추가
                if (!primeImplicants.contains(imp)) {
                    primeImplicants.add(imp);
                }
            }
        }
        
        currentColumn = nextColumn; // 다음 차수로 이동
    }
    
    return primeImplicants;
}
```

---

## 3. `PIChart` Reduction 및 Branching Logic 설계

Don't Care를 **열(Column) 목록에서 완전히 제외**하고 표를 구축한 뒤 축소 알고리즘을 가동합니다.

### 3.1. EPI 추출 및 지배 법칙 의사코드

```java
public List<Implicant> simplifyChart(List<Implicant> pis, List<Integer> targetMinterms) {
    List<Implicant> selectedEPIs = new ArrayList<>();
    boolean changed = true;
    
    while (changed) {
        changed = false;
        
        // 1. EPI 찾기
        // 각 Minterm Column별로 이를 커버하는 PI가 단 1개뿐인지 조사
        for (int minterm : targetMinterms) {
            List<Implicant> coveringPIs = getCoveringPIs(pis, minterm);
            if (coveringPIs.size() == 1) {
                Implicant epi = coveringPIs.get(0);
                selectedEPIs.add(epi);
                
                // EPI가 커버하는 모든 Minterm 열을 표에서 제거
                targetMinterms.removeAll(epi.minterms);
                pis.remove(epi);
                
                changed = true;
                break; // 표가 수정되었으므로 처음부터 재검사
            }
        }
        if (changed) continue;
        
        // 2. Row Domination (행 지배)
        // PI A가 커버하는 Minterm 집합이 PI B가 커버하는 집합을 완전히 포함(Superset)하면 B 제거
        for (int i = 0; i < pis.size(); i++) {
            for (int j = 0; j < pis.size(); j++) {
                if (i == j) continue;
                Implicant a = pis.get(i);
                Implicant b = pis.get(j);
                
                if (coversAll(a, b, targetMinterms)) {
                    // a가 b를 지배하므로 피지배 행 b를 제거
                    pis.remove(b);
                    changed = true;
                    break;
                }
            }
            if (changed) break;
        }
        if (changed) continue;
        
        // 3. Column Domination (열 지배)
        // Minterm C1을 커버하는 PI 집합이 C2를 커버하는 집합에 포함(Subset)되면 지배 열 C2 제거
        for (int i = 0; i < targetMinterms.size(); i++) {
            for (int j = 0; j < targetMinterms.size(); j++) {
                if (i == j) continue;
                int c1 = targetMinterms.get(i);
                int c2 = targetMinterms.get(j);
                
                if (isColumnDominated(pis, c1, c2)) {
                    // c1이 c2를 지배하므로 지배 열 c2를 제거
                    targetMinterms.remove((Integer) c2);
                    changed = true;
                    break;
                }
            }
            if (changed) break;
        }
    }
    
    return selectedEPIs;
}
```

---

### 3.2. Branching Approach (Branch-and-Bound) 의사코드
EPI 제거와 지배 법칙으로도 더 이상 표가 축소되지 않을 때(Cyclic Table), 최적의 조합을 재귀적으로 완전 탐색하여 리터럴 수와 항의 수 기준 최소 해를 구합니다.

```java
public List<Implicant> solveBranching(List<Implicant> remainingPIs, List<Integer> remainingMinterms) {
    // 기저 조건: 커버해야 할 Minterm이 없으면 빈 조합 리턴
    if (remainingMinterms.isEmpty()) {
        return new ArrayList<>();
    }
    
    // 탐색 속도를 높이기 위해, 가장 많은 Minterm을 커버하는 PI를 기준 행으로 선택
    Implicant p = selectBestBranchingPI(remainingPIs, remainingMinterms);
    
    // 분기 1: PI 'p'를 최종 식에 포함하는 경우 (Select p)
    List<Implicant> nextPIs1 = new ArrayList<>(remainingPIs);
    nextPIs1.remove(p);
    List<Integer> nextMinterms1 = new ArrayList<>(remainingMinterms);
    nextMinterms1.removeAll(p.minterms); // p가 커버하는 항목 지움
    
    // 단순화 수행 후 재귀 호출
    List<Implicant> branch1EPIs = simplifyChart(nextPIs1, nextMinterms1);
    List<Implicant> branch1SubResult = solveBranching(nextPIs1, nextMinterms1);
    
    List<Implicant> result1 = new ArrayList<>();
    result1.add(p);
    result1.addAll(branch1EPIs);
    result1.addAll(branch1SubResult);
    
    // 분기 2: PI 'p'를 최종 식에서 배제하는 경우 (Exclude p)
    List<Implicant> nextPIs2 = new ArrayList<>(remainingPIs);
    nextPIs2.remove(p);
    List<Integer> nextMinterms2 = new ArrayList<>(remainingMinterms);
    
    // 단순화 수행 후 재귀 호출
    List<Implicant> branch2EPIs = simplifyChart(nextPIs2, nextMinterms2);
    List<Implicant> branch2SubResult = solveBranching(nextPIs2, nextMinterms2);
    
    List<Implicant> result2 = new ArrayList<>();
    result2.addAll(branch2EPIs);
    result2.addAll(branch2SubResult);
    
    // 두 분기의 결과 중 더 좋은 식을 비교 평가하여 최종 반환
    return chooseBetterSolution(result1, result2);
}
```

#### 식의 평가지표 비교 법칙 (`chooseBetterSolution`)
1. **항의 개수(Product Terms)**: 더 적은 수의 PI 객체 리스트를 우선합니다.
2. **리터럴 개수(Literals)**: 항의 수가 같다면 각 임플리컨트의 변수 문자 개수(대시가 아닌 개수: `numVars - Integer.bitCount(mask)`)의 총합이 적은 쪽을 선정합니다.

---

## 4. 예외 케이스 및 경계값 테스트 케이스 (Verification Test Cases)

### 4.1. Don't Care 검증 케이스
* **입력**: Minterms: `m(0, 2, 5)`, Don't Cares: `d(1, 3)`
* **기대 결과**: 
  * 병합 단계에서 `0(000)`, `1(001)`, `2(010)`, `3(011)`이 서로 결합하여 `0-` 및 `-0` 등의 PI를 만듭니다.
  * 최종 PI Chart에서는 `d(1, 3)`은 완전히 열(Column)에서 배제되므로 오직 `m(0, 2, 5)`만 커버하면 됩니다.
  * 최종 식: $F = x_1'\overline{x_2} + x_1 x_2' x_3$ (변수 매핑에 따라 다름) 형태로 정확히 도출되어야 합니다.

### 4.2. Cyclic Table (순환 관계) 검증 케이스
* **입력**: Minterms: `m(0, 1, 2, 5, 6, 7)` (Don't care 없음)
* **상황**: 모든 PI가 동일한 수의 Minterm을 커버하여 Row/Column Domination으로 더 이상 줄어들지 않고 순환에 도달합니다.
* **기대 결과**: Branching Approach에 의해 무한 루프에 빠지지 않고 안전하게 탈출하여 최소 SOP 식을 찾아내야 합니다.
