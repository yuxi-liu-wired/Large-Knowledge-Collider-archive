package eu.larkc.plugin.reasoner.keywordreasoner.utils;

import org.openrdf.model.impl.NumericLiteralImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.vu.few.krr.larkc.keywordreasoner.Keyword;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import org.apache.commons.io.FileUtils;


import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.SparqlWorkflowDescription;
import eu.larkc.plugin.reasoner.keywordreasoner.KRConstants;
import eu.larkc.shared.RDFParsingUtils;

public class KRUtils {
	
	/**
	 * Used for testing purposes only.
	 * */	
	public static SetOfStatements readStatements2(String filename){
		String input = readFile(new File(filename));
		try {
			Collection<Statement> parsedInput = RDFParsingUtils.parseN3(input);
			return new SetOfStatementsImpl(parsedInput);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new SetOfStatementsImpl();
	}
	
	/**
	 * Used for testing purposes only.
	 * */
	public static SparqlWorkflowDescription readWorkflowDefinition(String filename) throws Exception{
		URL workflowURL = KRUtils.class.getResource(filename);
		String workflow = FileUtils.readFileToString(new File(workflowURL.toURI()));
		Collection<Statement> parsedWorkflow = RDFParsingUtils.parseN3(workflow);
		SetOfStatements workflowDescription = new SetOfStatementsImpl(parsedWorkflow);		
		return new SparqlWorkflowDescription(workflowDescription);
	}
	
	private static String readFile(File file){
    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();
    try {
    	reader = new BufferedReader(new FileReader(file));
    	String line = "";
      while ((line = reader.readLine()) != null){
      	sb.append(line.trim());
      }
      reader.close();
      return sb.toString();
    } 
    catch (FileNotFoundException e) {e.printStackTrace();} 
    catch (IOException e) {e.printStackTrace();}
    return "";		
	}
	
	/**
	 * Create an RDF graph with the resulting set of keywords. The format of the graph is as follows:
	 * 
	 * _:k1 <urn:larkc.keywordreasoner.results.keyword.name> "the name" .
	 * _:k1 <urn:larkc.keywordreasoner.results.keyword.uri> "the uri" .
   * _:k1 <urn:larkc.keywordreasoner.results.keyword.cl> "the confidence value" .
	 * _:k1 <urn:larkc.keywordreasoner.results.keyword.sd> "the semantic distance" .
	 * 
	 * @param keywords
	 * 								the list of keywords to save in the RDF graph
	 * @param graphURI 
	 * 								the URI of the RDF graph that contains the keywords
	 * 
	 * @return an RDF graph with the triples enconding the list of keywords
	 * 
	 * @author Gaston Tagni <g.e.tagni@vu.nl>
	 * @version 1.0
	 * 
	 * */
	public static RdfGraph wrapKeywordsIntoSetOfStatements(Collection<Keyword> keywords, URI graphURI){
		List<Statement> listOfStatements = new ArrayList<Statement>();
		int i = 1;
		for (Keyword k : keywords){
			BNode bNode = new BNodeImpl("k"+i);
			URI predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_NAME);
			Literal literal = ValueFactoryImpl.getInstance().createLiteral(k.getValue());
			listOfStatements.add(new StatementImpl(bNode,predicate,literal));
			
			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_URI);
			literal = ValueFactoryImpl.getInstance().createLiteral(k.getAssociatedConceptIRI().toString());
			listOfStatements.add(new StatementImpl(bNode,predicate,literal));

			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_CL);
	//		literal = ValueFactoryImpl.getInstance().createLiteral(k.getConfidenceLevel());
			listOfStatements.add(new StatementImpl(bNode,predicate,literal));
			
			listOfStatements.add(new StatementImpl(bNode,predicate,new NumericLiteralImpl(Double.parseDouble(literal.stringValue()))));

			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_SD);
			literal = ValueFactoryImpl.getInstance().createLiteral(k.getSemanticDistance());
			listOfStatements.add(new StatementImpl(bNode,predicate,literal));
	//		listOfStatements.add(new StatementImpl(bNode,predicate,new NumericLiteralImpl(Double.parseDouble(literal.stringValue()))));

			i++;
		}
		RdfGraph graph = DataFactory.INSTANCE.createRdfGraph(listOfStatements,graphURI);
		return graph;
	}	

	public static SetOfStatements wrapKeywordsIntoSetOfStatements2(Collection<Keyword> keywords, URI graphURI){
		
		 final Set<Statement> statements = new HashSet<Statement>();
		 
		int i = 1;
		for (Keyword k : keywords){
//			BNode bNode = new BNodeImpl("k"+i);
			URI bNode = new URIImpl(graphURI.toString()+"#k"+i);
			URI predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_NAME);
			Literal literal = ValueFactoryImpl.getInstance().createLiteral(k.getValue());
			statements.add(new StatementImpl(bNode,predicate,literal));
			
			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_URI);
			literal = ValueFactoryImpl.getInstance().createLiteral(k.getAssociatedConceptIRI().toString());
			statements.add(new StatementImpl(bNode,predicate,literal));

			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_CL);
			literal = ValueFactoryImpl.getInstance().createLiteral(k.getConfidenceLevel());
	//		listOfStatements.add(new StatementImpl(bNode,predicate,literal));
			
		statements.add(new StatementImpl(bNode,predicate,new NumericLiteralImpl(Double.parseDouble(literal.stringValue()))));

			predicate = new URIImpl(KRConstants.RESULTS_KEYWORD_SD);
	       literal = ValueFactoryImpl.getInstance().createLiteral(k.getSemanticDistance());
	//		listOfStatements.add(new StatementImpl(bNode,predicate,literal));
	     statements.add(new StatementImpl(bNode,predicate,new NumericLiteralImpl(Double.parseDouble(literal.stringValue()))));

			i++;
		}
		
		SetOfStatements result =new SetOfStatementsImpl(statements);
		
		
		//RdfGraph graph = DataFactory.INSTANCE.createRdfGraph(listOfStatements,graphURI);
		return  result;
	}	
	/**
	 * This method reads the initial set of keywords from the given file.  
	 * 
	 * @param url 
	 * 					the URL of the file that contains the set of kwywords to be read
	 * @return List<String> 
	 * 										the list of keywords. If the file can not be read an empty list is returned.
	 * 
	 * @author Gaston Tagni <g.e.tagni@vu.nl>
	 * @version 1.0
	 * */
	public static List<String> readKeywordsFile(URL url){
		ArrayList<String> list = new ArrayList<String>();	
    try {	
    	URLConnection connection = url.openConnection(); 
    	connection.setUseCaches(false);
    	BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
    	String line = "";
    	while ((line = reader.readLine()) != null){
    		list.add(line.trim());
    	}
    	reader.close();
      return list;
    }  
    catch (IOException e) {e.printStackTrace();}
    return list;		
	}
	
	
	
	public static String convertSetOfStatementsIntoPattern(SetOfStatements statements)
	{
		//state keyword group 
		String keywordGroup = "\n gwas:x gwas:hasKeywordGroup gwas:g2 .";
		
		//initialize  pattern
		String result = keywordGroup + "\n";
		
		
		CloseableIterator<Statement> vit = statements.getStatements();
		
		while (vit.hasNext()) {
			
			
			Statement smt = vit.next();
			Resource subject = smt.getSubject();
			URI predicate = smt.getPredicate();
			Value object = smt.getObject();
			
			result = result + " " + subject.toString() + " " + predicate.toString() + " " + object.toString() +" .\n";
		}
		
		return result;
	}
	
	public static SetOfStatements convertKeywordsIntoSetOfStatements(Collection<Keyword> keywords)
	{
		
	//URI subject = new URIImpl("gwas:g2");
	//URI predicate = new URIImpl("gwas:hasKeyword");
	
	return  convertKeywordsIntoSetOfStatements(keywords, "gwas:g2", "gwas:hasKeyword");
	
	}
	
	public static SetOfStatements convertKeywordsIntoSetOfStatements(Collection<Keyword> keywords, String s, String p)
	{
		
//		BNode bNode = new BNodeImpl("");
		URI subject = new URIImpl(s);
		URI predicate = new URIImpl(p);
//		Literal literal = ValueFactoryImpl.getInstance().createLiteral("value here");
//		Statement stmt = new StatementImpl(bNode, predicate, literal);
//		Statement stmt2 = new StatementImpl(subject, predicate, literal);
		
		
		ArrayList<Statement> l=new ArrayList<Statement>();
		

		Iterator<Keyword> vit = keywords.iterator();
		

		
		while (vit.hasNext()) {
			
		
			String keyword = vit.next().getValue();	
			
            // remove the heading of the keyword
		     String name = keyword.substring(keyword.lastIndexOf('#')+1, keyword.length());
			
			Literal literal = ValueFactoryImpl.getInstance().createLiteral(name);
			
	//		System.out.println(keyword.getValue());
			
			Statement stmt = new StatementImpl(subject, predicate, literal);
			
			l.add(stmt);

		}
		
		SetOfStatements result = new SetOfStatementsImpl(l);
		
		return result;
	}

}
