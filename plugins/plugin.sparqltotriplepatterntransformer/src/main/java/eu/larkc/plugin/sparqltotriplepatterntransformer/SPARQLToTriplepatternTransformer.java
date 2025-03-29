package eu.larkc.plugin.sparqltotriplepatterntransformer;

import java.util.ArrayList;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.core.query.TriplePattern;
import eu.larkc.core.query.TriplePatternQuery;
import eu.larkc.core.query.TriplePatternQueryImpl;
import eu.larkc.core.util.RDFConstants;
import eu.larkc.plugin.Plugin;
import eu.larkc.shared.SampleQueries;


public class SPARQLToTriplepatternTransformer extends Plugin {

	public static final String STATEMENTPATTERNS = "StatementPatterns";
	
	protected URI outputGraph;
	
	
	public SPARQLToTriplepatternTransformer(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		
		logger.debug("Input: "+input.getStatements().toString());
		
		// Does not care about the input name since it has a single argument, use any named graph
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);

		TriplePatternQuery tpq = new TriplePatternQueryImpl(new ArrayList<TriplePattern>());
		
		if( query instanceof SPARQLQueryImpl)
		{
			StatementPatternCollector spc = new StatementPatternCollector();
			((SPARQLQueryImpl) query).getParsedQuery().getTupleExpr().visit(spc);
	
			for (StatementPattern sp : spc.getStatementPatterns()){
				Resource s = (Resource) sp.getSubjectVar().getValue();
				URI p = (URI) sp.getPredicateVar().getValue();
				Value o = (Value) sp.getObjectVar().getValue();
				
				if (!(s == null && p == null && o == null)){
					
					if (s==null) 
						s=new URIImpl(RDFConstants.LARKC_NAMESPACE+ "var1");
					
					if (p==null)
						p=(URI) (sp.getSubjectVar().equals(sp.getPredicateVar())?s:new URIImpl(RDFConstants.LARKC_NAMESPACE+ "var2"));
					
					if (o==null)
						if (sp.getSubjectVar().equals(sp.getObjectVar()))
							o=s;
						else if (sp.getPredicateVar().equals(sp.getObjectVar()))
							o=p;
						else
							o=new URIImpl(RDFConstants.LARKC_NAMESPACE+ "var3");					
					
					tpq.add(new TriplePattern(s, p, o));
				}
			}
		}
		
		return DataFactory.INSTANCE.setNamedGraph(tpq.toRDF(), outputGraph);
	}

	@Override
	public void shutdown() {
		// no cleanup
	}
	
	/**
	 * For testing
	 * @param args
	 */
	public static void main(String args[]) {
		SetOfStatements s=
				new SPARQLToTriplepatternTransformer(
						new URIImpl("http://a")).invokeInternal(new SPARQLQueryImpl(SampleQueries.WHO_KNOWS_FRANK).toRDF());
		System.out.println(s);
	}


	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		outputGraph=getNamedGraphFromParameters(workflowDescription, RDFConstants.DEFAULTOUTPUTNAME);
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
}
