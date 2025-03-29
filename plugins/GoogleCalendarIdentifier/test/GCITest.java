import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.repository.RepositoryException;

import eu.larkc.core.LarkcTest;
import eu.larkc.core.data.InformationSet;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.IllegalWorkflowGraphException;
import eu.larkc.core.data.workflow.MultiplePluginParametersException;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.executor.Executor;
import eu.larkc.core.executor.ExecutorException;
import eu.larkc.core.query.SPARQLQueryImpl;


public class GCITest extends LarkcTest{

	public static String EVENTS_FOR_USER_LUKA = 
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
	"PREFIX cyc: <http://sw.opencyc.org/concept/>\n" + 
	"SELECT DISTINCT ?event where {\n" + 
	"?event rdf:type cyc:Event .\n" +  
	"?person rdf:type foaf:Person .\n" + 
	"?person foaf:name \"Luka Bradeško\" .\n" +
	"?person cyc:owns ?event .\n" +
	"}";
	
	
	/**
	 * Testing my Precious decider
	 * 
	 * @throws IllegalWorkflowGraphException
	 */
	@Test
	public void GoogleIdentifierTest()
			throws IllegalWorkflowGraphException {
		System.out.println("STARTING THE GCI TEST");
		
		
		BNode gci = new BNodeImpl("GCI");
		URI gciURI = new URIImpl("urn:GoogleCalendarIdentifier");
		Statement hasUri = new StatementImpl(gci, WorkflowDescriptionPredicates.RDF_TYPE, gciURI);
		
		BNode GCIParam1 =new BNodeImpl("GCIparam1");
		Statement paramsFileIdentifier=new StatementImpl(gci, WorkflowDescriptionPredicates.HAS_PARAMETERS, GCIParam1);
		Statement param0_0 = new StatementImpl(GCIParam1,new URIImpl("http://larkc.eu/plugin#loginInfo"), new LiteralImpl("Luka Bradesko,username,password"));

		// define a path
		BNode bPath = new BNodeImpl("Path");

		// define an input
		Statement pathHasInputStmt = new StatementImpl(bPath,
				WorkflowDescriptionPredicates.PATH_HAS_INPUT_URI,
				gci);

		// define an output
		Statement pathHasOutputStmt = new StatementImpl(bPath,
				WorkflowDescriptionPredicates.PATH_HAS_OUTPUT_URI,
				gci);
		

		ArrayList<Statement> stmtList = new ArrayList<Statement>();

		stmtList.add(hasUri);
		stmtList.add(paramsFileIdentifier);
		stmtList.add(param0_0);
		stmtList.add(pathHasInputStmt);
		stmtList.add(pathHasOutputStmt);
		

		SetOfStatements workflowDescription = new SetOfStatementsImpl(stmtList);

		Executor executor;

		try {
			executor = new Executor(workflowDescription);
			executor.execute(new SPARQLQueryImpl(EVENTS_FOR_USER_LUKA).toRDF());
			InformationSet nextResults = executor.getNextResults();
			Assert.assertNotNull(nextResults);
		} catch (ExecutorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
}
