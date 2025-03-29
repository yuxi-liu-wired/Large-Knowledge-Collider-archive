package eu.larkc.plugin.webpie;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.webpie.WebPIE</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class WebPIE extends GenericHadoopPlugin
{

	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public WebPIE(URI pluginUri) {
		super(pluginUri);
		// TODO Auto-generated constructor stub
	}


	/**
	 * Called on plug-in initialisation. The plug-in instances are initialised on
	 * workflow initialisation.
	 * 
	 * @param workflowDescription 
	 * 		set of statements containing plug-in specific 
	 * 		information which might be needed for initialization (e.g. plug-in parameters).
	 */
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		super.initialiseInternal(workflowDescription);
	}

	/**
	 * Called on plug-in invokation. The actual "work" should be done in this method.
	 * 
	 * @param input 
	 * 		a set of statements containing the input for this plug-in
	 * 
	 * @return a set of statements containing the output of this plug-in
	 */


	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
			List<String> inputFile = DataFactory.INSTANCE
					.extractObjectsForPredicate(input, Constants.FILEPATH);

			if (inputFile.isEmpty())
				return new SetOfStatementsImpl();

			if (inputFile.size() != 1)
				throw new IllegalArgumentException(
						"WebPIE works for one path at a time");

			// Get the input
			Path inputPath = new Path(inputFile.get(0));
			inputPath = ensurePathIsOnDFS(config, inputPath);
			
			String[] args = new String[] { inputPath.toString(), "--fragment",
					"rdfs", "--rulesStrategy", "fixed", "--duplicatesStrategy",
					"end" };

			runTool(config, "jobs.Reasoner", args);

			BNode bn = new BNodeImpl(inputPath.toString());
			List<Statement> sts = Collections
					.singletonList((Statement) new StatementImpl(bn,
							Constants.FILEPATH, new LiteralImpl(inputPath
									.toString())));

			return new SetOfStatementsImpl(sts);
		}

		catch (IOException e) {
			e.printStackTrace(); // TODO exception handling
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SetOfStatementsImpl();
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow deletion.
	 * Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
	}
}
