package eu.larkc.plugin.ldspider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;


import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackNQOutputStream;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.Crawler.Mode;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerNx;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.content.ContentHandlers;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.plugin.Plugin;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.ldspider.LDSpiderPlugin</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class LDSpiderPlugin extends Plugin
{
	
	private static final String errorLogFile = "LDSpiderPluginErrorLog.log";
	int depth = 2;
	int maxURIs = 100;
	boolean includeABox = true;
	boolean includeTBox = false;
	private Frontier frontier;
	private Crawler crawler;
	private int maxplds=10000;

	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public LDSpiderPlugin(org.openrdf.model.URI pluginUri) {
		super(pluginUri);
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
		crawler=new Crawler(10);
		
		
		ContentHandler contentHandler = new ContentHandlers(new ContentHandlerRdfXml(), new ContentHandlerNx());
		crawler.setContentHandler(contentHandler);
		
		//Print to Stdout
		PrintStream ps = System.out;
		//Print to file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(errorLogFile);
			//Add printstream and file stream to error handler
			Callback rcb = new CallbackNQOutputStream(fos);
			ErrorHandler eh = new ErrorHandlerLogger(ps, rcb);
			rcb.startDocument();

			//Connect hooks with error handler
			crawler.setErrorHandler(eh);
		} catch (FileNotFoundException e) {
			logger.warn("Could not open log file for writing",e);
		}

		
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
		String outputFile="crawledData-"+System.currentTimeMillis();
		
		List<String> seedFiles=DataFactory.INSTANCE.extractObjectsForPredicate(input, Constants.FILEPATH);
		
		frontier=new BasicFrontier();
		for (String s: seedFiles) {
			try {
				BufferedReader b=new BufferedReader(new FileReader(s));
				String line=null;
				while ((line=b.readLine())!=null)
					if (!line.trim().isEmpty())
						frontier.add(new URI(line));
			}
			catch (Exception e) {
				logger.warn("Problem reading seed URLs from file: " + s);
			}
		}
		
		OutputStream os;
		try {
			os = new FileOutputStream(outputFile);
			Sink sink = new SinkCallback(new CallbackNQOutputStream(os));
			crawler.setOutputCallback(sink);			
			crawler.evaluateBreadthFirst(frontier, depth, maxURIs, maxplds, Mode.ABOX_AND_TBOX_EXTRAROUND);
			
			BNode bn=new BNodeImpl(outputFile);
			List<Statement> sts=Collections.singletonList((Statement)new StatementImpl(bn, Constants.FILEPATH, new LiteralImpl(outputFile)));
			
			return new SetOfStatementsImpl(sts);
		} catch (FileNotFoundException e) {
			logger.error("Can not open file for writing: " + outputFile,e);
		}
		return new SetOfStatementsImpl(); // return nothing, plugin has failed
	}

	/**
	 * Called on plug-in destruction. Plug-ins are destroyed on workflow deletion.
	 * Free an resources you might have allocated here.
	 */
	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
	}
	
	
/*	public static void main(String[] args) {
		LDSpiderPlugin ldspider=new LDSpiderPlugin(new URIImpl("http://irrelevant"));
		
		ldspider.initialiseInternal(null);

	}*/
}
