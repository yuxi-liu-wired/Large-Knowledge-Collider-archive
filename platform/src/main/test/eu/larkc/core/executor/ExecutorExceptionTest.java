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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.LarkcTest;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.shared.SampleWorkflows;

/**
 * Test the functionality of the executor when exceptions are thrown.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class ExecutorExceptionTest extends LarkcTest {

	private static Logger logger = LoggerFactory
			.getLogger(ExecutorExceptionTest.class);

	/**
	 * Executor test using a workflow description with cylces.
	 * 
	 * @throws ExecutorException
	 */
	@Test(expected = ExecutorException.class)
	public void executorTestUsingWorkflowDescriptionWithCycles()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getWorkflowDescriptionWithCycles();

		new Executor(workflowDescription);
	}

	/**
	 * Executor test using a workflow description with cylces.
	 * 
	 * @throws ExecutorException
	 */
	@Test(expected = ExecutorException.class)
	public void executorTestUsingIncorrectPluginTypes()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getWorkflowDescriptionWithIncorrectPluginTypes();

		new Executor(workflowDescription);
	}

	/**
	 * Executor test using a workflow description with cylces.
	 * 
	 * @throws ExecutorException
	 */
	@Test(expected = ExecutorException.class)
	public void executorTestUsingIncorrectEndpointTypes()
			throws ExecutorException {
		SetOfStatements workflowDescription = SampleWorkflows
				.getWorkflowDescriptionWithIncorrectEndpointTypes();

		new Executor(workflowDescription);
	}

}
