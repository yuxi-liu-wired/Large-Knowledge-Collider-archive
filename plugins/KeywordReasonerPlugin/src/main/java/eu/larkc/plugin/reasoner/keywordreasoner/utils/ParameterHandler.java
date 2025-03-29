package eu.larkc.plugin.reasoner.keywordreasoner.utils;

import java.util.List;
import java.util.Vector;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;

public class ParameterHandler {
	
	public static Value getParameter(String parameter, SetOfStatements triples){
		CloseableIterator<Statement> statements = triples.getStatements();
		while (statements.hasNext()){
			Statement st = statements.next();
			if (st.getPredicate().toString().compareToIgnoreCase(parameter) == 0){
				return st.getObject();
			}
		}
		return null;
	}
	
	public static List<Value> getParameters(String parameter, SetOfStatements triples){
		CloseableIterator<Statement> statements = triples.getStatements();
		Vector<Value> values = new Vector<Value>();
		while (statements.hasNext()){
			Statement st = statements.next();
			if (st.getPredicate().toString().compareToIgnoreCase(parameter) == 0){
				values.add(st.getObject());
			}
		}
		return values;		
	}
}