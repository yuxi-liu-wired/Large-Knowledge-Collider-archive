package eu.larkc.plugin.datacleaner;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.semanticweb.yars.nx.parser.ParseException;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.plugin.Plugin;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.datacleaner.DataCleaner</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class DataCleaner extends Plugin
{

	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public DataCleaner(URI pluginUri) {
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
		// no init needed
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
		

		List<String> filePaths = DataFactory.INSTANCE.extractObjectsForPredicate(input,Constants.FILEPATH);
		
		ArrayList<Statement> resultTriples=new ArrayList<Statement>();
		
		for (String p:filePaths) {
			String expectedRedirectsFile=p.replace(".data.nq", ".redirs.nx");
			if (new File(expectedRedirectsFile).exists() && new File(p).exists()) {
				LiteralImpl outFile=new LiteralImpl("cleaned"+ System.currentTimeMillis() + ".nq");
				new File(outFile.toString()).deleteOnExit(); // this is a temporary file
				try {
					org.semanticweb.clean.Clean.clean(p, false, expectedRedirectsFile, false, outFile.stringValue(), false);
					Statement s=new StatementImpl(new BNodeImpl(UUID.randomUUID().toString()), Constants.FILEPATH, outFile);
					resultTriples.add(s);
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new SetOfStatementsImpl(resultTriples);
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
