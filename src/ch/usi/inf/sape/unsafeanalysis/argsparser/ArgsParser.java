package ch.usi.inf.sape.unsafeanalysis.argsparser;

import java.lang.reflect.Field;

/**
 * 
 * @author Luis Mastrangelo
 *
 */
public class ArgsParser {

	private static <T> String getUsage(Class<T> cls)
			throws InstantiationException, IllegalAccessException {

		String usage = "Usage:\n";
		for (Field f : cls.getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				usage += String.format("  -%s, --%s: %s\n", arg.shortkey(),
						arg.longkey(), arg.desc());
			}
		}

		return usage;
	}

	private static String match(Arg arg, String param) {
		String[] parts = param.split("=");
		if (parts.length == 2) {
			String key = parts[0];
			String value = parts[1];
			return key.equals("-" + arg.shortkey())
					|| key.equals("--" + arg.longkey()) ? value : null;
		} else {
			return null;
		}
	}

	private static <T> void setField(T result, Field f, String value)
			throws IllegalArgumentException, IllegalAccessException {
		if (f.getType() == String[].class) {
			String parts[] = value.split(",");
			f.set(result, parts);
		} else if (f.getType() == Integer.class) {
			f.set(result, Integer.parseInt(value));
		} else {
			f.set(result, value);
		}
	}

	private static <T> void set(String[] args, Arg arg, T result, Field f)
			throws IllegalArgumentException, IllegalAccessException,
			ArgumentMissingException {
		for (int i = 0; i < args.length; i++) {
			String value = match(arg, args[i]);
			if (value != null) {
				setField(result, f, value);
				return;
			}
		}

		throw new ArgumentMissingException();
	}

	private static <T> T internalParse(String[] args, Class<T> cls)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ArgumentMissingException {
		T result = cls.newInstance();

		for (Field f : cls.getFields()) {
			Arg arg = f.getAnnotation(Arg.class);

			if (arg != null) {
				set(args, arg, result, f);
			}
		}

		return result;
	}

	public static <T> T parse(String[] args, Class<T> cls)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException {
		try {
			return internalParse(args, cls);
		} catch (ArgumentMissingException e) {
			String usage = getUsage(cls);
			System.out.println(usage);

			System.exit(1);

			return null;
		}
	}
}
