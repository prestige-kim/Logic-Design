import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 이진 항(Minterm 및 결합된 항)을 표현하는 핵심 데이터 객체입니다.
 * 스스로 다른 항과의 결합 유효성을 판단하고 결합을 수행하는 책임을 집니다.
 */
public class Implicant {
    public String term;            // 이진 문자열 표현 (예: "01x0", "1100")
    public List<Integer> minterms; // 이 항이 커버하는 원래의 Minterm 번호 목록
    public boolean isUsed;         // 결합 과정에서 사용되었는지 여부 (최종 PI 판별용)

    /**
     * [0차 생성자] 10진수 민텀 번호를 지정된 변수 자릿수에 맞는 이진 문자열로 변환하여 생성합니다.
     * 
     * @param numVars      변수의 개수 (이진 자릿수)
     * @param mintermValue 10진수 민텀 값
     */
    public Implicant(int numVars, int mintermValue) {
        this.term = toBinaryString(mintermValue, numVars);
        this.minterms = new ArrayList<>();
        this.minterms.add(mintermValue);
        this.isUsed = false;
    }

    /**
     * [N차 생성자] 결합된 형태의 이진 표현과 그에 대응되는 민텀 목록으로 상위 객체를 생성합니다.
     * 
     * @param term     이진 문자열 표현 (예: "01x0")
     * @param minterms 커버하는 Minterm 번호 리스트
     */
    public Implicant(String term, List<Integer> minterms) {
        this.term = term;
        // 외부 리스트의 부작용을 막기 위해 복사하여 새로 생성합니다.
        this.minterms = new ArrayList<>(minterms);
        this.isUsed = false;
    }

    /**
     * 10진수 숫자를 자릿수에 맞춰 0과 1로 구성된 2진수 문자열로 변환합니다.
     */
    private String toBinaryString(int value, int numVars) {
        String result = "";
        int temp = value;
        for (int i = 0; i < numVars; i++) {
            result = (temp % 2) + result;
            temp = temp / 2;
        }
        return result;
    }

    /**
     * 현재 이진 표현에서 문자 '1'의 개수(Hamming Weight)를 세어 반환합니다.
     * 대시('x')는 카운트하지 않습니다.
     * 
     * @return '1'의 개수
     */
    public int countOnes() {
        int count = 0;
        for (int i = 0; i < term.length(); i++) {
            if (term.charAt(i) == '1') {
                count++;
            }
        }
        return count;
    }

    /**
     * [통합 설계] 다른 항과의 결합을 수행합니다.
     * 대시('x')의 위치가 서로 완전히 일치하고, 대시 이외의 자리 중 단 한 곳만 다를 때 결합이 성공합니다.
     * 결합에 성공하면 그 다른 비트를 대시('x')로 치환하고, 두 객체의 민텀 목록을 병합한 새로운 Implicant를 반환합니다.
     * 결합이 불가능한 구조일 경우 null을 반환합니다.
     * 
     * @param other 비교 및 결합 대상이 되는 다른 Implicant 객체
     * @return 결합된 새로운 Implicant 객체, 혹은 결합 불가능 시 null
     */
    public Implicant combine(Implicant other) {
        if (this.term.length() != other.term.length()) {
            return null;
        }

        int diffIndex = -1;
        for (int i = 0; i < this.term.length(); i++) {
            char c1 = this.term.charAt(i);
            char c2 = other.term.charAt(i);

            if (c1 != c2) {
                // 대시('x') 위치가 다르거나 다른 문자가 이미 발견된 경우 (2개 이상의 문자가 다름) 결합 불가
                if (c1 == 'x' || c2 == 'x' || diffIndex != -1) {
                    return null;
                }
                diffIndex = i;
            }
        }

        // 모든 문자가 같으면 결합 불가
        if (diffIndex == -1) {
            return null;
        }

        // 한 자리의 다른 문자를 'x'로 교체하여 새로운 문자열 작성
        String newTerm = this.term.substring(0, diffIndex) + "x" + this.term.substring(diffIndex + 1);

        // 자연 정렬 및 중복을 제거하며 두 리스트의 민텀 목록 병합
        Set<Integer> mergedMinterms = new TreeSet<>(this.minterms);
        mergedMinterms.addAll(other.minterms);

        return new Implicant(newTerm, new ArrayList<>(mergedMinterms));
    }

    /**
     * HashSet 등 해시 기반 자료구조에서 객체의 중복을 자동으로 방지(O(1))할 수 있도록
     * 논리적으로 동일한 이진 표현(term)을 가지면 동등한 객체로 취급하도록 정의합니다.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Implicant other = (Implicant) obj;
        return this.term.equals(other.term);
    }

    /**
     * equals가 true를 반환하는 객체들은 동일한 해시 코드를 보장해야 합니다.
     */
    @Override
    public int hashCode() {
        return this.term.hashCode();
    }

    /**
     * 콘솔/디버깅 출력용 이진 표현 및 민텀 리스트 문자열 표현입니다. (예: "01x0 [2, 6]")
     */
    @Override
    public String toString() {
        return term + " " + minterms.toString();
    }
}
