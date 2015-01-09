package ch.usi.inf.sape.boaclient;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class UnsafeMethods {

	public static Set<String> get() {
		Class<?> c = sun.misc.Unsafe.class;
		HashSet<String> result = new HashSet<String>();

		for (Method m : c.getMethods()) {
			if (m.getDeclaringClass() != Object.class) {
				result.add(m.getName());
			}
		}

		return result;
	}
	
//	public static String getBoaCode() {
//		String result = "";
//		Set<String> ms = UnsafeMethods.get();
//		for (String m : ms) {
//			System.out.format("add(methods, \"%s\");\n", m);
//		}
//		
//	}
}
