package eu.larkc.plugin.transform.urbancomputing.ubl;

import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import eu.larkc.core.data.iterator.RDFHandlerIterator;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.plugin.Plugin;
import eu.larkc.urbancomputing.LarKCUtilities;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.util.RDFConstants;

/**
 * This plugin applies an XSL transformation to an XML document to convert it in RDF.
 * Locations of XML document and its transformations are set in a XMLDocument object.
 * 
 * @author Daniele Dell'Aglio
 * Ported to 2.0 by Bas Groenewoud & Chris Dijkshoorn
 * Ported to 2.5 by Daniele Dell'Aglio, Silviu Bota
 */
public class XML2RDFTransformer extends Plugin {
	//only first time when called, return results (anytime b.)
	private boolean once = false;
	private boolean importConfig = false;
	
	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	public static final URI GRAPH_PROPERTY = new URIImpl("http://larkc.cefriel.it/ontologies/plugin#isPartOfGraph");
	public static final URI XSLT_PROPERTY = new URIImpl("http://larkc.cefriel.it/ontologies/plugin#hasXslt");

	DataFactory df = DataFactory.INSTANCE;
	HashSet<Statement> statements = new HashSet<Statement>();
	
	public XML2RDFTransformer(URI pluginName) {
		super(pluginName);
	}

	
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
//		if(!importConfig){
//			DataFactory.INSTANCE.createRdfStoreConnection().removeStatement(null, null, null, this.getIdentifier());
//			LarKCUtilities.importRdfGraph(new File("plugins/XML2RDFTransformer/configuration-schema/configuration.rdf"), this.getIdentifier());
//			importConfig=true;
//		}
	}
	
	public SetOfStatements invokeInternal(SetOfStatements sos) {
		System.out.println("XML2RDFTransformer:invokeInternal-start");
		if (once) { return null; }
		once = true;

		logger.debug("XML2RDFTransformer invoked.");
		
		if (sos == null) {
			return null;
		}
		
		URI graphName = null; 
		URL doc = null;
		String xslt = null;
		
		CloseableIterator<Statement> it = sos.getStatements();
		
		while(it.hasNext()){
			Statement s = it.next();
			
			if(s.getPredicate().equals(XSLT_PROPERTY)){
				try{
					doc = new URL(s.getSubject().stringValue());
				}catch (MalformedURLException e) {
					System.err.println("Malformed URL Exception");
					return null;
				}
				xslt = s.getObject().stringValue();
			}
			else if(s.getPredicate().equals(GRAPH_PROPERTY)){
				graphName = new URIImpl(s.getObject().stringValue());
			}
			s = it.next();
			
			if(s.getPredicate().equals(XSLT_PROPERTY)){
				try{
					doc = new URL(s.getSubject().stringValue());
				}catch (MalformedURLException e) {
					System.err.println("Malformed URL Exception");
					return null;
				}
				xslt = s.getObject().stringValue();
			}
			else if(s.getPredicate().equals(GRAPH_PROPERTY)){
				graphName = new URIImpl(s.getObject().stringValue());
			}
		
			if(doc==null || xslt==null || graphName == null){
				//raise exception
				logger.error("Error! One of the two parameters is missing!");
				return null;
			}
			else{
			}
			
			try {
				ByteArrayOutputStream transformOutputStream = new ByteArrayOutputStream();
				
				TransformerFactory tFactory = TransformerFactory.newInstance();
				StreamSource xslSource = new StreamSource(xslt);
				StreamSource xmlSource = new StreamSource(doc.openStream());
				StreamResult outResult = new StreamResult(transformOutputStream);
							
				Transformer transformer = tFactory.newTransformer(xslSource);
				transformer.transform(xmlSource, outResult);
				transformOutputStream.close();
				
				ByteArrayInputStream modelInputStream = new ByteArrayInputStream(transformOutputStream.toByteArray());
				
				
				ValueFactory vf = new ValueFactoryImpl();
				RdfStoreConnection con = df.createRdfStoreConnection();
				RDFHandlerIterator iter = new RDFHandlerIterator(graphName);
				RDFParser parser = new RDFXMLParser();
				parser.setValueFactory(vf);
				parser.setRDFHandler(iter);
				parser.parse(modelInputStream, graphName.toString());
				
				while(iter.hasNext()){
					Statement st = iter.next();
					
					statements.add(con.addStatement(st.getSubject(), st.getPredicate(), st.getObject(), graphName));
				}
			} catch (RDFParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("XML2RDFTransformer:invokeInternal-end");
		
		ArrayList<Statement> l=new ArrayList<Statement>();
		l.add(new StatementImpl(new BNodeImpl(UUID.randomUUID()+""), RDFConstants.DEFAULTOUTPUTNAME, graphName));

		return new SetOfStatementsImpl(l); 
//		return df.createRdfGraph(statements, graphName);
	}
	
	
	
	public void shutdownInternal() {
	}
}