/*
 * Copyright (C) 2008, 2009 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.larkc.plugin;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.BooleanInformationSetImpl;
import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfStoreConnection;
import eu.larkc.core.data.SAILRdfStoreConnectionImpl;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SesameVariableBinding;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;

public class SparqlQueryEvaluationReasoner extends Plugin {
	
	protected static Logger logger = LoggerFactory.getLogger(SparqlQueryEvaluationReasoner.class);
	
	public SparqlQueryEvaluationReasoner(URI pluginName) {
		super(pluginName);
	}
	
	@Override
	public void shutdown() {
	}

	public URI getIdentifier() {
		return new URIImpl("urn:"+this.getClass().getName());
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection();
		if (con instanceof SAILRdfStoreConnectionImpl){
			if (((SAILRdfStoreConnectionImpl)con).getRepositoryClass() == "virtuoso.sesame2.driver.VirtuosoRepository") {
				CloseableIterator<Statement> i = input.getStatements();
				while (i.hasNext()) {
					Statement s = i.next();
					if (s.getPredicate().equals(RDFConstants.LARKC_HASSERIALIZEDFORM)) {
						SesameVariableBinding varbinding = new SesameVariableBinding();
						try {
							logger.debug("Got query: " + s.getObject().stringValue());
							SAILRdfStoreConnectionImpl.myRepository.getConnection().prepareTupleQuery(QueryLanguage.SPARQL,s.getObject().stringValue()).evaluate(varbinding);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return varbinding.toRDF(new SetOfStatementsImpl());
					}
				}
			}
		}
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
		if (query==null)
			throw new IllegalArgumentException("No query found for reasoner");
		
		Set<String> graphNames=new HashSet<String>(DataFactory.INSTANCE.extractObjectsForPredicate(input, RDFConstants.DEFAULTOUTPUTNAME));
		if (graphNames.isEmpty()){
			graphNames.add(RDFConstants.DEFAULTOUTPUTNAME.stringValue());
		}
		if (graphNames.size()!=1) {
			throw new IllegalArgumentException("SparqlQueryEvaluationReasoner can not take multiple labelled graphs as input: " +graphNames); //TODO merge graphs, if this happens
		}
		
		query.setLabelledGroup(new URIImpl(graphNames.iterator().next()));
		
		logger.debug("Got query: " + query.toString());
		
		if (query.isConstruct()) {
			return DataFactory.INSTANCE.createRdfStoreConnection().executeConstruct(query);
		}
		else if(query.isAsk()) {
			return new BooleanInformationSetImpl(DataFactory.INSTANCE.createRdfStoreConnection().executeAsk(query)).toRDF(new SetOfStatementsImpl());
		}
		else if(query.isDescribe()) {
			return DataFactory.INSTANCE.createRdfStoreConnection().executeConstruct(query);
		}
		else if(query.isSelect()) {
			logger.debug("Processing select query...");
			
			VariableBinding vb = DataFactory.INSTANCE.createRdfStoreConnection().executeSelect(query);
			logger.debug("Select finished. Now to format results (VariableBinding.toRDF(new SetOfStatementsImpl()).");
			SetOfStatements result=vb.toRDF(new SetOfStatementsImpl());
			logger.debug("Results ready");
			return result;
		}
		else {
			throw new IllegalStateException("Invalid query type for query " + query);
		}
	}


	
	/* (non-Javadoc)
	 * @see eu.larkc.plugin.Plugin#cacheInsert(eu.larkc.core.data.SetOfStatements, eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected void cacheInsert(SetOfStatements input, SetOfStatements output) {
		// Caching is disabled for this plugin
	}

	/* (non-Javadoc)
	 * @see eu.larkc.plugin.Plugin#cacheLookup(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected SetOfStatements cacheLookup(SetOfStatements input) {
		// Caching is disabled for this plugin
		return null;
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
	}
		

	@Override
	protected void shutdownInternal() {
	}	
}
