import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 프로그램의 Entry Point 역할을 하는 Main 클래스입니다.
 * 변수 개수, Minterm, Don't Care의 문자열 입력을 파싱하고,
 * LogicSimplifier 및 PIChart를 호출하여 최종 최적 논리식을 대수적으로 출력합니다.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("  Quine-McCluskey (도표법) 논리식 간소화 프로그램");
        System.out.println("==================================================");

        // 1. 변수의 개수 N 입력
        System.out.print("변수의 개수 (N, 최대 10): ");
        int numVars = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        // 2. Minterm 리스트 입력
        System.out.print("Minterm 리스트 입력 (예: 0 2 5 6 7 8 10 13): ");
        String mintermInput = scanner.nextLine();
        List<Integer> minterms = parseInputString(mintermInput);

        // 3. Don't Care 리스트 입력
        System.out.print("Don't Care 리스트 입력 (없으면 엔터, 예: 1 3): ");
        String dontCareInput = scanner.nextLine();
        List<Integer> dontCares = parseInputString(dontCareInput);

        if (minterms.isEmpty()) {
            System.out.println("\n[오류] Minterm 리스트가 비어 있어 간소화를 진행할 수 없습니다.");
            return;
        }

        // 4. [1단계] LogicSimplifier를 통한 Prime Implicants (PI) 목록 생성
        LogicSimplifier simplifier = new LogicSimplifier(numVars);
        List<Implicant> piList = simplifier.findPrimeImplicants(minterms, dontCares);

        System.out.println("\n--------------------------------------------------");
        System.out.println(" [1단계] 도출된 Prime Implicants (PI) 목록");
        System.out.println("--------------------------------------------------");
        if (piList.isEmpty()) {
            System.out.println("  (없음 - 논리식이 성립하지 않거나 항상 0입니다.)");
        } else {
            for (Implicant pi : piList) {
                System.out.println("  " + pi);
            }
        }

        // 5. [2단계] PIChart 생성 및 최적 해 탐색 (EPI 선택 + 행렬 축소 + 순환 차트 해결)
        PIChart chart = new PIChart(piList, minterms);
        List<Implicant> selected = chart.solve();

        System.out.println("\n--------------------------------------------------");
        System.out.println(" [2단계] 최종 간소화에 선정된 최적 Implicants 목록");
        System.out.println("--------------------------------------------------");
        if (selected.isEmpty()) {
            System.out.println("  (선정된 항이 없습니다.)");
        } else {
            for (Implicant imp : selected) {
                System.out.println("  " + imp);
            }
        }

        // 6. [3단계] 최종 대수 기호 SOP 수식 변환 및 출력
        String sopExpression = convertToSOPExpression(selected);
        System.out.println("\n==================================================");
        System.out.println("  최종 논리식 간소화 결과 (SOP):");
        System.out.println("  Y = " + sopExpression);
        System.out.println("==================================================");
    }

    /**
     * 공백 또는 쉼표(,)로 구분되어 입력된 문자열을 정수 리스트로 파싱합니다.
     */
    private static List<Integer> parseInputString(String input) {
        List<Integer> list = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return list;
        }
        // 공백 및 쉼표 기준으로 토큰 분할
        String[] tokens = input.split("[,\\s]+");
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                list.add(Integer.parseInt(token.trim()));
            }
        }
        return list;
    }

    /**
     * 최종 선정된 Implicant들의 이진 표현(예: "1x01")을 대수학 기호 표현식(예: "AB'D")으로 변환합니다.
     * 대시('x') 자리는 생략되며, 0은 변수 뒤에 프라임(')을 붙이고, 1은 원형을 출력합니다.
     */
    private static String convertToSOPExpression(List<Implicant> selected) {
        if (selected == null || selected.isEmpty()) {
            return "0";
        }

        List<String> terms = new ArrayList<>();
        for (Implicant imp : selected) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < imp.term.length(); i++) {
                char c = imp.term.charAt(i);
                String varName = "x" + (i + 1); // 변수 인덱스에 따라 x1, x2, x3... 매핑

                if (c == '1') {
                    sb.append(varName);
                } else if (c == '0') {
                    sb.append(varName).append("'");
                }
            }
            // 모든 비트가 대시('x')로 소거된 경우 상수 1로 출력
            if (sb.length() == 0) {
                terms.add("1");
            } else {
                terms.add(sb.toString());
            }
        }

        // 각 곱 항(Product Term)들을 합(+) 기호로 연동
        return String.join(" + ", terms);
    }
}
