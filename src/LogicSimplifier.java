import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tabular Method(도표법)를 통해 결합 가능한 모든 항을 찾아내고,
 * 결합되지 않은 최종 Prime Implicant(주임플리컨트) 목록을 수집하는 핵심 연산 엔진입니다.
 */
public class LogicSimplifier {
    private int numVars; // 부울 변수 개수 (N)

    /**
     * LogicSimplifier 생성자
     * 
     * @param numVars 입력받을 변수의 개수 (최대 10)
     */
    public LogicSimplifier(int numVars) {
        this.numVars = numVars;
    }

    /**
     * [통합 설계] 입력받은 민텀과 돈케어를 기반으로 전체 Quine-McCluskey의 테이블 결합 과정을 수행하여,
     * 최종 Prime Implicants (PI) 목록을 반환합니다.
     * 
     * @param minterms  오리지널 민텀 목록
     * @param dontCares 돈케어 목록
     * @return 간소화 후보가 될 Prime Implicants 리스트
     */
    public List<Implicant> findPrimeImplicants(List<Integer> minterms, List<Integer> dontCares) {
        // 빠른 조회를 위한 오리지널 민텀 세트 (돈케어 필터링에 사용)
        Set<Integer> mintermSet = new HashSet<>(minterms);

        // 0차(Column 0) 임플리컨트 세트 생성 (민텀 + 돈케어 통합)
        // LinkedHashSet을 사용하여 순서를 유지하면서 중복 입력을 사전에 걸러냅니다.
        Set<Implicant> currentColumn = new LinkedHashSet<>();
        for (int m : minterms) {
            currentColumn.add(new Implicant(numVars, m));
        }
        for (int d : dontCares) {
            currentColumn.add(new Implicant(numVars, d));
        }

        // 수집된 최종 Prime Implicants를 보관할 세트
        Set<Implicant> primeImplicants = new LinkedHashSet<>();

        // 더 이상 새로운 결합 항이 만들어지지 않을 때까지 Column별 루프 수행
        while (!currentColumn.isEmpty()) {
            // 1. Hamming Weight (이진수 내부 1의 개수)에 따라 그룹화
            // 0개부터 N개까지 대응하므로 총 N+1개의 리스트를 가집니다.
            List<List<Implicant>> groups = new ArrayList<>();
            for (int i = 0; i <= numVars; i++) {
                groups.add(new ArrayList<>());
            }
            for (Implicant imp : currentColumn) {
                groups.get(imp.countOnes()).add(imp);
            }

            // 다음 차수(Column)의 결합 항들을 중복 없이 모으기 위한 세트
            Set<Implicant> nextColumnSet = new LinkedHashSet<>();

            // 2. 인접 그룹 간 결합 대조 (i번째 그룹과 i+1번째 그룹)
            for (int i = 0; i < numVars; i++) {
                List<Implicant> groupA = groups.get(i);
                List<Implicant> groupB = groups.get(i + 1);

                for (Implicant a : groupA) {
                    for (Implicant b : groupB) {
                        Implicant combined = a.combine(b);
                        if (combined != null) {
                            // 결합에 성공하면 두 부모 항은 더 이상 PI가 아니므로 사용됨 표시
                            a.isUsed = true;
                            b.isUsed = true;
                            nextColumnSet.add(combined);
                        }
                    }
                }
            }

            // 3. 현재 Column 중 결합에 단 한 번도 사용되지 않은 항들을 PI로 수집
            for (Implicant imp : currentColumn) {
                if (!imp.isUsed) {
                    // [최적화 필터] 오직 돈케어만 커버하는 PI는 최종 간소화 식에 포함될 필요가 없으므로 제외합니다.
                    // 원래 민텀 목록 중 최소 1개 이상의 원소를 커버하고 있을 때만 PI로 인정합니다.
                    boolean coversOriginalMinterm = false;
                    for (int val : imp.minterms) {
                        if (mintermSet.contains(val)) {
                            coversOriginalMinterm = true;
                            break;
                        }
                    }
                    if (coversOriginalMinterm) {
                        primeImplicants.add(imp);
                    }
                }
            }

            // 결합 완료된 다음 Column으로 세대를 교체하여 진행
            currentColumn = nextColumnSet;
        }

        return new ArrayList<>(primeImplicants);
    }
}
