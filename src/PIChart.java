import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prime Implicant Chart (주임플리컨트 도표)를 생성하고 최적의 해를 탐색하는 분석기입니다.
 * 필수 주임플리컨트(EPI)를 우선 선정한 후, 잔여 민텀들을 커버하는 최소 개수의 Implicant 조합을 백트래킹을 통해 찾아냅니다.
 */
public class PIChart {
    private List<Implicant> primeImplicants; // 행(Row)에 위치할 Prime Implicants 목록
    private List<Integer> minterms;          // 열(Column)에 위치할 오리지널 민텀 목록

    /**
     * 분석에 필요한 Prime Implicants 목록과 민텀 목록을 초기화합니다.
     */
    public PIChart(List<Implicant> piList, List<Integer> mtList) {
        this.primeImplicants = new ArrayList<>(piList);
        this.minterms = new ArrayList<>(mtList);
    }

    /**
     * EPI를 우선 선정하고, 남은 민텀에 대해 백트래킹으로 최적의 최소 Implicant 조합을 결정합니다.
     * 
     * @return 최종 선정된 최적의 Implicant 리스트
     */
    public List<Implicant> solve() {
        List<Implicant> selectedPIs = new ArrayList<>();
        if (primeImplicants.isEmpty() || minterms.isEmpty()) {
            return selectedPIs;
        }

        // 1. 필수 주임플리컨트(EPI) 탐색 및 선정
        // 단 하나의 PI에 의해서만 커버되는 민텀이 있다면, 해당 PI는 필수(EPI)입니다.
        Set<Integer> coveredMinterms = new HashSet<>();
        boolean[] isEPI = new boolean[primeImplicants.size()];

        for (int mt : minterms) {
            int coverCount = 0;
            int lastCoverIndex = -1;
            for (int i = 0; i < primeImplicants.size(); i++) {
                if (primeImplicants.get(i).minterms.contains(mt)) {
                    coverCount++;
                    lastCoverIndex = i;
                }
            }
            if (coverCount == 1) {
                isEPI[lastCoverIndex] = true;
            }
        }

        // 찾은 EPI들을 최종 목록에 추가하고, 커버된 민텀 기록
        for (int i = 0; i < primeImplicants.size(); i++) {
            if (isEPI[i]) {
                Implicant epi = primeImplicants.get(i);
                selectedPIs.add(epi);
                coveredMinterms.addAll(epi.minterms);
            }
        }

        // 2. EPI로 처리하고 남아있는 잔여 민텀 추출
        List<Integer> remainingMinterms = new ArrayList<>();
        for (int mt : minterms) {
            if (!coveredMinterms.contains(mt)) {
                remainingMinterms.add(mt);
            }
        }

        // 잔여 민텀이 없다면 즉시 결과 반환
        if (remainingMinterms.isEmpty()) {
            return selectedPIs;
        }

        // 3. 백트래킹을 이용한 최적의 잔여 PI 조합 탐색
        List<Implicant> remainingPIs = new ArrayList<>();
        for (int i = 0; i < primeImplicants.size(); i++) {
            if (!isEPI[i]) {
                remainingPIs.add(primeImplicants.get(i));
            }
        }

        List<Implicant> bestCombination = new ArrayList<>();
        backtrack(remainingPIs, remainingMinterms, 0, new ArrayList<>(), bestCombination);

        selectedPIs.addAll(bestCombination);
        return selectedPIs;
    }

    /**
     * 백트래킹 기법을 통해 잔여 민텀을 모두 커버하는 최소 크기의 PI 조합을 탐색합니다.
     */
    private void backtrack(List<Implicant> remainingPIs, List<Integer> remainingMinterms, int index,
                           List<Implicant> current, List<Implicant> best) {
        // 이미 구한 최적 조합의 크기보다 크거나 같으면 더 탐색할 필요가 없음 (가지치기)
        if (!best.isEmpty() && current.size() >= best.size()) {
            return;
        }

        // 현재 조합으로 남은 모든 민텀을 만족스럽게 커버하는지 검사
        if (coversAll(current, remainingMinterms)) {
            best.clear();
            best.addAll(current);
            return;
        }

        // 더 이상 고려할 후보가 없으면 종료
        if (index >= remainingPIs.size()) {
            return;
        }

        // 분기 1: 현재 인덱스의 PI를 포함하는 경우
        current.add(remainingPIs.get(index));
        backtrack(remainingPIs, remainingMinterms, index + 1, current, best);
        current.remove(current.size() - 1); // 롤백

        // 분기 2: 현재 인덱스의 PI를 포함하지 않는 경우
        backtrack(remainingPIs, remainingMinterms, index + 1, current, best);
    }

    /**
     * 선택된 Implicant 조합이 대상 민텀들을 모두 커버하는지 확인합니다.
     */
    private boolean coversAll(List<Implicant> implicants, List<Integer> targetMinterms) {
        Set<Integer> covered = new HashSet<>();
        for (Implicant imp : implicants) {
            covered.addAll(imp.minterms);
        }
        return covered.containsAll(targetMinterms);
    }
}
