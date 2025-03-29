package eu.larkc.plugin.hadoop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.jms.Message;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.mortbay.log.Log;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

import utils.FileUtils;
import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.LabelledGroupOfStatementsImpl;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.shared.SampleQueries;

/**
 * Reads files from HDFS and loads them onto the data layer. Can also read files from the local filesystem.
 * @author spyros
 *
 */
public class HadoopRDFReader extends GenericHadoopPlugin {

	public static final URI fixedContext=new URIImpl("http://larkc.eu#Fixedcontext");
	
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
		
		try {
			final RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection();

			RDFParser parser = new NTriplesParser();
			parser.setRDFHandler(new RDFHandlerBase() {
				public void handleStatement(Statement s) {
					con.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), fixedContext);
				}
			});
			
			FileSystem dfs=FileSystem.get(new Configuration());
			FileStatus[] files=dfs.listStatus(inPath, FileUtils.FILTER_ONLY_HIDDEN);

			for (FileStatus f : files) {
				Log.debug("Loading from " + f.getPath());
				InputStream inStream=new GZIPInputStream(dfs.open(f.getPath()));
				parser.parse(inStream, "defaultNS");
				inStream.close();
			}
			
			return new LabelledGroupOfStatementsImpl(con);
		} catch (Exception e) {
			throw new IllegalStateException("Error loading data from DFS",e);
		} 
	}
	
	/**
	 * For testing
	 * @param args
	 */
	public static void main(String[] args) {
		String path="/Users/spyros/Documents/workspace/1283465196403pool/output-decompressed-Filtered1283465332088";//"/Users/spyros/Documents/workspace/1282926008880pool/output-Filtered1282926152774";
	
		DataFactory.INSTANCE.createRdfStoreConnection().removeStatement(null, null, null, null);
		
		HadoopRDFReader r=new HadoopRDFReader(new URIImpl("http://boo"));

		AttributeValueMap a=new AttributeValueMap();
		a.put(Constants.INPUT, path);
		SetOfStatements sts= r.invoke(a.toRDF());
		
		// The same invocation twice should return cached results.
		r.invoke(a.toRDF());
		
		// Different invocations should call the plugin again.
		a.put("irrelevant", "irrelevant");
		r.invoke(a.toRDF());
		
		SPARQLQueryReasoner reasoner=new SPARQLQueryReasoner(new URIImpl("http://baa"));
		
		ArrayList<Statement> nst=new ArrayList<Statement>(1000);
		CloseableIterator<Statement> it=sts.getStatements();
		while (it.hasNext())
			nst.add(it.next());
		it.close();
		it=DataFactory.INSTANCE.createSPARQLQuery(SampleQueries.PASS_ALL_CONSTRUCT).toRDF().getStatements();
		while (it.hasNext())
			nst.add(it.next());
		it.close();
		
		SetOfStatements results= reasoner.invoke(DataFactory.INSTANCE.createRdfGraph(nst, new URIImpl("http://test")));
		System.out.println(results);
		it=results.getStatements();
		while (it.hasNext())
			System.out.println(it.next());
		it.close();
		
	}


	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
	}
	
}
