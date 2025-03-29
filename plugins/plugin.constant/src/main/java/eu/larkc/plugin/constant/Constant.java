package eu.larkc.plugin.constant;


import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.constant.Constant</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class Constant extends Plugin
{

	public static final URIImpl GRAPH = new URIImpl("http://larkc.eu/schema#graph");
	private SetOfStatements ret;
	public static final URI fixedContext=new URIImpl("http://larkc.eu#Fixedcontext");
	
	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public Constant(URI pluginUri) {
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
		List<String> graphs = DataFactory.INSTANCE.extractObjectsForPredicate(workflowDescription, GRAPH);
		if (graphs.size()!=1)
			throw new IllegalArgumentException("Constant plugin only supports a single graph");
		String gString;
		try {
			gString = URLDecoder.decode(graphs.get(0),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		URI outputGraphName = super.getNamedGraphFromParameters(workflowDescription, RDFConstants.DEFAULTOUTPUTNAME);
		
		final URI label;
		if (outputGraphName==null)
			label=new URIImpl("http://larkc.eu#arbitraryGraphLabel" + UUID.randomUUID());
		else
			label=outputGraphName;

		RDFParser parser = new NTriplesParser();
		
		final RdfStoreConnection connection=DataFactory.INSTANCE.createRdfStoreConnection();
		
		parser.setStopAtFirstError(true);
		parser.setVerifyData(true);
		parser.setDatatypeHandling(DatatypeHandling.IGNORE);
		parser.setRDFHandler(new RDFHandlerBase() {
			public void handleStatement(Statement s) {
				if (s.getContext()!=null)
					connection.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), (URI)s.getContext(), label);
				else
					connection.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), fixedContext, label);
			}
		});
		
		try {
			parser.parse(new StringReader(gString), outputGraphName.toString());
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not parse constant", e);
		}
		
		final ArrayList<Statement> l=new ArrayList<Statement>();
		l.add(new StatementImpl(new BNodeImpl(UUID.randomUUID()+""), RDFConstants.DEFAULTOUTPUTNAME, label));
		
		ret= new SetOfStatementsImpl(l);
		
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
		return ret;
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
