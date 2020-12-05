package coverage;


import java.io.*;
import java.util.*;

public class CoverageCalculator {
    static private String packagePath;
    static private MavenInvocationHelper helper;
    final static private String mapFileName = "statemap.data";

    public CoverageCalculator(String packagePath) {
        CoverageCalculator.packagePath = packagePath;
        helper = new MavenInvocationHelper();
    }

    public static void outputToFile(HashMap<String, TestSuiteCoverageState> suiteCoverageStateMap) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(mapFileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(suiteCoverageStateMap);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + mapFileName);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static HashMap<String, TestSuiteCoverageState> readFromFile() {
        HashMap<String, TestSuiteCoverageState> result;
        try {
            FileInputStream fileIn = new FileInputStream(mapFileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (HashMap<String, TestSuiteCoverageState>) in.readObject();
            in.close();
            fileIn.close();
            return result;
        } catch (IOException i) {
            System.out.println("No old coverage data was found.");
            System.out.println("If this is the first run, please make sure you have cleaned Starts' data.");
            System.out.println("If you have already cleaned, you can ignore this warning.");
            i.printStackTrace();
            return new HashMap<String, TestSuiteCoverageState>();
        } catch (ClassNotFoundException c) {
            System.out.println("ClassNotFound.");
            c.printStackTrace();
            return new HashMap<String, TestSuiteCoverageState>();
        }
    }
    public void generateReport(HashMap<String, TestSuiteCoverageState> suiteCoverageStateMap, TestSuiteCoverageState targetCoverageState){
        if(suiteCoverageStateMap.isEmpty()){
            System.out.println("empty coverage map!");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("Missed Lines:\n");

        for(String filename : targetCoverageState.fileCoverageMap.keySet()){
            // check through all filenames in targetCoverageState.
            report.append("Filename: "+filename+"\n");
            FileCoverage fc= targetCoverageState.fileCoverageMap.get(filename);
            Set<Integer> missedLines=fc.missedLines;

            for(Iterator<Integer> iter=missedLines.iterator();iter.hasNext();){
                // check whether this line is uncovered in all other states.
                Integer line=iter.next();
                boolean missed=true;
                for(TestSuiteCoverageState state:suiteCoverageStateMap.values()){
                    if(!state.fileCoverageMap.containsKey(filename)){
                        // no such file in old report, the lines should keep status of "missed".
                        System.out.println("No such file in old data. Ignored.");
                        continue;
                    }
                    Set<Integer> missedLinesAnotherSuite=state.fileCoverageMap.get(filename).missedLines;
                    if((!missedLinesAnotherSuite.contains(line)) && line<=state.fileCoverageMap.get(filename).maxLogicalLineNumber){
//                        System.out.println("remove line"+line+"-"+filename);
                        iter.remove();
                        missed=false;
                        break;
                    }
                }
//                if(missed){
//                    report.append("Line "+line+"\n");
//                }
            }
            ArrayList<Integer> sortedLines= new ArrayList<Integer>(missedLines);
            Collections.sort(sortedLines);
            for(Integer line:sortedLines){
                report.append("Line "+line+"\n");
            }
            report.append("\n");
        }
        System.out.println(report);
        ReportGenerator reportGenerator=new ReportGenerator(targetCoverageState);
        try{
            reportGenerator.generateReport();
        }catch (IOException e){
            System.out.println("Exception when generating the report: ");
            e.printStackTrace();
        }



    }
    public void calculate() {
        HashSet<String> affectedTests = helper.runTestWithRTS(); // get all affected tests
        if (affectedTests.isEmpty()) {
            System.out.println("No affected tests.");
            return;
        }
        // read the old TestSuiteCoverageState
        HashMap<String, TestSuiteCoverageState> oldSuiteCoverageStateMap = readFromFile();
        System.out.println("old coverage:");
        System.out.println(oldSuiteCoverageStateMap.toString());
        // try to get hashmap<testSuiteName, TestSuiteCoverageState>
        HashMap<String, TestSuiteCoverageState> suiteCoverageStateMap = new HashMap<>();
        Iterator<String> iter = affectedTests.iterator();
        int n = helper.affectedTests.size();
        TestSuiteCoverageState targetCoverageState = null;
        for (int i = 0; i < n && iter.hasNext(); i++) {
            helper.cleanCoverageFile();
            helper.cleanAll();
            String testName = iter.next();
            System.out.println("ready to run affected test: " + testName);
            double runningTime=helper.runTest(testName);
            if(runningTime==-1){
                System.out.println("Terminate because of test failure.");
                return;
            }

            TestSuiteCoverageState tscs = new TestSuiteCoverageState(packagePath);
            tscs.init();
            suiteCoverageStateMap.put(testName, tscs);
            targetCoverageState=tscs;
        }

        System.out.println("coverage for selected tests:");
        System.out.println(suiteCoverageStateMap.toString());

        for(HashMap.Entry<String, TestSuiteCoverageState> mapEntry:oldSuiteCoverageStateMap.entrySet()){
            if(!suiteCoverageStateMap.containsKey(mapEntry.getKey())){
                suiteCoverageStateMap.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        // Now update coverage data.

        System.out.println("updated coverage:");
        System.out.println(suiteCoverageStateMap.toString());
        outputToFile(suiteCoverageStateMap);

        // Generate coverage report
        generateReport(suiteCoverageStateMap, targetCoverageState);
    }

    public static void main(String[] args) {
        //TODO
        final String packagePath = "de/syngenio";
        CoverageCalculator coverageCalculator = new CoverageCalculator(packagePath);
        coverageCalculator.calculate();
    }

}
