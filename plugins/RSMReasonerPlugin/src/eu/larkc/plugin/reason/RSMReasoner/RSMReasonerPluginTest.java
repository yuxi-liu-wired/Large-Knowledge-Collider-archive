package eu.larkc.plugin.reason.RSMReasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.query.SPARQLQuery;



public class RSMReasonerPluginTest {
	public static void main(String[] args) {
		 
		
		String nameSpaces = "" +
		"PREFIX rsm: <http://www.saltlux.com/rsm#> \n" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n" +
		"PREFIX g2r: <http://www.saltlux.com/geo/functions#> \n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n" +	
		"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n" +
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
		"";
		
		String query = "" +
		nameSpaces +
		"select distinct  \n" +
		" ?id ?long ?lat  \n" + 
		" where {  \n" +
		" ?id rdf:type rsm:RoadSignElement .  \n" +
		" ?id geo:long ?long .  \n" +
		" ?id geo:lat ?lat . \n " +
		"}" +	
		"";		
		query = query.trim();
		
		
		
	   URI pluginURI = new URIImpl(KRConstants.PLUGIN_URI);
	
		
		RSMReasonerPlugin re = new RSMReasonerPlugin(pluginURI);
		
		SPARQLQuery thequery = DataFactory.INSTANCE.createSPARQLQuery(query);
		
		SetOfStatements input =thequery.toRDF(null);
		
		SetOfStatements answer = re.invokeInternal(input);
		
		printVariableBinding(answer);
		
	}
	
	private static void printVariableBinding(SetOfStatements data)
	{
		VariableBinding  vb = DataFactory.INSTANCE.createVariableBinding(data);
		
		Iterator<VariableBinding.Binding> vit = vb.iterator();
        

		while (vit.hasNext()) {

			Binding bin = vit.next();
			System.out.println(bin.getValues().toString());

		}

		
	}
	
}
