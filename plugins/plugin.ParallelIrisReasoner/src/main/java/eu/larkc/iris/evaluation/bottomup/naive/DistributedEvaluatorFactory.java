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
package eu.larkc.iris.evaluation.bottomup.naive;

import eu.larkc.iris.evaluation.bottomup.IDistributedRuleEvaluator;
import eu.larkc.iris.evaluation.bottomup.IDistributedRuleEvaluatorFactory;

/**
 * Factory to create distributed naive evaluators
 * Two types of evaluators are used the single-pass and naive. Single-pass is used for non-recursive rules and naive for recursive stratified rules.
 * 
 * @author florian.fischer@softgress.com
 *
 */
public class DistributedEvaluatorFactory implements IDistributedRuleEvaluatorFactory {

	
	@Override
	public IDistributedRuleEvaluator createEvaluator() {
		return new DistributedDependencyAwareEvaluator();
	}

	@Override
	public IDistributedRuleEvaluator createEvaluator(int evaluator) {
		switch (evaluator) {
		case SINGLEPASSEVALUATOR:
			return new DistributedOnePassEvaluator();			
		case RECURSIONAWAREEVALUATOR:
			return new DistributedDependencyAwareEvaluator();
		case NAIVEEVALAUTOR:
			return new DistributedNaiveEvaluator();
		default:
			return null;
		}
	}
}
