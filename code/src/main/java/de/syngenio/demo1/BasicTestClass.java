// basic testing
package de.syngenio.demo1;

public class BasicTestClass {
	public int maxValueWithinThreshold(int num1, int num2, int num3) {
		int maxNum = -1;
//		Threshold.threshold = pollutedValue();
		if (num1 < num2) {
			if (num2 < num3) {
				maxNum = num3;
			} else {
				maxNum = num2;
			}
		} else {  //num2 <= num1
			if (num1 < num3) {
				maxNum = num3;
			} else {
				maxNum = num1;
			}
		}
		if (maxNum <= Threshold.getThreshold()) {
			return maxNum;
		} else {
			return Threshold.getThreshold();
		}
	}

	public int pollutedValue() {
		return Integer.MAX_VALUE;
	}
}
