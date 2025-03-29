package eu.larkc.plugin.sparqlkeyword;

import java.util.Arrays;
import java.util.Collection;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.LabelledGroupOfStatements;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;

public class IRSelecter extends BaseLineFTSelecter {
	public IRSelecter(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected Collection<SPARQLQuery> prepareQueries(Collection<String> terms, SetOfStatements sos) {
		StringBuilder sb = new StringBuilder();
		for (String term : terms) {
			sb.append(term).append(" ");
		}
		SPARQLQuery query = new SPARQLQueryImpl("SELECT ?uri WHERE { " +
				"?uri <http://www.ontotext.com/luceneQuery> \"" + sb + "\"}");
		
		return Arrays.asList(query);
	}

	private static void benchmark(IRSelecter s, String query, int maxMolecules) {
		Logger log = LoggerFactory.getLogger(IRSelecter.class);
		// configure plugin
		s.setMaxMolecules(maxMolecules);
		long t1 = System.currentTimeMillis();
		SetOfStatements sos = s.invokeInternal(DataFactory.INSTANCE.createSPARQLQuery(query).toRDF());
		long t2 = System.currentTimeMillis();
		log.info("*** " + ((LabelledGroupOfStatements)sos).getLabel());
		log.info("Executed in " + (t2-t1) + "ms. Found " + 
				s.getSelectedStatements() + " statements, " + 
				s.getSelectedMolecules() + " molecules.");
	}
	
	public static void main(String[] args) {
		// Instantiate the base-line full text searches selecter
		IRSelecter s = new IRSelecter(new URIImpl("http://larkc.eu/plugin#IRSelecter"));

		// Benchmark selection
		String query1 = "SELECT ?s ?p ?o WHERE { ?s ?p ?o FILTER(?s = 'london madrid and berlin are the largest european capitals') }";	
		benchmark(s, query1, 10);
		benchmark(s, query1, 1000);
		String query2 = "SELECT ?s ?p ?o WHERE { ?s ?p ?o FILTER(?s = 'london' )}";
		benchmark(s, query2, 10);
		benchmark(s, query2, 1000);
	}

}
