package eu.larkc.plugin.hadoop.filterplugin;

import java.io.IOException;

import javax.jms.Message;

import org.apache.hadoop.fs.Path;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.core.query.SPARQLQueryImpl;
import eu.larkc.plugin.Plugin;
import eu.larkc.plugin.hadoop.Constants;
import eu.larkc.plugin.hadoop.GenericHadoopPlugin;


/**
 * A hadoop plugin that will filter triples according to the given statement patterns
 * @author spyros
 */
public class FilterPlugin extends GenericHadoopPlugin {

	public static final String STATEMENTPATTERNS = "StatementPatterns";



	private static Logger logger = LoggerFactory.getLogger(Plugin.class);

	public FilterPlugin(URI pluginName) {
		super(pluginName);
		
	}


	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
			
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(input);
			
		preprocessQuery(input, attval);
		
		// Get the input
		String outP = attval.get(Constants.INPUT)+"-Filtered" + System.currentTimeMillis();
		Path outputPath=new Path(outP);
		
		Path inputPath= new Path(attval.get(Constants.INPUT));
		inputPath = ensurePathIsOnDFS(config, inputPath);
		
		String[] args=new String[]{inputPath.toString(), outputPath.toString(), attval.get(STATEMENTPATTERNS)};


		runTool(config, FilterJob.class.getCanonicalName() , args);
		
		// Return pointers to files
		AttributeValueMap r=new AttributeValueMap();
		r.put(Constants.INPUT, outputPath.toString());
		
		return r.toRDF();
		}
		catch (IOException e) {
			e.printStackTrace(); // TODO exception handling
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}


	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
	}

	private void preprocessQuery(SetOfStatements input, AttributeValueMap attVal) {
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);

		String serializedTriplePatterns="";
		
		if( query instanceof SPARQLQueryImpl)
		{
			StatementPatternCollector spc = new StatementPatternCollector();
			((SPARQLQueryImpl) query).getParsedQuery().getTupleExpr().visit(spc);
	
			for (StatementPattern sp : spc.getStatementPatterns()){
				Var s=sp.getSubjectVar();
				Var p=sp.getPredicateVar();
				Var o=sp.getObjectVar();

				if (!(s == null && p == null && o == null)){
					String ps=null;
					
					if (s.getValue()!=null) 
						serializedTriplePatterns+=putBrackets(s.getValue().toString());
					else
						serializedTriplePatterns+="_:-1";
					
					serializedTriplePatterns+=" ";
					if (p.getValue()!=null) 
						serializedTriplePatterns+=putBrackets(p.getValue().toString());
					else {
						ps=s.getName().equals(p.getName())?"_:-1":"_:-2";
						serializedTriplePatterns+=ps;
					}
					
					serializedTriplePatterns+=" ";
					if (o.getValue()!=null) 
						serializedTriplePatterns+=putBrackets(o.getValue().toString());
					else
						if (o.getName().equals(s.getName()))
							serializedTriplePatterns+="_:-1";
						else if (o.getName().equals(p.getName()))
							serializedTriplePatterns+=ps;
						else
							serializedTriplePatterns+="_:-3";
					serializedTriplePatterns+=" \n";
				}
			}
			if (!serializedTriplePatterns.isEmpty())
				serializedTriplePatterns=serializedTriplePatterns.substring(0, serializedTriplePatterns.length()-1);
		}
		else {
			throw new IllegalArgumentException("Do not know how to parse query " + query);
		}
		
		attVal.put(STATEMENTPATTERNS, serializedTriplePatterns);
	}
	
	private String putBrackets(String resource) {
		if (!resource.startsWith("\"") && !resource.startsWith("_")) {
			return "<" + resource + ">";
		}
		else
			return resource;
	}

	
}
