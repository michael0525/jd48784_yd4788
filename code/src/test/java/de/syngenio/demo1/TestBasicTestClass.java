package de.syngenio.demo1;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestBasicTestClass {

	private BasicTestClass testObject;

	@Before
	public void setUp() {
		testObject = new BasicTestClass();
	}

	@Test
	public void assureThatFirstNumberIsMaxNumber() {
		int num1 = 3, num2 = 2, num3 = 1;
		int expectedVal = testObject.maxValueWithinThreshold(num1, num2, num3);
		int threshold = Threshold.getThreshold();
		assertEquals(expectedVal, Math.min(num1, threshold));
	}

	@Test
	public void assureThatSecondNumberIsMaxNumber() {
		int num1 = 1, num2 = 2, num3 = 1;
		int expectedVal = testObject.maxValueWithinThreshold(num1, num2, num3);
		int threshold = Threshold.getThreshold();
		assertEquals(expectedVal, Math.min(num2, threshold));
	}

	@Test
	public void assureThatThirdNumberIsMaxNumber() {
		int num1 = 1, num2 = 2, num3 = 3;
		int expectedVal = testObject.maxValueWithinThreshold(num1, num2, num3);
		int threshold = Threshold.getThreshold();
		assertEquals(expectedVal, Math.min(num3, threshold));
	}

	@Test
	public void mockTest() {
		assertEquals(true, true);
	}
}
