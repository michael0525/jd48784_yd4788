package coverage;

import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenInvocationHelper {
    class RTSLoggingHandler implements InvocationOutputHandler {
        public boolean foundAffected=false;
        public boolean foundTotal=false;

        Pattern patternAffected = Pattern.compile("Running (.+)");
        Pattern patternTotal = Pattern.compile("TotalTests: (\\d+)");
        public int total=0;
        public void reset(){
            foundAffected=false;
            foundTotal=false;
            affectedTests.clear();
            total=0;
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
        public boolean success=false;
        Pattern patternSuccess = Pattern.compile("Failures: 0");
        Pattern patternTime = Pattern.compile("Time elapsed: ([0-9]+[.][0-9]*) sec");
        double runningTime=0;
        public void reset(){
            success=false;
        }
        @Override
        public void consumeLine(String s) {
//            System.out.println(s);
            Matcher matchSuccess = patternSuccess.matcher(s);
            Matcher matchTime=patternTime.matcher(s);
            if(matchSuccess.find()){
                success=true;
            }
            if(matchTime.find()){
                runningTime=Double.parseDouble(matchTime.group(1));
                System.out.println("running time: "+runningTime+" s");
            }
        }
    }
    HashSet<String> affectedTests=new HashSet<>();
    private RTSLoggingHandler RTSLoggingHandler = new RTSLoggingHandler();
    private String mavenHome;
    private String mavenPom;
    private Invoker invoker;
    private SystemOutLogger systemOutLogger = new SystemOutLogger();;
    private TestLoggingHandler testLoggingHandler = new TestLoggingHandler();
//    private PrintStreamLogger streamLogger;
//    ByteArrayOutputStream byteArrayOutputStream;
//    PrintStream ps;
    InvocationRequest request;


    public MavenInvocationHelper() {

        Properties properties = new Properties();
        InputStream input = null;
        invoker = new DefaultInvoker();
//        byteArrayOutputStream = new ByteArrayOutputStream();
//        final String utf8 = StandardCharsets.UTF_8.name();



        try {
            input = new FileInputStream("project.properties");
            properties.load(input);

            mavenHome = properties.getProperty("maven_home");
            invoker.setMavenHome(new File(mavenHome));

            mavenPom = properties.getProperty("maven_pom");
            request = new DefaultInvocationRequest();
            request.setPomFile(new File(mavenPom));

//            ps = new PrintStream(byteArrayOutputStream, true, utf8);
//            streamLogger = new PrintStreamLogger(ps, InvokerLogger.DEBUG);


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
    public void runTest(String testName){
        testLoggingHandler.reset();
        invoker.setOutputHandler(testLoggingHandler);
        request.setGoals(Collections.singletonList("verify -Dtest="+testName));

        try{
            invoker.execute(request);
            if(testLoggingHandler.success){
                System.out.println("test passed.");
            }else{
                System.out.println("test failed");
            }
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());;
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
    public void runTestWithRTS() {
        RTSLoggingHandler.reset();
//        byteArrayOutputStream.reset();
//        invoker.setLogger(streamLogger);
        request.setGoals(Collections.singletonList("starts:starts"));
        invoker.setOutputHandler(RTSLoggingHandler);
        try{
            invoker.execute(request);
        }catch (MavenInvocationException e){
            System.out.println(e.getMessage());;
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
