package eu.larkc.plugin.reasoner.iris;

/**
 * Exception which should be thrown whenever an illegal triple is formed.
 * 
 * @author Iker Larizgoitia, Christoph Fuchs
 * 
 */
public class IllegalTripleException extends IllegalArgumentException {

	private static final long serialVersionUID = 4741172181468222866L;

	/**
	 * Constructor
	 * 
	 * @param string
	 *            message
	 */
	public IllegalTripleException(String string) {
		super(string);
	}

}
