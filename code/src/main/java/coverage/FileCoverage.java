package coverage;

import java.util.Set;

public class FileCoverage {
    int numberOfLines;
    Set<Integer> missedLines;

    public FileCoverage(int numberOfLines, Set<Integer> missedLines) {
        this.numberOfLines = numberOfLines;
        this.missedLines = missedLines;
    }
}
