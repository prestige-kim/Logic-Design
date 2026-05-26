import java.util.ArrayList;
import java.util.List;

public class LogicSimplifier {
    private int numVars;

    public LogicSimplifier(int numVars){
        this.numVars = numVars;
    }

    public List<List<Implicant>> groupByHammingWeight(List<Implicant> implicants){
        List<List<Implicant>> groups = new ArrayList<>();

        for(int i = 0 ; i <= numVars ; i++){
            groups.add(new ArrayList<>());
        }

        for(Implicant imp : implicants){
            int countOne = imp.countOnes();
            groups.get(countOne).add(imp);
        }

        return groups;
    }

    public void check(List<List<Implicant>> groups){
        for(int i = 0 ; i < numVars ; i++){
            List<Implicant> groupA = groups.get(i);
            List<Implicant> groupB = groups.get(i+1);

            for(Implicant a : groupA){
                for(Implicant b : groupB){
                    if(a.canCombineWith(b)){
                    }
                }
            }
        }
    }
}
