import java.util.ArrayList;
import java.util.List;

public class Implicant {
    public String term;            // 이진 문자열 표현 (예: "0-10", "1100")
    public List<Integer> minterms; // 이 항이 커버하는 원래의 Minterm 번호 목록
    public boolean isUsed;

    // 초기 0차(Column 0) 생성자 (예: 5가 입력되면 "0101" 문자열 생성)
    public Implicant(int numVars, int mintermValue) {
        this.term = toBinaryString(mintermValue, numVars);
        this.minterms = new ArrayList<>();
        this.minterms.add(mintermValue);
    }

    // 숫자를 자릿수(numVars)에 맞는 0과 1의 이진 문자열로 변환하는 메서드
    // StringBuilder를 배제하고, 학교 실습에 최적화된 일반 String 연산(+=)을 사용합니다.
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

    // 문자열에서 '1'의 개수를 세어 반환하는 메서드 (Hamming Weight)
    // 일반적인 for 루프와 charAt을 사용하여 구현합니다.
    public int countOnes() {
        int count = 0;
        for (int i = 0; i < term.length(); i++) {
            if (term.charAt(i) == '1') {
                count++;
            }
        }
        return count;
    }

    public boolean canCombineWith(Implicant a, Implicant b, int numVars){
        int count = 0;

        for(int i = 0 ; i < numVars ; i++){
            if(a.term.charAt(i) != b.term.charAt(i)){
                count++;
            }
        }

        if(count == 1)
            return true;
        else
            return false;
    }


    // 검증용 출력 메서드
    @Override
    public String toString() {
        return term + " " + minterms.toString();
    }
}