package coverage;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class ReportGenerator {
    TestSuiteCoverageState finalCoverageState;

    public ReportGenerator(TestSuiteCoverageState finalCoverageState){
        this.finalCoverageState=finalCoverageState;

    }
    public void generateReport() throws IOException {
        for(String packageName : finalCoverageState.fileCoverageMap.keySet()){
            generateOneFile(convertPackageNameToPath(packageName), finalCoverageState.fileCoverageMap.get(packageName));
        }
    }
    public static void generateOneFile(String filepath, FileCoverage fileCoverage) throws IOException {
        // for read
        String basePath=System.getProperty("user.dir");;
        String fullPath=basePath+"/src/main/java/"+filepath;
        BufferedReader bufferedReader=new BufferedReader(new FileReader(fullPath));
        String line=bufferedReader.readLine();
        Integer lineCounter=1;
        // for write
        StringBuilder outputsb=new StringBuilder();


        while(line!=null){
            if(fileCoverage.missedLines.contains(lineCounter)){
                outputsb.append(line+"    // <-- MISSED LINE\n");
            }else{
                outputsb.append(line+"\n");
            }

            line=bufferedReader.readLine();
            lineCounter++;
        }
        bufferedReader.close();

        // for write
        String writePathString=basePath+"/report/"+filepath;
        Path path= Paths.get(writePathString);
        Files.createDirectories(path.getParent());
        try{
            Files.createFile(path);
        }catch (FileAlreadyExistsException ignored){

        }

        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(writePathString));
        bufferedWriter.write(outputsb.toString());
        bufferedWriter.close();
    }
    public static String convertPackageNameToPath(String packageName){
        String[] dirs=packageName.split("\\.");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<dirs.length-1;i++){
            sb.append(dirs[i]);
            sb.append('/');
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append('.');
        sb.append(dirs[dirs.length-1]);
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        String packageName="de.syngenio.demo1.MyTestClass.java";
        String path=convertPackageNameToPath(packageName);
        System.out.println(path);
        HashSet<Integer> missedLines=new HashSet<Integer>();
        missedLines.add(10);
        missedLines.add(19);
        FileCoverage fileCoverage=new FileCoverage(1,missedLines);

        generateOneFile(path, fileCoverage);
        ///Users/jianwendong/evolution/jd48784_yd4788/code/src/main/java/de/syngenio/demo1/MyTestClass.java
    }
}
