package eu.larkc.plugin.hadoop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.jms.Message;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.LabelledGroupOfStatementsImpl;
import eu.larkc.core.data.RdfGraphInMemory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.pluginManager.local.LocalPluginManager;
import eu.larkc.core.util.RDFConstants;

/**
 * Reads files from HDFS and loads them onto the data layer. Can also read files from the local filesystem.
 * @author spyros
 *
 */
public class HadoopRDFReader extends GenericHadoopPlugin {

	public static final URI fixedContext=new URIImpl("http://larkc.eu#Fixedcontext");
	private URI outputGraphName;
	
	/** The logger. */
	protected final Logger logger = LoggerFactory
			.getLogger(HadoopRDFReader.class);
	
	public static final PathFilter FILTER_ONLY_HIDDEN = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};
	
	/**
	 * 
	 * @param pluginName
	 */
	public HadoopRDFReader(URI pluginName) {
		super(pluginName);
	}


	/**
	 * Returns information about the file passed as an initialization parameter
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		AttributeValueMap attval= DataFactory.INSTANCE.createAttributeValueList(input);
		Path inPath=new Path(attval.get(Constants.INPUT));
		
		logger.info("Reading from  " + inPath);
		
		final ArrayList<Statement> l=new ArrayList<Statement>(10000);
		

		
		try {
		
			FileSystem dfs=FileSystem.get(config);
			FileStatus[] files=dfs.listStatus(inPath, FILTER_ONLY_HIDDEN);
	
			for (FileStatus f : files) {
				FSDataInputStream is = dfs.open(f.getPath());
				InputStream inStream=null;
				if (f.getPath().getName().endsWith(".gz"))
					inStream=new GZIPInputStream(is);
				else if (f.getPath().getName().endsWith(".zip"))
					inStream=new ZipInputStream(is);
				else
					inStream=is;
				
				RDFParser parser=null;
				if (f.getPath().getName().contains(".xml")) 
					parser = new RDFXMLParser();
				else 
					parser = new NTriplesParser();
				parser.setRDFHandler(new RDFHandlerBase() {
					public void handleStatement(Statement s) {
						l.add(new StatementImpl(s.getSubject(), s.getPredicate(), s.getObject()));
					}
				});
				
				parser.parse(inStream, outputGraphName.toString());
				inStream.close();
				logger.info("Did read from file  " + f.getPath());
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("Error loading data from DFS",e);
		}
		
		return new RdfGraphInMemory(fixedContext, l);
		
		/*
		try {
			final RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection();

			RDFParser parser = new NTriplesParser();
			parser.setRDFHandler(new RDFHandlerBase() {
				public void handleStatement(Statement s) {
					con.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), fixedContext);
				}
			});
			
			FileSystem dfs=FileSystem.get(new Configuration());
			FileStatus[] files=dfs.listStatus(inPath, FILTER_ONLY_HIDDEN);

			for (FileStatus f : files) {
				FSDataInputStream is = dfs.open(f.getPath());
				InputStream inStream=new GZIPInputStream(is);
				parser.parse(inStream, outputGraphName.toString());
				inStream.close();
			}
			
			return new LabelledGroupOfStatementsImpl(con);
		} catch (Exception e) {
			throw new IllegalStateException("Error loading data from DFS",e);
		} */
	}
	
	/**
	 * For testing
	 * @param args
	 */
	public static void main(String[] args) {
//		String path="/Users/spyros/Documents/workspace/1283465196403pool/output-decompressed-Filtered1283465332088";//"/Users/spyros/Documents/workspace/1282926008880pool/output-Filtered1282926152774";
//	
//		DataFactory.INSTANCE.createRdfStoreConnection().removeStatement(null, null, null, null);
//		
//		HadoopRDFReader r=new HadoopRDFReader(new URIImpl("http://boo"));
//
//		AttributeValueMap a=new AttributeValueMap();
//		a.put(Constants.INPUT, path);
//		SetOfStatements sts= r.invoke(a.toRDF());
//		
//		// The same invocation twice should return cached results.
//		r.invoke(a.toRDF());
//		
//		// Different invocations should call the plugin again.
//		a.put("irrelevant", "irrelevant");
//		r.invoke(a.toRDF());
//		
//		SPARQLQueryReasoner reasoner=new SPARQLQueryReasoner(new URIImpl("http://baa"));
//		
//		ArrayList<Statement> nst=new ArrayList<Statement>(1000);
//		CloseableIterator<Statement> it=sts.getStatements();
//		while (it.hasNext())
//			nst.add(it.next());
//		it.close();
//		it=DataFactory.INSTANCE.createSPARQLQuery(SampleQueries.PASS_ALL_CONSTRUCT).toRDF().getStatements();
//		while (it.hasNext())
//			nst.add(it.next());
//		it.close();
//		
//		SetOfStatements results= reasoner.invoke(DataFactory.INSTANCE.createRdfGraph(nst, new URIImpl("http://test")));
//		System.out.println(results);
//		it=results.getStatements();
//		while (it.hasNext())
//			System.out.println(it.next());
//		it.close();
		
	}


	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see eu.larkc.plugin.hadoop.GenericHadoopPlugin#initialiseInternal(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected void initialiseInternal(SetOfStatements params) {
		super.initialiseInternal(params);
		outputGraphName=super.getNamedGraphFromParameters(params, RDFConstants.DEFAULTOUTPUTNAME);
	}
	
	
	
}
