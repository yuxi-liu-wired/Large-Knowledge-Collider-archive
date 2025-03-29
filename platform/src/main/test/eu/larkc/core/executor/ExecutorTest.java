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
 * Test the functionality of the executor.
 * 
 * @author Christoph Fuchs, Norbert Lanzanasto
 * 
 */
public class ExecutorTest extends LarkcTest {

	private static Logger logger = LoggerFactory.getLogger(ExecutorTest.class);

	/**
	 * Executor test using a workflow description (same testing as in the
	 * cheatingExecutorTestWithDecider):
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescription() throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescription();

		Executor executor;

		executor = new Executor(workflowDescription);
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
	 * Executor test using a workflow description without prefixes.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescriptionWithoutPrefixes()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {
		// Get and parse workflow description
		String workflow = SampleN3Workflows
				.getMinimalN3WorkflowFromD533WithoutPrefixes();
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
		logger.debug("Expected statment: {}", stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		logger.debug("Expected statment: {}", stmt2);
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
	 * Executor test using a workflow description with splits and merges
	 * (diamond).
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescriptionWithSplitsAndMerges()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getWorkflowDescriptionWithSplitsAndMerges();

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
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);
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
	 * Executor test using a workflow description where parameters for the
	 * plugins are defined.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingPluginParameters() throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithParameters();

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
	 * Executor test using a workflow description where multiple parameters for
	 * the plugins are defined.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingMultiplePluginParameters()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithMultipleParametersPerPlugin();

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
	 * Executor test using a workflow description with source and sink.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescriptionWithInputAndOutput()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithPath();

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
	 * Executor test using a workflow description with plugin without input.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescriptionWithPluginWithoutInput()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithPluginWithoutInput();

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
	 * Executor test using a workflow description with five plugins and
	 * different merge behaviors.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingWorkflowDescriptionWithWaitForTwoMergeBehavior()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getSimpleWorkflowDescriptionWithWaitForTwoMergeBehavior();

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

		boolean check = false;

		if (actualStatements.size() == 4 || actualStatements.size() == 6)
			check = true;

		Assert.assertTrue(check);
	}

	/**
	 * Executor test using the minimalistic workflow from D533.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingSimpleCorrectWorkflowDescription()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getMinimalWorkflowFromD533();

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
	 * Executor test using the minimalistic workflow from D533 as a string.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingMinimalWorkflowFromD533()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {

		// Get and parse workflow description
		String workflow = SampleN3Workflows.getMinimalN3WorkflowFromD533();
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
	 * Executor test using the more complex (split&merge) workflow from D533 as
	 * a string.
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestUsingSplitMergeWorkflowFromD533()
			throws RDFParseException, RDFHandlerException, IOException,
			ExecutorException {

		// Get and parse workflow description
		String workflow = SampleN3Workflows.getSplitMergeWorkflowFromD533();
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
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);
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
	 * Executor test using a workflow description with one path with multiple
	 * input plugins.
	 * 
	 * @throws ExecutorException
	 */
	@Test
	public void executorTestWithMultipleInputs() throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getWorkflowDescriptionWithTwoInputs();

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
		expectedStatements.add(stmt1);
		StatementImpl stmt2 = new StatementImpl(bnode,
				RDFConstants.LARKC_HASSERIALIZEDFORM, new LiteralImpl(
						SampleQueries.WHO_KNOWS_FRANK.toString()));
		expectedStatements.add(stmt2);
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
