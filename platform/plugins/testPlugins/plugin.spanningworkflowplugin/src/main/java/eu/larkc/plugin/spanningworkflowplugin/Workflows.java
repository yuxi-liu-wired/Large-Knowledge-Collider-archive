package eu.larkc.plugin.spanningworkflowplugin;

import java.util.ArrayList;

import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;

public class Workflows {

	/**
	 * A simple, linear workflow consisting of three chained plugins in N3.
	 * 
	 * @return the workflow as a string in N3 format
	 */
	public static String getN3Workflow() {
		return "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
				+ "@prefix larkc: <http://larkc.eu/schema#> . \n"

				+ "# Define four plug-ins \n"
				+ "_:plugin1 a <urn:eu.larkc.plugin.identify.TestIdentifier> . \n"
				+ "_:plugin2 a <urn:eu.larkc.plugin.transform.TestTransformer> . \n"
				+ "_:plugin3 a <urn:eu.larkc.plugin.decide.TestDecider> . \n"

				+ "# Connect the plug-ins \n"
				+ "_:plugin1 larkc:connectsTo _:plugin2 . \n"
				+ "_:plugin2 larkc:connectsTo _:plugin3 . \n"

				+ "# Define a path to set the input and output of the workflow \n"
				+ "_:path a larkc:Path . \n"
				+ "_:path larkc:hasInput _:plugin1 . \n"
				+ "_:path larkc:hasOutput _:plugin3 . \n"

				+ "# Connect an endpoint to the path \n"
				+ "_:ep a <urn:eu.larkc.endpoint.test> . \n"
				+ "_:ep larkc:links _:path . \n";
	}

	/**
	 * Minimal workflow description.
	 * 
	 * @return a mini-workflow which is as minimal as possible.
	 */
	public static SetOfStatements getMinimalWorkflowDescription() {
		URI transformerURI = new URIImpl(
				"urn:eu.larkc.plugin.transform.TestTransformer");
		URI identifierURI = new URIImpl(
				"urn:eu.larkc.plugin.identify.TestIdentifier");

		BNode bTestTransformer1 = new BNodeImpl("TestTransformer1");
		BNode bTestIdentifier1 = new BNodeImpl("TestIdentifier1");

		Statement typeOfStmt1 = new StatementImpl(bTestTransformer1,
				WorkflowDescriptionPredicates.RDF_TYPE, transformerURI);
		Statement typeOfStmt2 = new StatementImpl(bTestIdentifier1,
				WorkflowDescriptionPredicates.RDF_TYPE, identifierURI);

		Statement connectsToStmt1 = new StatementImpl(bTestIdentifier1,
				WorkflowDescriptionPredicates.CONNECTS_TO_URI,
				bTestTransformer1);

		// define a path
		BNode bPath = new BNodeImpl("ExamplePath");

		// define an input
		Statement pathHasInputStmt = new StatementImpl(bPath,
				WorkflowDescriptionPredicates.PATH_HAS_INPUT_URI,
				bTestIdentifier1);

		// define an output
		Statement pathHasOutputStmt = new StatementImpl(bPath,
				WorkflowDescriptionPredicates.PATH_HAS_OUTPUT_URI,
				bTestTransformer1);

		ArrayList<Statement> stmtList = new ArrayList<Statement>();

		stmtList.add(typeOfStmt1);
		stmtList.add(typeOfStmt2);
		stmtList.add(connectsToStmt1);
		stmtList.add(pathHasInputStmt);
		stmtList.add(pathHasOutputStmt);

		SetOfStatements workflowDescription = new SetOfStatementsImpl(stmtList);

		return workflowDescription;
	}
}
