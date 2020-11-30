package coverage;

import java.util.Set;

public class FileCoverage implements java.io.Serializable {
    int numberOfLines; // the number of logical lines.
    int maxLogicalLineNumber; // the maximum line number for logical lines.
    Set<Integer> missedLines;

    public FileCoverage(int numberOfLines, Set<Integer> missedLines) {
        this.numberOfLines = numberOfLines;
        this.missedLines = missedLines;
    }
}
