/*
   This file is part of the LarKC platform 
   http://www.larkc.eu/

   Copyright 2010 LarKC project consortium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.larkc.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.LarkcTest;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.shared.RDFParsingUtils;
import eu.larkc.shared.SampleN3Workflows;
import eu.larkc.shared.SampleQueries;
import eu.larkc.shared.SampleWorkflows;

/**
 * Test the functionality of the executor with distributed plugins.
 * 
 * @author Christoph Fuchs, Norbert Lanzanasto
 * 
 */
public class ExecutorDistributedTest extends LarkcTest {

	private static Logger logger = LoggerFactory
			.getLogger(ExecutorDistributedTest.class);

	/**
	 * Executor test using a workflow description with deployment description.
	 * 
	 * @throws ExecutorException
	 */
	// @Ignore
	@Test
	public void executorTestUsingWorkflowDescriptionWithDeploymentDescription()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithDeploymentDescription();

		Executor executor = new Executor(workflowDescription);
		executor.execute(new SPARQLQueryImpl(SampleQueries.WHO_KNOWS_FRANK)
				.toRDF());
		SetOfStatements nextResults = executor.getNextResults();

		Collection<Statement> actualStatements = new ArrayList<Statement>();
		CloseableIterator<Statement> it = nextResults.getStatements();
		while (it.hasNext()) {
			actualStatements.add(it.next());
		}
		it.close();

		// Construct the expected result
		Collection<Statement> expectedStatements = new ArrayList<Statement>();
		BNode bnode = new BNodeImpl("blank");

		Statement stmt1 = new StatementImpl(bnode, RDFConstants.RDF_TYPE,
				RDFConstants.LARKC_SPARQLQUERY);
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);

		Assert.assertEquals(expectedStatements.size(), actualStatements.size());

		boolean found = false;
		Statement expectedStmt = null;
		for (Statement actualStmt : actualStatements) {
			for (Statement stmt : expectedStatements) {
				expectedStmt = stmt;
				if (actualStmt.getPredicate().stringValue()
						.equals(expectedStmt.getPredicate().stringValue())) {
					Assert.assertEquals(expectedStmt.getObject().stringValue(),
							actualStmt.getObject().stringValue());
					found = true;
					break;
				}
			}
			expectedStatements.remove(expectedStmt);
			if (!found)
				Assert.fail("Statement not present in expected statements!");
			found = false;
		}
	}

	/**
	 * Executor test using a workflow description with deployment description
	 * located in a file.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	// @Ignore
	@Test
	public void executorTestForDistributedWorkflowForGAT()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {

		// Get and parse workflow description
		String workflow = SampleN3Workflows.getDistributedWorkflowForGAT();
		Collection<Statement> col = RDFParsingUtils.parseN3(workflow);

		// Retrieve statements
		Iterator<Statement> iter = col.iterator();
		List<Statement> stmnts = new ArrayList<Statement>();
		while (iter.hasNext()) {
			stmnts.add(iter.next());
		}
		SetOfStatements workflowDescription = new SetOfStatementsImpl(stmnts);

		// Set up executor
		Executor executor = new Executor(workflowDescription);
		executor.execute(new SPARQLQueryImpl(SampleQueries.WHO_KNOWS_FRANK)
				.toRDF());
		SetOfStatements nextResults = executor.getNextResults();

		Collection<Statement> actualStatements = new ArrayList<Statement>();
		CloseableIterator<Statement> it = nextResults.getStatements();
		Statement next;
		while (it.hasNext()) {
			next = it.next();
			logger.debug("Result statement: {}", next);
			actualStatements.add(next);
		}
		it.close();

		// Construct the expected result
		Collection<Statement> expectedStatements = new ArrayList<Statement>();
		BNode bnode = new BNodeImpl("blank");

		Statement stmt1 = new StatementImpl(bnode, RDFConstants.RDF_TYPE,
				RDFConstants.LARKC_SPARQLQUERY);
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);

		Assert.assertEquals(expectedStatements.size(), actualStatements.size());

		boolean found = false;
		Statement expectedStmt = null;
		for (Statement actualStmt : actualStatements) {
			for (Statement stmt : expectedStatements) {
				expectedStmt = stmt;
				if (actualStmt.getPredicate().stringValue()
						.equals(expectedStmt.getPredicate().stringValue())) {
					Assert.assertEquals(expectedStmt.getObject().stringValue(),
							actualStmt.getObject().stringValue());
					found = true;
					break;
				}
			}
			expectedStatements.remove(expectedStmt);
			if (!found)
				Assert.fail("Statement not present in expected statements!");
			found = false;
		}
	}

	/**
	 * Executor test using a workflow description with deployment description
	 * located in a file.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	// @Ignore
	@Test
	public void executorTestForDistributedWorkflowForJEE()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {

		// Get and parse workflow description
		String workflow = SampleN3Workflows.getDistributedWorkflowForJEE();
		Collection<Statement> col = RDFParsingUtils.parseN3(workflow);

		// Retrieve statements
		Iterator<Statement> iter = col.iterator();
		List<Statement> stmnts = new ArrayList<Statement>();
		while (iter.hasNext()) {
			stmnts.add(iter.next());
		}
		SetOfStatements workflowDescription = new SetOfStatementsImpl(stmnts);

		// Set up executor
		Executor executor = new Executor(workflowDescription);
		executor.execute(new SPARQLQueryImpl(SampleQueries.WHO_KNOWS_FRANK)
				.toRDF());
		SetOfStatements nextResults = executor.getNextResults();

		Collection<Statement> actualStatements = new ArrayList<Statement>();
		CloseableIterator<Statement> it = nextResults.getStatements();
		while (it.hasNext()) {
			actualStatements.add(it.next());
		}
		it.close();

		// Construct the expected result
		Collection<Statement> expectedStatements = new ArrayList<Statement>();
		BNode bnode = new BNodeImpl("blank");

		Statement stmt1 = new StatementImpl(bnode, RDFConstants.RDF_TYPE,
				RDFConstants.LARKC_SPARQLQUERY);
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);

		Assert.assertEquals(expectedStatements.size(), actualStatements.size());

		boolean found = false;
		Statement expectedStmt = null;
		for (Statement actualStmt : actualStatements) {
			for (Statement stmt : expectedStatements) {
				expectedStmt = stmt;
				if (actualStmt.getPredicate().stringValue()
						.equals(expectedStmt.getPredicate().stringValue())) {
					Assert.assertEquals(expectedStmt.getObject().stringValue(),
							actualStmt.getObject().stringValue());
					found = true;
					break;
				}
			}
			expectedStatements.remove(expectedStmt);
			if (!found)
				Assert.fail("Statement not present in expected statements!");
			found = false;
		}
	}

	/**
	 * Executor test using a workflow description with deployment description
	 * described directly in the workflow.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	// @Ignore
	@Test
	public void executorTestForDistributedWorkflowForGatwithEncodedHost()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {

		// Get and parse workflow description
		String workflow = SampleN3Workflows
				.getMinimalDistributedWorkflowFromD533WithHostDescription();
		Collection<Statement> col = RDFParsingUtils.parseN3(workflow);

		// Retrieve statements
		Iterator<Statement> iter = col.iterator();
		List<Statement> stmnts = new ArrayList<Statement>();
		while (iter.hasNext()) {
			stmnts.add(iter.next());
		}
		SetOfStatements workflowDescription = new SetOfStatementsImpl(stmnts);

		// Set up executor
		Executor executor = new Executor(workflowDescription);
		executor.execute(new SPARQLQueryImpl(SampleQueries.WHO_KNOWS_FRANK)
				.toRDF());
		SetOfStatements nextResults = executor.getNextResults();

		Collection<Statement> actualStatements = new ArrayList<Statement>();
		CloseableIterator<Statement> it = nextResults.getStatements();
		while (it.hasNext()) {
			actualStatements.add(it.next());
		}
		it.close();

		// Construct the expected result
		Collection<Statement> expectedStatements = new ArrayList<Statement>();
		BNode bnode = new BNodeImpl("blank");

		Statement stmt1 = new StatementImpl(bnode, RDFConstants.RDF_TYPE,
				RDFConstants.LARKC_SPARQLQUERY);
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);

		Assert.assertEquals(expectedStatements.size(), actualStatements.size());

		boolean found = false;
		Statement expectedStmt = null;
		for (Statement actualStmt : actualStatements) {
			for (Statement stmt : expectedStatements) {
				expectedStmt = stmt;
				if (actualStmt.getPredicate().stringValue()
						.equals(expectedStmt.getPredicate().stringValue())) {
					Assert.assertEquals(expectedStmt.getObject().stringValue(),
							actualStmt.getObject().stringValue());
					found = true;
					break;
				}
			}
			expectedStatements.remove(expectedStmt);
			if (!found)
				Assert.fail("Statement not present in expected statements!");
			found = false;
		}
	}

}
