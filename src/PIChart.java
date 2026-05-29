import java.util.ArrayList;
import java.util.List;

/**
 * Prime Implicant Chart (주임플리컨트 도표)를 생성하고 축소 및 해 탐색 기법을 적용하여,
 * 오리지널 민텀들을 모두 커버하는 최소 비용(최소 개수)의 Implicant 조합을 결정하는 분석기입니다.
 */
public class PIChart {
    private List<Implicant> primeImplicants; // 행(Row)에 위치할 Prime Implicants 목록
    private List<Integer> minterms;          // 열(Column)에 위치할 오리지널 민텀 목록
    private boolean[][] chart;               // 차트 본체 (chart[i][j]가 true이면 i번째 PI가 j번째 민텀을 커버함)
    private boolean[] isRowDeleted;          // 행의 삭제(선택 또는 제외) 여부 추적
    private boolean[] isColDeleted;          // 열의 삭제(커버 완료) 여부 추적

    /**
     * [통합 설계] 생성자 내에서 차트 생성 및 오리지널 민텀 간의 포함 여부 매핑을 자동으로 수행합니다.
     * 
     * @param piList 도출된 Prime Implicants 리스트
     * @param mtList 커버 대상이 되는 오리지널 민텀 리스트 (돈케어는 제외)
     */
    public PIChart(List<Implicant> piList, List<Integer> mtList) {
        this.primeImplicants = new ArrayList<>(piList);
        this.minterms = new ArrayList<>(mtList);

        int numRows = primeImplicants.size();
        int numCols = minterms.size();

        this.chart = new boolean[numRows][numCols];
        this.isRowDeleted = new boolean[numRows];
        this.isColDeleted = new boolean[numCols];

        // 2차원 매핑 행렬 작성
        for (int i = 0; i < numRows; i++) {
            Implicant pi = primeImplicants.get(i);
            for (int j = 0; j < numCols; j++) {
                int mt = minterms.get(j);
                // 해당 PI가 이 민텀을 커버하고 있는지 확인
                if (pi.minterms.contains(mt)) {
                    chart[i][j] = true;
                }
            }
        }
    }

    /**
     * [통합 설계] EPI 선정, 행/열 지배 법칙 적용, 그리고 순환 차트 발생 시 최적화 탐색을 유기적으로 호출하여
     * 모든 민텀을 커버하는 가장 단순화된 최종 Implicant 목록을 도출합니다.
     * 
     * @return 최종 선정된 최적의 Implicant 리스트
     */
    public List<Implicant> solve() {
        List<Implicant> selectedPIs = new ArrayList<>();

        int numRows = primeImplicants.size();
        int numCols = minterms.size();

        if (numRows == 0 || numCols == 0) {
            return selectedPIs;
        }

        boolean changed;
        do {
            changed = false;

            // ① 필수 주임플리컨트(EPI) 자동 선택 및 관련 행렬 제거
            changed |= selectEPIs(selectedPIs);

            // ② 행/열 지배(Dominance) 규칙을 적용한 도표 축소
            changed |= reduceChartByDominance();

        } while (changed); // 변화가 없을 때까지 반복 축소

        // ③ 행렬 축소 이후에도 미처 해결되지 않고 남아있는 잔여 순환 차트 해결
        if (!isAllColsCovered()) {
            List<Implicant> remainingOptimal = solveRemainingChartWithBacktracking();
            selectedPIs.addAll(remainingOptimal);
        }

        return selectedPIs;
    }

    /**
     * 단 하나의 active PI에 의해서만 커버되는 민텀(열)이 존재하면 해당 PI를 필수 주임플리컨트(EPI)로 선정합니다.
     */
    private boolean selectEPIs(List<Implicant> selectedPIs) {
        int numRows = primeImplicants.size();
        int numCols = minterms.size();
        boolean foundAny = false;

        for (int col = 0; col < numCols; col++) {
            if (isColDeleted[col]) continue;

            int activeCoverCount = 0;
            int epiRow = -1;

            for (int row = 0; row < numRows; row++) {
                if (isRowDeleted[row]) continue;

                if (chart[row][col]) {
                    activeCoverCount++;
                    epiRow = row;
                }
            }

            // 오직 하나의 활성 행만 이 열을 커버할 수 있는 경우 -> EPI로 확정
            if (activeCoverCount == 1) {
                foundAny = true;
                isRowDeleted[epiRow] = true;
                Implicant epi = primeImplicants.get(epiRow);

                if (!selectedPIs.contains(epi)) {
                    selectedPIs.add(epi);
                }

                // 이 EPI가 커버하는 모든 열(민텀)을 삭제 처리 (커버 완료 표시)
                for (int c = 0; c < numCols; c++) {
                    if (chart[epiRow][c]) {
                        isColDeleted[c] = true;
                    }
                }
            }
        }
        return foundAny;
    }

    /**
     * 행 지배(Row Dominance)와 열 지배(Column Dominance)를 행렬에 적용하여 중복되거나 불필요한 차트를 지웁니다.
     */
    private boolean reduceChartByDominance() {
        int numRows = primeImplicants.size();
        int numCols = minterms.size();
        boolean changed = false;

        // 1. 행 지배 (Row Dominance)
        // 행 A가 행 B의 커버 범위를 완벽히 포함(Superset)하고 있다면, 더 좁은 범위만 커버하는 행 B는 쓸데없으므로 삭제합니다.
        for (int i = 0; i < numRows; i++) {
            if (isRowDeleted[i]) continue;
            for (int j = 0; j < numRows; j++) {
                if (i == j || isRowDeleted[j]) continue;

                if (doesRowDominate(i, j)) {
                    isRowDeleted[j] = true; // 지배당하는 j행을 삭제
                    changed = true;
                }
            }
        }

        // 2. 열 지배 (Column Dominance)
        // 열 A를 만족하기 위해 필요한 PI 리스트가 열 B를 만족하기 위한 PI 리스트의 완벽한 포함(Superset) 관계라면,
        // 더 좁고 까다로운 열 B를 해결하면 열 A는 무조건 함께 해결되므로, 더 쉽게 해결되는 열 A는 차트에서 지워도 무방합니다.
        for (int i = 0; i < numCols; i++) {
            if (isColDeleted[i]) continue;
            for (int j = 0; j < numCols; j++) {
                if (i == j || isColDeleted[j]) continue;

                if (doesColDominate(i, j)) {
                    isColDeleted[i] = true; // 더 쉽게 커버되는 i열을 삭제
                    changed = true;
                }
            }
        }

        return changed;
    }

    /**
     * 행 rowA가 행 rowB보다 지배적인지(rowB가 커버하는 모든 활성 열을 rowA도 커버하는지) 판별합니다.
     */
    private boolean doesRowDominate(int rowA, int rowB) {
        int numCols = minterms.size();
        boolean hasMore = false;
        boolean coversAll = true;

        for (int col = 0; col < numCols; col++) {
            if (isColDeleted[col]) continue;

            if (chart[rowB][col]) {
                if (!chart[rowA][col]) {
                    coversAll = false; // B가 커버하는 비트를 A가 커버하지 못하므로 탈락
                    break;
                }
            } else if (chart[rowA][col]) {
                hasMore = true; // A가 B보다 더 널널하게 많이 커버함
            }
        }

        if (!coversAll) return false;

        // 두 행의 활성 열 커버리지가 완벽하게 동일한 경우, 무한 상호 삭제를 예방하기 위해 인덱스 크기를 기준으로 타이 브레이킹
        if (!hasMore && rowA > rowB) {
            return false;
        }

        return true;
    }

    /**
     * 열 colA가 열 colB보다 지배적인지(colB를 커버하는 모든 활성 행이 colA도 커버하는지) 판별합니다.
     */
    private boolean doesColDominate(int colA, int colB) {
        int numRows = primeImplicants.size();
        boolean hasMore = false;
        boolean coversAll = true;

        for (int row = 0; row < numRows; row++) {
            if (isRowDeleted[row]) continue;

            if (chart[row][colB]) {
                if (!chart[row][colA]) {
                    coversAll = false; // B를 만족하는 PI가 A는 만족시키지 못하므로 탈락
                    break;
                }
            } else if (chart[row][colA]) {
                hasMore = true; // A를 만족시킬 수 있는 선택지가 B보다 더 많음 (더 쉽게 커버 가능)
            }
        }

        if (!coversAll) return false;

        // 두 열의 커버 구조가 완전히 같을 경우 무한 삭제를 방지하기 위해 타이 브레이킹
        if (!hasMore && colA > colB) {
            return false;
        }

        return true;
    }

    /**
     * 모든 민텀 열이 완전히 지워졌는지(커버되었는지) 확인합니다.
     */
    private boolean isAllColsCovered() {
        for (boolean deleted : isColDeleted) {
            if (!deleted) return false;
        }
        return true;
    }

    /**
     * [백트래킹 최적해 탐색] 필수 EPI 선택과 도표 축소를 마친 후에도 남아있는 순환 도표를 대상으로
     * 가지치기(Pruning)를 동반한 백트래킹을 적용하여 가장 적은 개수의 PI 조합을 엄밀하게 찾아냅니다.
     */
    private List<Implicant> solveRemainingChartWithBacktracking() {
        List<Integer> activeCols = new ArrayList<>();
        for (int col = 0; col < minterms.size(); col++) {
            if (!isColDeleted[col]) {
                activeCols.add(col);
            }
        }

        List<Integer> activeRows = new ArrayList<>();
        for (int row = 0; row < primeImplicants.size(); row++) {
            if (!isRowDeleted[row]) {
                activeRows.add(row);
            }
        }

        List<Integer> bestCombination = new ArrayList<>();
        backtrack(activeRows, activeCols, 0, new ArrayList<>(), bestCombination);

        List<Implicant> result = new ArrayList<>();
        for (int rowIndex : bestCombination) {
            result.add(primeImplicants.get(rowIndex));
        }
        return result;
    }

    /**
     * 백트래킹 코어 탐색
     */
    private void backtrack(List<Integer> activeRows, List<Integer> activeCols, int startRowIndex,
                           List<Integer> currentCombination, List<Integer> bestCombination) {
        // 이미 구한 최적 조합의 크기를 넘어서면 탐색 중지 (Pruning)
        if (!bestCombination.isEmpty() && currentCombination.size() >= bestCombination.size()) {
            return;
        }

        // 현재 조합으로 남은 모든 열이 커버되는지 검증
        if (coversAllCols(currentCombination, activeCols)) {
            bestCombination.clear();
            bestCombination.addAll(currentCombination);
            return;
        }

        // 더 이상 고를 수 있는 행이 없는 경우 탐색 종료
        if (startRowIndex >= activeRows.size()) {
            return;
        }

        // 1. 해당 행을 선택하지 않는 분기 탐색
        backtrack(activeRows, activeCols, startRowIndex + 1, currentCombination, bestCombination);

        // 2. 해당 행을 선택하는 분기 탐색
        int nextRow = activeRows.get(startRowIndex);
        currentCombination.add(nextRow);
        backtrack(activeRows, activeCols, startRowIndex + 1, currentCombination, bestCombination);
        currentCombination.remove(currentCombination.size() - 1); // 백트래킹 롤백
    }

    /**
     * 선택된 조합이 모든 잔여 열을 커버하는지 판단합니다.
     */
    private boolean coversAllCols(List<Integer> rowIndices, List<Integer> activeCols) {
        for (int col : activeCols) {
            boolean covered = false;
            for (int row : rowIndices) {
                if (chart[row][col]) {
                    covered = true;
                    break;
                }
            }
            if (!covered) return false;
        }
        return true;
    }
}
