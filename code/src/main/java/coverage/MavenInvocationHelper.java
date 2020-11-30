package coverage;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenInvocationHelper {
    class RTSLoggingHandler implements InvocationOutputHandler {
        HashSet<String> affectedTests=new HashSet<>();
        boolean foundAffected=false;
        boolean foundTotal=false;

        Pattern patternAffected = Pattern.compile("Running (.+)");
        Pattern patternTotal = Pattern.compile("TotalTests: (\\d+)");
        int total=0;
        void reset(){
            foundAffected=false;
            foundTotal=false;
            affectedTests.clear();
            total=0;
        }
        HashSet<String> getAffectedTests(){
            return affectedTests;
        }
        @Override
        public void consumeLine(String s) {
            Matcher matchAffected = patternAffected.matcher(s);
            if (matchAffected.find()) {
                System.out.println("found affected test:");
                foundAffected=true;
                System.out.println(matchAffected.group(1));
                affectedTests.add(matchAffected.group(1));
            }

            Matcher matchTotal = patternTotal.matcher(s);
            if (matchTotal.find()) {
                System.out.println("found total existing tests:");
                foundTotal=true;
                System.out.println(matchTotal.group(1));
                total=Integer.parseInt(matchTotal.group(1));
            }
        }
    }
    class TestLoggingHandler implements InvocationOutputHandler {
        boolean success=true;
        Pattern patternFailed = Pattern.compile("There are test failures.");
        Pattern patternTime = Pattern.compile("Time elapsed: ([0-9]+[.][0-9]*) sec");
        double runningTime=0;
        void reset(){
            runningTime=0;
            success=true;
        }
        @Override
        public void consumeLine(String s) {
//            System.out.println(s);
            Matcher matchFailed = patternFailed.matcher(s);
            Matcher matchTime=patternTime.matcher(s);
            if(matchFailed.find()){
                success=false;
            }
            if(matchTime.find()){
                runningTime+=Double.parseDouble(matchTime.group(1));
                System.out.println("Accumulated running time: "+runningTime+" s");
            }
        }
    }


    HashSet<String> affectedTests=new HashSet<>();
    private RTSLoggingHandler rtsLoggingHandler = new RTSLoggingHandler();
    private Invoker invoker;
//    private SystemOutLogger systemOutLogger = new SystemOutLogger();;
    private TestLoggingHandler testLoggingHandler = new TestLoggingHandler();
    private InvocationRequest request;


    public MavenInvocationHelper() {

        Properties properties = new Properties();
        InputStream input = null;
        invoker = new DefaultInvoker();
        try {
            input = new FileInputStream("project.properties");
            properties.load(input);

            String mavenHome = properties.getProperty("maven_home");
            invoker.setMavenHome(new File(mavenHome));

            String mavenPom = properties.getProperty("maven_pom");
            request = new DefaultInvocationRequest();
            request.setPomFile(new File(mavenPom));
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void cleanAll(){
//        invoker.setLogger(systemOutLogger);
        System.out.println("clean maven");
        request.setGoals(Collections.singletonList("clean"));
        try{
            invoker.execute(request);
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());;
        }
    }
    public double runTest(String testName){
        System.out.println("start running test: " + testName);
        testLoggingHandler.reset();
        invoker.setOutputHandler(testLoggingHandler);
        request.setGoals(Collections.singletonList("verify -Dtest="+testName));

        try{
            invoker.execute(request);
            if(testLoggingHandler.success){
                System.out.println("test passed.");

                return testLoggingHandler.runningTime;
            }else{
                System.out.println("test failed");
                return -1;
            }
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());
            return -1;
        }
    }
    public double runTest(HashSet<String> testNames){
        if(testNames.isEmpty()){
            System.out.println("No test to run");
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        for(String testName:testNames){
            sb.append(testName);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        String testNameString=sb.toString();
        System.out.println("start running test: " + testNameString);
        testLoggingHandler.reset();
        invoker.setOutputHandler(testLoggingHandler);
        request.setGoals(Collections.singletonList("verify -Dtest="+testNameString));

        try{
            invoker.execute(request);
            if(testLoggingHandler.success){
                System.out.println("test passed.");

                return testLoggingHandler.runningTime;
            }else{
                System.out.println("test failed");
                return 0;
            }
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());
            return 0;
        }
    }
    public void cleanCoverageFile(){
        String execFilePath = System.getProperty("user.dir")+"/target/jacoco-ut.exec";
        System.out.println("exec file:" + execFilePath);
        if(deleteFile(execFilePath)){
            System.out.println("deleted.");
        }else{
            System.out.println("exec file not found.");
        }
    }
    public HashSet<String> runTestWithRTS() {
        rtsLoggingHandler.reset();
//        byteArrayOutputStream.reset();
//        invoker.setLogger(streamLogger);
        request.setGoals(Collections.singletonList("starts:starts"));
        invoker.setOutputHandler(rtsLoggingHandler);
        try{
            invoker.execute(request);
            affectedTests = rtsLoggingHandler.getAffectedTests();
            return affectedTests;
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());
            return new HashSet<>();
        }

    }
    public boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
}
