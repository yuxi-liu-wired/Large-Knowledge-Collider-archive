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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.facts.IFacts;
import org.deri.iris.storage.IRelation;

import eu.larkc.iris.evaluation.EvaluationContext;

/**
 * CascadingCompiledRule encapsulates a rule that has been translated from the
 * IRIS internal representation to a suitable cascading workflow. This class
 * delegates the basic logic for a map a map-reduce computation in an internal
 * FlowAssembly object.
 * 
 * @history Oct 3, 2010, fisf, creation
 * @author Florian Fischer
 */
public class CascadingCompiledRule implements IDistributedCompiledRule {

	public CascadingCompiledRule(IRule rule, FlowAssembly flowAssembly,
			eu.larkc.iris.Configuration configuration) {
		this.rule = rule;
		this.flowAssembly = flowAssembly;
		this.mConfiguration = configuration;
	}

	@Override
	public void evaluate(Integer ruleNumber) throws EvaluationException {

		if (flowAssembly == null) {
			throw new IllegalArgumentException("Flow assembly must not be null");
		}

		throw new RuntimeException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.iris.rules.compiler.IDistributedCompiledRule#evaluate(eu.larkc
	 * .iris.evaluation.EvaluationContext)
	 */
	@Override
	public boolean evaluate(EvaluationContext evaluationContext)
			throws EvaluationException {

		// start returns immediately
		if (flowAssembly == null) {
			throw new IllegalArgumentException("Flow assembly must not be null");
		}

		return flowAssembly.evaluate(evaluationContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.iris.rules.compiler.IDistributedCompiledRule#evaluateIteratively
	 * (org.deri.iris.facts.IFacts)
	 */
	@Override
	public IRelation evaluateIteratively(IFacts deltas)
			throws EvaluationException {

		throw new NotImplementedException(
				"Semi-naive evaluation is not supported.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.iris.rules.compiler.IDistributedCompiledRule#getFlowAssembly()
	 */
	public FlowAssembly getFlowAssembly() {
		return flowAssembly;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.larkc.iris.rules.compiler.IDistributedCompiledRule#getRule()
	 */
	@Override
	public IRule getRule() {
		return rule;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof CascadingCompiledRule == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final CascadingCompiledRule otherObject = (CascadingCompiledRule) obj;

		return new EqualsBuilder().append(this.rule, otherObject.getRule())
				.isEquals();

	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(rule.toString()).toHashCode();
	}

	@Override
	public String toString() {
		return rule.toString();
	}

	/**
	 * The internal IRIS configuration
	 */
	@SuppressWarnings("unused")
	private final eu.larkc.iris.Configuration mConfiguration;

	/**
	 * The internal IRIS
	 */
	private IRule rule;

	/**
	 * The internal representation as a cascading flow of this rule.
	 */
	private final FlowAssembly flowAssembly;

}