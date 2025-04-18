/*
 * Copyright 2010 Softgress - http://www.softgress.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.larkc.iris.rules.compiler;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.facts.IFacts;
import org.deri.iris.storage.IRelation;

import eu.larkc.iris.evaluation.EvaluationContext;

/**
 * Represents a compiled rule in a distributed environment
 * 
 * @author valer.roman@softgress.com
 *
 */
public interface IDistributedCompiledRule {

	
	/**
	 * Evaluate rule with all known facts. 
	 * Does not return information about whether new information was derived.
	 * 
	 * @param ruleNumber
	 */
	void evaluate(Integer ruleNumber) throws EvaluationException;
	
	/**
	 * Evaluate rule with all known facts.
	 * @return The result relation for this rule.
	 * @throws EvaluationException 
	 */
	boolean evaluate(EvaluationContext evaluationContext) throws EvaluationException;

	/**
	 * Evaluate the rule using deltas (see semi-naive evaluation) to more intelligently seek out
	 * tuples that have not already been computed.
	 * @param deltas The collection of recently discovered facts.
	 * @return The result relation for this rule.
	 * @throws EvaluationException 
	 */
	IRelation evaluateIteratively( IFacts deltas ) throws EvaluationException;
	
	public FlowAssembly getFlowAssembly();
	
	public IRule getRule();
	
}
