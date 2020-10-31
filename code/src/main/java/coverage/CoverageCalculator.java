package coverage;


import java.util.HashSet;
import java.util.Iterator;

public class CoverageCalculator {
    static private String packagePath;
    static private MavenInvocationHelper helper;

    public CoverageCalculator(String packagePath){
        CoverageCalculator.packagePath = packagePath;
        helper = new MavenInvocationHelper();

    }
    public void calculate(){
//        helper.cleanAll();
//        helper.runTest("de.syngenio.demo4.TestController");
        HashSet<String> affectedTests = helper.runTestWithRTS(); // get all affected tests
        helper.cleanCoverageFile();

        Iterator<String> iter=affectedTests.iterator();
        int n = helper.affectedTests.size();
        for(int i=0;i<n&&iter.hasNext();i++){
            String testName=iter.next();
            System.out.println("ready to run affected test: "+testName);
            helper.runTest(testName);
        }


    }
    public static void main(String[] args) {
        //TODO
        final String packagePath = "de/syngenio";
        CoverageCalculator coverageCalculator = new CoverageCalculator(packagePath);
        coverageCalculator.calculate();
    }

}
