package eu.larkc.plugin.nquadstontriples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLParser;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;

/**
 * <p>Generated LarKC plug-in skeleton for <code>eu.larkc.plugin.nquadstontriples.NQuadsToNTriples</code>.
 * Use this class as an entry point for your plug-in development.</p>
 */
public class NQuadsToNTriples extends GenericHadoopPlugin
{
	
	public static final URI fixedContext=new URIImpl("http://larkc.eu#Fixedcontext");
 	
	public static final PathFilter FILTER_ONLY_HIDDEN = new PathFilter() {
		public boolean accept(Path p) {
			String name = p.getName();
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};


	/**
	 * Constructor.
	 * 
	 * @param pluginUri 
	 * 		a URI representing the plug-in type, e.g. 
	 * 		<code>eu.larkc.plugin.myplugin.MyPlugin</code>
	 */
	public NQuadsToNTriples(URI pluginUri) {
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
		List<String> paths=DataFactory.INSTANCE.extractObjectsForPredicate(input, Constants.FILEPATH);

		List<Statement> sts=new ArrayList<Statement>();
		
		for (String path: paths) {
			logger.info("Reading from  " + path);
			try {
				Path inPath=new Path(path);
				String nDir=inPath.getParent()+"/dir"+System.currentTimeMillis()+"/";
				new File(nDir).mkdir();
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
				String outputFile;
				if (f.getPath().getName().contains(".xml")) {
					parser = new RDFXMLParser();
					outputFile=f.getPath().getName().replace(".xml", ".nt.gz");
				}
				else if (f.getPath().getName().endsWith(".nq")) {
					parser = new net.fortytwo.sesametools.nquads.NQuadsParser();
					outputFile=f.getPath().getName().replace(".nq", ".nt.gz");
				}
				else { 
					throw new IllegalArgumentException("Can only convert xml and nquads");
				}
				
				
				OutputStreamWriter w = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(nDir+outputFile)));
				final RDFWriter wr= new NTriplesWriter(w);
				wr.startRDF();
				parser.setStopAtFirstError(false);
				parser.setRDFHandler(new RDFHandlerBase() {
					public void handleStatement(Statement s) {
						try {
							if (s.getObject() instanceof Literal)
							wr.handleStatement(new StatementImpl(s.getSubject(), s.getPredicate(), new LiteralImpl(((Literal)s.getObject()).getLabel().replace("\"", ""))));
						} catch (RDFHandlerException e) {
							e.printStackTrace();
						}
					}
				});					
				
				parser.parse(inStream, fixedContext.toString());
				inStream.close();
				wr.endRDF();
				w.close();
				BNode bn=new BNodeImpl(outputFile);
				sts.add(new StatementImpl(bn, Constants.FILEPATH, new LiteralImpl(nDir)));
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			} catch (RDFParseException e) {
				e.printStackTrace();
			} catch (RDFHandlerException e) {
				e.printStackTrace();
			}
			

		}
		return new SetOfStatementsImpl(sts);
		
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
