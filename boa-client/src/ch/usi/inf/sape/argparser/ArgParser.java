package ch.usi.inf.sape.argparser;

import java.lang.reflect.Field;

public class ArgParser {

	private static boolean match(Arg arg, String param) {
		return param.equals("-" + arg.shortkey())
				|| param.equals("--" + arg.longkey());
	}

	public static <T> T parseArgs(String[] args, Class<T> cls)
			throws InstantiationException, IllegalAccessException {
		T result = cls.newInstance();

		for (int i = 0; i < args.length; i++) {
			for (Field f : cls.getFields()) {
				Arg arg = f.getAnnotation(Arg.class);

				if (arg != null) {
					if (match(arg, args[i])) {
						Object value = args[i + 1];
						f.set(result, value);

						i++;
					}
				}
			}
		}

		return result;
	}
}
