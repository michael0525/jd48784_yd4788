// basic testing
package de.syngenio.demo1;

public class MyTestClass {
	public int doSomething(String param1, int param2) {
		if (param2 < 10) {
			int a =helper();
			if(a>2){
				return param2;
			}
//			return 0;
			return param2;
		} else {
			return Integer.parseInt(param1);
		}
	}
	public int helper(){
		return 4;
	}
}
