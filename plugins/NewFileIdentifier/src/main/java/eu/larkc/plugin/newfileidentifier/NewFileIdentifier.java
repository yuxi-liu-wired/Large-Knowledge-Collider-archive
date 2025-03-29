package eu.larkc.plugin.newfileidentifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.plugin.Plugin;

/**
 * The NewFileIdentifier plugin identifies a file or directory, passed to it as a parameter. It has a very simple implementation,
 * always returning the same result (i.e. the path to the file or directory). Relative paths are resolved from the directory the
 * platform has been started from. The last modification date for the file is included in the output so that subsequent plugins
 * in the workflow can detect changes to their input and recalculate as appropriate.
 * 
 * Since the output of this plugin depends on external events (namely file modification), caching is disabled for this plugin
 * 
 * 
 * PARAMETERS- _: larkc:filePath <literal pointing to a file in the filesystem>
 * INPUT- none
 * OUTPUT- _: larkc:lastModified <last modified date for file>
 *         _: larkc:filePath <canonical file path>
 *         
 * @author spyros
 *
 */
public class NewFileIdentifier extends Plugin {
	private List<String> filePaths;
	
	/**
	 * 
	 * @param pluginName
	 */
	public NewFileIdentifier(URI pluginName) {
		super(pluginName);
	}

	@Override
	public void initialiseInternal(SetOfStatements params) {
		// Here, the plugin parameters are passed at workflow initialization time.
		// For this plugin, we get the paths of the files or directories to be identified.
		filePaths = DataFactory.INSTANCE.extractObjectsForPredicate(params,Constants.FILEPATH);
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// Returns information about the file passed as an initialization parameter
		// Input is ignored
		
		List<Statement> sts=new ArrayList<Statement>();
		File f;
		for (String filePath: filePaths) {
			try {			
				f = new File(filePath).getCanonicalFile();
				addPath(f,sts);
			} catch (Exception e) {
				throw new IllegalArgumentException("Problem with input file: " + filePath,e);
			}
		}

		return new SetOfStatementsImpl(sts);
	}

	/**
	 * Adds information about a File to the output Statements.
	 * @param f
	 * @param sts
	 */
	private void addPath(File f, List<Statement> sts) {
		BNode bn=new BNodeImpl(f.toString());	
		
		long lastModified=f.lastModified();
		sts.add(new StatementImpl(bn, Constants.LASTMODIFIED, new LiteralImpl(lastModified+"")));
		sts.add(new StatementImpl(bn, Constants.FILEPATH, new LiteralImpl(f.toString())));
	}

	@Override
	public void shutdown() {}

	
	/*DISABLE CACHING*/
	
	/* (non-Javadoc)
	 * @see eu.larkc.plugin.Plugin#cacheLookup(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected SetOfStatements cacheLookup(SetOfStatements input) {
		return null; // Disable caching for this plugin
	}
	/* (non-Javadoc)
	 * @see eu.larkc.plugin.Plugin#cacheInsert(eu.larkc.core.data.SetOfStatements, eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected void cacheInsert(SetOfStatements input, SetOfStatements output) {
		return; // Disable caching for this plugin
	}


	@Override
	protected void shutdownInternal() {
		// do nothing	
	}
	

}
