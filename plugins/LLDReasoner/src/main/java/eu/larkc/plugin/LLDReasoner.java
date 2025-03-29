//FIXME: Include licensing header
package eu.larkc.plugin;

import java.util.concurrent.CountDownLatch;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.DataFactoryImpl;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.qos.QoSInformation;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;

public class LLDReasoner extends Plugin {

	public LLDReasoner() {
	    super(new URIImpl("urn:LLDReasoner"));
	}
			
	public LLDReasoner(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	private static Logger logger = LoggerFactory.getLogger(LLDReasoner.class);

	private synchronized BindingSet getNextBinding(TupleQueryResult tqr) {
		try {
			if (!tqr.hasNext()) return null;
			return tqr.next();
		}
		catch (Exception e) {
			return null;
		}
	}

	public SetOfStatements select(String sq) {
	
		logger.debug("Hello from LLD Reasoner");
		logger.debug("The query is" + sq);
		
		final SetOfStatementsImpl result = new SetOfStatementsImpl();
		
		System.setProperty("bk.endpoint", "http://linkedlifedata.com/");
		System.setProperty("bk.endpoint.username", "wp7b");
		System.setProperty("bk.endpoint.password", "wp7b");
		System.setProperty("repository.id", "owlim");
		
		try {

			Repository myRepository = new HTTPRepository(
					"http://linkedlifedata.com/", "owlim");
			String username = System.getProperty("bk.endpoint.username");
			String password = System.getProperty("bk.endpoint.password");
			
			logger.debug("Hello from LLD Reasoner 2");
			
			if (username != null && password != null) {
				((HTTPRepository) myRepository).setUsernameAndPassword(
						username, password);
			}
			RepositoryConnection connection = myRepository.getConnection();
			connection.setAutoCommit(false);

			logger.debug("Requesting tupels from LLD...");
			final TupleQueryResult tqResult = connection.prepareTupleQuery(
					QueryLanguage.SPARQL, sq).evaluate();

			try {
				BindingSet bindings = getNextBinding(tqResult);
				while (bindings != null) {
					try {

						result.getData().add(new StatementImpl((Resource)bindings.getValue("s"), (URI)bindings.getValue("p"), bindings.getValue("o")));
      					bindings = getNextBinding(tqResult);

					}
					catch (Exception e) {
						logger.debug("Exception: {}", e.getMessage());
					}
				}
			}
			catch (Exception e) {
				logger.debug("Exception: {}", e.getMessage());
			}
			
			logger.debug("Found "+result.getData().size()+" statements");

			CloseableIterator<Statement> it = result.getStatements();
			int count = 0;

			while (it.hasNext())
                        {
                                count++;
                                Statement stat = it.next();
                                
                                if(stat != null)
                                        logger.debug(count +" "+ stat.toString());

				if (count > 10) 
					break;
                        }

			tqResult.close();
			connection.close();

		} catch (Exception e) {
			logger.debug("Exception: {}", e.getMessage());
		}

		return result;
	}

	public URI getIdentifier() {
		return new URIImpl("urn:" + this.getClass().getName());
	}

	public QoSInformation getQoSInformation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//For internal testing purposes only
	public static void main(String[] args) {
		LLDReasoner selector = new LLDReasoner(new URIImpl("urn:eu.larkc.plugin.RISelector"));
	
		String sparqlQuery = "SELECT ?s ?p ?o WHERE { { ?s ?p ?o . ?s ?p \"asthma\"} }";
		/*"SELECT ?s ?p ?o " +
				"WHERE { { ?s ?p ?o . ?s ?p \"trauma\"}  " +
						"UNION {?s ?p ?o . ?s ?p 'MYOCARDIUM' } " +
						"UNION {?s ?p ?o . ?s ?p 'APRAXIAS' } " +
						"UNION {?s ?p ?o . ?s ?p 'level;' } " +
						"UNION {?s ?p ?o . ?s ?p 'superoxide' } " +
						"UNION {?s ?p ?o . ?s ?p 'envenomations' } }";
		*/
		
		SPARQLQuery inputQuery = DataFactoryImpl.INSTANCE.createSPARQLQuery(sparqlQuery);
		SetOfStatements output = selector.invoke(inputQuery.toRDF());
	
		if(output != null)
		{
			CloseableIterator<Statement> it = output.getStatements();
			
			while (it.hasNext())
				logger.info(it.next().toString());
		}
		
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// TODO Auto-generated method stub
		CloseableIterator<Statement> it = input.getStatements();
		Statement st = it.next();
		String inputQuery = st.getObject().toString(); 
		
		String sparqlQuery = "SELECT ?s ?p ?o WHERE { { ?s ?p ?o . ?s ?p 'asthma'} }";
		
		return select(inputQuery.split("\"")[1].toString());
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
}
