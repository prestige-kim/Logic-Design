import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("변수의 개수 (N, 최대 10): ");
        int numVars = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Minterm 리스트 입력 (예: 0 2 5): ");
        String mintermInput = scanner.nextLine();
        List<Integer> minterms = parseInputString(mintermInput);

        System.out.print("Don't Care 리스트 입력 (없으면 엔터, 예: 1 3): ");
        String dontCareInput = scanner.nextLine();
        List<Integer> dontCares = parseInputString(dontCareInput);

        // 0차 임플리컨트 생성
        List<Implicant> column0 = new ArrayList<>();
        for (int m : minterms) {
            column0.add(new Implicant(numVars, m));
        }
        for (int d : dontCares) {
            column0.add(new Implicant(numVars, d));
        }

        // 5. 1의 개수(0개부터 N개까지)에 따라 그룹 분할
        // N개 변수일 때 가능한 1의 개수는 0 ~ N개이므로, 총 N+1개의 리스트가 필요합니다.
        List<List<Implicant>> groups = new ArrayList<>();
        for (int i = 0; i <= numVars; i++) {
            groups.add(new ArrayList<>());
        }

        // 각 임플리컨트를 1의 개수에 맞는 그룹에 분배
        for (Implicant imp : column0) {
            int onesCount = imp.countOnes();
            groups.get(onesCount).add(imp);
        }

        // 6. 그룹별 출력 검증 (Tabular Method의 첫 번째 표 시각화)
        System.out.println("\n===== [검증] Column 0 그룹화 결과 =====");
        for (int i = 0; i <= numVars; i++) {
            System.out.println("Group " + i + " (1의 개수: " + i + "):");
            List<Implicant> group = groups.get(i);
            if (group.isEmpty()) {
                System.out.println("  (비어 있음)");
            } else {
                for (Implicant imp : group) {
                    System.out.println("  " + imp);
                }
            }
        }
    }

    private static List<Integer> parseInputString(String input) {
        List<Integer> list = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return list;
        }
        String[] tokens = input.split("[,\\s]+");
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                list.add(Integer.parseInt(token.trim()));
            }
        }
        return list;
    }
}