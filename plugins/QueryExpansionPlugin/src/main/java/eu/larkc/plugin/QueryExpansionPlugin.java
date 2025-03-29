//FIXME: Include licensing header
package eu.larkc.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.qos.QoSInformation;
import eu.larkc.core.query.SPARQLQuery;

public class QueryExpansionPlugin extends Plugin {

	public QueryExpansionPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	private static Logger logger = LoggerFactory.getLogger(QueryExpansionPlugin.class);

	private SetOfStatements transformQuery(SetOfStatements input) {

		if(input != null) {
			
			SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
			
			if(query == null) {
				return null;
			}
			
			Set<String> similarWords = new HashSet<String>();
			
			CloseableIterator<Statement> it = input.getStatements();
			
			while (it.hasNext()) {	
				Statement stmt = it.next();
				
				if(stmt != null) {
					if(logger.isDebugEnabled())
						logger.debug(stmt.toString());
					
					similarWords.add(stmt.getObject().stringValue());
				}
			}
			
			SparqlGenerator sparqlGenerator = new SparqlGenerator();
						
			List<SPARQLQuery> queries = sparqlGenerator.generateSparqlQueries(query.toString(), similarWords);
				
			if(logger.isDebugEnabled())
				logger.debug("Nr of refactored queries: "+queries.size());
						
			if(queries.size() == 1) {
				return ((SPARQLQuery)queries.get(0)).toRDF();
			}
			else {
				//TODO: Throw exception
			}
		}
		
		return null;
	}

	public URI getIdentifier() {
		return new URIImpl("urn:" + this.getClass().getName());
	}

	public QoSInformation getQoSInformation() {
		return new QoSInformation() {};
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		//TODO
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// TODO Auto-generated method stub
		return transformQuery(input);
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
}
