# [Phase 3] Quine-McCluskey 및 PI Chart Reduction 상세 의사코드 및 검증 논리

이 문서는 사용자가 Java로 완벽히 컴파일하고 동작시킬 수 있도록, **문자열(String)과 문자 비교**를 사용하여 정교하게 리팩토링된 **의사코드(Pseudocode)와 알고리즘 명세서**입니다. AI 코드 자동 생성 금지 수칙을 준수하면서도 최고의 프로그래밍 가이드가 되도록 작성되었습니다.

---

## 1. `Implicant` 클래스의 문자열 연산 설계

문자열(예: `"01-0"`)과 이진 변환 연산을 사용하여 객체 고유의 속성을 완전 제어합니다.

### 1.1. 핵심 메서드 의사코드

#### ① 이진 문자열 생성 및 변환
```java
private String toBinaryString(int value, int numVars) {
    String result = "";
    int temp = value;
    for (int i = 0; i < numVars; i++) {
        // 나머지를 문자열의 '앞'에 더해 나갑니다. (역순 방지)
        result = (temp % 2) + result;
        temp = temp / 2;
    }
    return result;
}
```

#### ② '1'의 개수 카운팅 (`countOnes()`)
```java
public int countOnes() {
    int count = 0;
    for (int i = 0; i < term.length(); i++) {
        if (term.charAt(i) == '1') {
            count++;
        }
    }
    return count;
}
```

#### ③ 결합 조건 검증 (`canCombineWith(Implicant other)`)
두 문자열을 비교하여 대시(`-`)의 위치는 완전히 일치하고, 정확히 다른 문자 자리가 단 1개(하나는 `'0'`, 다른 하나는 `'1'`)인지 루프로 조사합니다.
```java
public boolean canCombineWith(Implicant other) {
    // 문자열의 길이가 다르면 비교 불가
    if (this.term.length() != other.term.length()) {
        return false;
    }
    
    int diffCount = 0;
    for (int i = 0; i < this.term.length(); i++) {
        char charA = this.term.charAt(i);
        char charB = other.term.charAt(i);
        
        if (charA != charB) {
            // 어느 한쪽이라도 대시('-') 자리가 서로 다르면 병합 불가
            if (charA == '-' || charB == '-') {
                return false;
            }
            diffCount++;
        }
    }
    
    // 정확히 단 한 자리만 달라야 병합 가능
    return diffCount == 1;
}
```

#### ④ 실제 병합 수행 (`combine(Implicant other)`)
두 문자열 중 다른 자리를 대시(`'-'`)로 치환한 새 임플리컨트를 만듭니다.
```java
public Implicant combine(Implicant other) {
    String newTerm = "";
    
    // 두 문자열을 돌며 다른 자리를 찾아 '-'로 치환하여 새로운 문자열 조립
    for (int i = 0; i < this.term.length(); i++) {
        char charA = this.term.charAt(i);
        char charB = other.term.charAt(i);
        
        if (charA != charB) {
            newTerm += "-"; // 다른 부분은 대시가 됨
        } else {
            newTerm += charA; // 같은 부분은 그대로 유지
        }
    }
    
    // Minterm 리스트를 병합하고 정렬
    List<Integer> newMinterms = new ArrayList<>(this.minterms);
    newMinterms.addAll(other.minterms);
    Collections.sort(newMinterms);
    
    // 새로 생성된 Implicant 객체를 반환 (Implicant 클래스에 새 term을 주입하는 별도 생성자 필요)
    return new Implicant(newTerm, newMinterms);
}
```

---

## 2. `LogicSimplifier` 결합 엔진 설계

### 2.1. Hamming Weight 그룹 분류 로직
```java
public List<List<Implicant>> groupByHammingWeight(List<Implicant> implicants) {
    List<List<Implicant>> groups = new ArrayList<>();
    for (int i = 0; i <= numVars; i++) {
        groups.add(new ArrayList<>());
    }
    for (Implicant imp : implicants) {
        int ones = imp.countOnes();
        groups.get(ones).add(imp);
    }
    return groups;
}
```

### 2.2. 전체 PI 추출 알고리즘 흐름
더 이상 병합되지 않고 남은(isUsed == false) 항들을 추출하여 최종 Prime Implicant 목록을 모읍니다.
```java
public List<Implicant> runLogicSimplifier(List<Integer> minterms, List<Integer> dontCares) {
    // 1. Column 0 구성
    List<Implicant> currentColumn = new ArrayList<>();
    for (int m : minterms) currentColumn.add(new Implicant(numVars, m));
    for (int d : dontCares) currentColumn.add(new Implicant(numVars, d));
    
    List<Implicant> primeImplicants = new ArrayList<>();
    
    while (!currentColumn.isEmpty()) {
        List<List<Implicant>> groups = groupByHammingWeight(currentColumn);
        List<Implicant> nextColumn = new ArrayList<>();
        Set<Implicant> combinedThisRound = new HashSet<>();
        
        // 인접 그룹 간 이중 for 루프로 비교 결합
        for (int i = 0; i < numVars; i++) {
            List<Implicant> groupA = groups.get(i);
            List<Implicant> groupB = groups.get(i + 1);
            
            for (Implicant a : groupA) {
                for (Implicant b : groupB) {
                    if (a.canCombineWith(b)) {
                        Implicant combined = a.combine(b);
                        
                        // 중복 생성 방지
                        if (!nextColumn.contains(combined)) {
                            nextColumn.add(combined);
                        }
                        combinedThisRound.add(a);
                        combinedThisRound.add(b);
                    }
                }
            }
        }
        
        // 결합에 참여하지 못한 항들은 최종 Prime Implicant가 됨
        for (Implicant imp : currentColumn) {
            if (!combinedThisRound.contains(imp)) {
                if (!primeImplicants.contains(imp)) {
                    primeImplicants.add(imp);
                }
            }
        }
        currentColumn = nextColumn; // 다음 Column으로 진행
    }
    
    return primeImplicants;
}
```

---

## 3. `PIChart` Reduction 및 Branching Logic 설계

### 3.1. EPI 추출 및 Domination 의사코드
```java
public List<Implicant> simplifyChart(List<Implicant> pis, List<Integer> targetMinterms) {
    List<Implicant> selectedEPIs = new ArrayList<>();
    boolean changed = true;
    
    while (changed) {
        changed = false;
        
        // 1. EPI 찾기: 오직 하나의 PI만 커버하는 Minterm 열 탐색
        for (int minterm : targetMinterms) {
            List<Implicant> coveringPIs = getCoveringPIs(pis, minterm);
            if (coveringPIs.size() == 1) {
                Implicant epi = coveringPIs.get(0);
                selectedEPIs.add(epi);
                targetMinterms.removeAll(epi.minterms); // 해당 EPI가 해결한 Minterm 지움
                pis.remove(epi); // 표에서 제거
                changed = true;
                break;
            }
        }
        if (changed) continue;
        
        // 2. Row Domination (행 지배): 피지배 행 제거
        for (int i = 0; i < pis.size(); i++) {
            for (int j = 0; j < pis.size(); j++) {
                if (i == j) continue;
                Implicant a = pis.get(i);
                Implicant b = pis.get(j);
                // a가 b보다 동일한 Minterm들을 더 많이 커버하면 b 삭제
                if (coversAll(a, b, targetMinterms)) {
                    pis.remove(b);
                    changed = true;
                    break;
                }
            }
            if (changed) break;
        }
        if (changed) continue;
        
        // 3. Column Domination (열 지배): 지배 열 제거
        for (int i = 0; i < targetMinterms.size(); i++) {
            for (int j = 0; j < targetMinterms.size(); j++) {
                if (i == j) continue;
                int c1 = targetMinterms.get(i);
                int c2 = targetMinterms.get(j);
                // c1이 c2보다 까다로우면 지배 열 c2 삭제
                if (isColumnDominated(pis, c1, c2)) {
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

### 3.2. Branching (Branch-and-Bound) 의사코드
표가 더 이상 줄어들지 않을 때 재귀 탐색을 가동합니다.
```java
public List<Implicant> solveBranching(List<Implicant> remainingPIs, List<Integer> remainingMinterms) {
    if (remainingMinterms.isEmpty()) {
        return new ArrayList<>();
    }
    
    // 가장 넓게 커버하는 PI 하나를 탐색 기준으로 임의 지정
    Implicant p = remainingPIs.get(0);
    
    // 분기 A: p를 선택한 경우
    List<Implicant> nextPIsA = new ArrayList<>(remainingPIs);
    nextPIsA.remove(p);
    List<Integer> nextMintermsA = new ArrayList<>(remainingMinterms);
    nextMintermsA.removeAll(p.minterms);
    
    List<Implicant> resultA = new ArrayList<>();
    resultA.add(p);
    resultA.addAll(simplifyChart(nextPIsA, nextMintermsA));
    resultA.addAll(solveBranching(nextPIsA, nextMintermsA));
    
    // 분기 B: p를 배제한 경우
    List<Implicant> nextPIsB = new ArrayList<>(remainingPIs);
    nextPIsB.remove(p);
    List<Integer> nextMintermsB = new ArrayList<>(remainingMinterms);
    
    List<Implicant> resultB = new ArrayList<>();
    resultB.addAll(simplifyChart(nextPIsB, nextMintermsB));
    resultB.addAll(solveBranching(nextPIsB, nextMintermsB));
    
    // 두 분기 중 더 나은 해(1차: 항의 개수 적은 쪽, 2차: 리터럴 수가 적은 쪽)를 선택
    return chooseBetterSolution(resultA, resultB);
}
```
* **리터럴 개수 계산**: 각 Implicant의 `term` 문자열에서 `'-'`가 아닌 `'0'` 또는 `'1'` 문자의 개수(즉, `term.length() - countDashes(term)`)의 총합을 구합니다.
