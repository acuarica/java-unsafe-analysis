package ch.usi.inf.sape.unsafeanalysis.argsparser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that a given field is target of an command line
 * argument.
 * 
 * @author Luis Mastrangelo
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Arg {

	/**
	 * The short key used for this argument. Usually the short key is a single
	 * letter.
	 * 
	 * @return The short key of this argument.
	 */
	public String shortkey();

	/**
	 * The long key for this argument. The long key is an alternative to the
	 * short key, usually used in scripts to self-document.
	 * 
	 * @return The long ley of this argument.
	 */
	public String longkey();

	/**
	 * The desc is used to provide some help text to the user to indicate what
	 * is the purpose of the argument.
	 * 
	 * @return The description of this argument.
	 */
	public String desc();

}
