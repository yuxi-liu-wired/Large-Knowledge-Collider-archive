package eu.larkc.plugin.dictionaryencoding;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.dictionaryencoding.DictionaryEncoding</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class DictionaryEncoding extends GenericHadoopPlugin
{

	enum Mode{DECODING, ENCODING};
	Mode mode;
	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public DictionaryEncoding(URI pluginUri) {
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
		List<String> mode= (DataFactory.INSTANCE.extractObjectsForPredicate(workflowDescription, Constants.DICTIONARYMODE));
		if (mode.size()!=1)
			throw new IllegalArgumentException("Specify either " +Constants.ENCODE+  " or " +Constants.DECODE +" for Dictionary Encoding.");
		if (mode.get(0).equals(Constants.ENCODE))
			this.mode=Mode.ENCODING;
		else if (mode.get(0).equals(Constants.DECODE))
			this.mode=Mode.DECODING;
		else
			throw new IllegalArgumentException("Unrecognized mode, specify either " +Constants.ENCODE+  " or " +Constants.DECODE);
		
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
			List<String> inputFile=DataFactory.INSTANCE.extractObjectsForPredicate(input, Constants.FILEPATH);
			
			if (inputFile.isEmpty())
				return new SetOfStatementsImpl();
			
			if (inputFile.size()!=1) 
				throw new IllegalArgumentException("Dictionary encoding works for one path at a time");
			
			// Get the input
			
			Path inputPath= new Path(inputFile.get(0));
			inputPath = ensurePathIsOnDFS(config, inputPath);
			
						
			String outputPath;
						
			if (mode.equals(Mode.ENCODING))	{
				Path tempPath=new Path("/tmp/"+this.getClass().getSimpleName().toString()+System.currentTimeMillis()).makeQualified(FileSystem.get(config));
				outputPath=inputPath.toString()+"/../"+System.currentTimeMillis()+"pool/";
				String[] args=new String[]{inputPath.toString(), tempPath.toUri().toString() , outputPath.toString(), "--samplingPercentage", "10", "--samplingThreshold", "1000"};
				runTool(config, "jobs.FilesImportTriples" , args);
			}
			else {
				outputPath=inputPath.toString()+"/output-decompressed"+System.currentTimeMillis();
				String[] args=new String[]{inputPath.toString(), outputPath.toString(), "--samplingPercentage", "10", "--samplingThreshold", "1000"};
				runTool(config, "jobs.FilesExportTriples" , args);
				outputPath+="/triples";
			}
			
			BNode bn=new BNodeImpl(outputPath);
			List<Statement> sts=Collections.singletonList((Statement)new StatementImpl(bn, Constants.FILEPATH, new LiteralImpl(outputPath)));
			
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
	
	public static void main(String[] args) {
		DictionaryEncoding de=new DictionaryEncoding(new URIImpl("http://foo"));
		
		SetOfStatementsImpl sts=new SetOfStatementsImpl(Collections.singletonList((Statement)new StatementImpl(new BNodeImpl("boo"), Constants.DICTIONARYMODE, new LiteralImpl("decode"))));
		de.initialiseInternal(sts);
		
		SetOfStatementsImpl sts2=new SetOfStatementsImpl(Collections.singletonList((Statement)new StatementImpl(new BNodeImpl("boo"), Constants.FILEPATH, new LiteralImpl("/Users/spyros/Documents/Papers/www11tut/timbl-data/1301325559324pool/output-decompressed1301325616421"))));
		de.invokeInternal(sts2);
	}
}
