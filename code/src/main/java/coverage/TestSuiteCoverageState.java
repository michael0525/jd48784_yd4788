package coverage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TestSuiteCoverageState implements java.io.Serializable {
    String packageName;
    Map<String, FileCoverage> fileCoverageMap;

    public TestSuiteCoverageState(String packageName) {
        String[] nameArr = packageName.split("/", -1);
        StringBuilder newPackageName = new StringBuilder();
        for (String name : nameArr) {
            newPackageName.append(name);
            newPackageName.append('.');
        }
        newPackageName.deleteCharAt(newPackageName.length() - 1);
        this.packageName = newPackageName.toString();

        this.fileCoverageMap = new HashMap<>();
    }

    public void init() {
        // Get the root directory
        String rootDir = System.getProperty("user.dir");
        // Get the directory for the target coverage data files
        File directoryPath = new File(rootDir + "/target/coverage-report");
        //List of all files and directories inside the jacoco directory
        File[] fileList = directoryPath.listFiles();
        //Get the test suite directories from the file directories
        List<File> testSuiteList = new ArrayList<>();
        for (File file : fileList) {
            if (file.getName().startsWith(packageName)) {
                testSuiteList.add(file);
            }
        }

        for (File testSuiteDir : testSuiteList) {
            File[] files = testSuiteDir.listFiles();
            List<File> htmlFileList = new ArrayList<>();
            for (File file : files) {
                if (file.getName().contains("java")) {
                    htmlFileList.add(file);
                }
            }
            for (File htmlFile : htmlFileList) {
                String content = fileToString(htmlFile);

                //Total number of lines in the file
                int maxLogicalLineNumber = getMaximumLocalLineNumber(content);

                //Total number of logical lines
                int numberOfLines = content.split("id=\"L", -1).length - 1;

                //Get package name and file name
                int packageNameStartIndex = content.indexOf("class=\"el_package\">") + 19;
                int packageNameLength = content.substring(packageNameStartIndex).indexOf("<");
                String packageName = content.substring(packageNameStartIndex, packageNameStartIndex + packageNameLength);

                int fileNameStartIndex = content.indexOf("class=\"el_source\">") + 18;
                int fileNameLength = content.substring(fileNameStartIndex).indexOf("<");
                String fileName = content.substring(fileNameStartIndex, fileNameStartIndex + fileNameLength);
                //Combine two names
                String name = packageName + "." + fileName;
                Set<Integer> missedLines = new HashSet<>();
                //Get the missed line numbers and put them into a set
                String[] splitArr = content.split("class=\"nc\" id=\"L", -1);
                for (int i = 1; i < splitArr.length; i++) {
                    String cur = splitArr[i];
                    Integer lineNumber = Integer.parseInt(cur.substring(0, cur.indexOf("\"")));
                    missedLines.add(lineNumber);
                }

                //Create a FileCoverage object and put it into the fileCoverageMap
                FileCoverage fc = new FileCoverage(numberOfLines, missedLines);
                fc.maxLogicalLineNumber=maxLogicalLineNumber;
                fileCoverageMap.put(name, fc);
            }
        }
    }

    private int getMaximumLocalLineNumber(String content) {
        Pattern patternLine = Pattern.compile("id=\"L(\\d+)\"");
        Matcher matchLine = patternLine.matcher(content);
        int result=-1;
        while(matchLine.find()){
            result=Integer.parseInt(matchLine.group(1));
        }
        if(result!=-1){
//            System.out.println("last line="+result);
            return result;
        }else{
            return 0;
        }

    }

    private String fileToString(File file) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(file.getPath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static void main(String[] args) {
        TestSuiteCoverageState tscs = new TestSuiteCoverageState("de/syngenio");
        tscs.init();
    }
}
