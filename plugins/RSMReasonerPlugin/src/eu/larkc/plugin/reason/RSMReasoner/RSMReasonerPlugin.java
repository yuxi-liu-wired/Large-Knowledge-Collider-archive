package eu.larkc.plugin.reason.RSMReasoner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
//import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
//import eu.larkc.core.data.VariableBindingValue;
import eu.larkc.core.data.VariableBinding.Binding;
//import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;

import java.rmi.RemoteException;


import com.saltlux.storm.conf.Configuration;
import com.saltlux.storm.conf.ConfigurationFactory;
import com.saltlux.storm.handler.rmi.env.SORChannelFuture;
import com.saltlux.storm.handler.rmi.env.SORChannelFutureFactory;
import com.saltlux.storm.sor.bridge.shared.ValueToNode;
import com.saltlux.storm.sor.bridge.sparql.SORQueryExecution;
//import com.saltlux.storm.sor.model.Value;
import com.saltlux.storm.sor.query.BindingSet;
import com.saltlux.storm.sor.query.ResultSet;


import eu.larkc.core.data.VariableBindingImpl;
import eu.larkc.core.data.VariableBindingImpl.BindingRow;



public class RSMReasonerPlugin extends Plugin {
	
	public RSMReasonerPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}
	
   
   
      
   
   
   
   
   
   
   protected String retrieveParameter(String param) {
		return retrieveParameter(param, getPluginParameters());
	}
	
	protected String retrieveParameter(String param, SetOfStatements pluginParams) {
		CloseableIterator<Statement> params = pluginParams.getStatements();
		while (params.hasNext()) {
			Statement stmt = params.next();
			String predicateName = stmt.getPredicate().toString();
			
			System.out.println("stmt=" + stmt.toString());
			if (predicateName.equals(param)) {
				Value value = stmt.getObject();
				System.out.println("find value=" + value.toString());
				return value.stringValue();
			}
		}
		return null;
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		// TODO Auto-generated method stub
		logger.info("RSMReasonerPlugin initialized.");	


		
	}

		
	

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		// TODO Auto-generated method stub
	
		
		SPARQLQuery query = DataFactory.INSTANCE.createSPARQLQuery(input);
		
		String query1 = query.toString();
		
		//if (!(query==null)) {this.theQuery = query;};
		
		VariableBinding result = null;	
		
		ResultSet rs = null;
		
		try {
			rs = querySPARQLtoResultSetBySOR(query1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(rs != null){
            System.out.println("Querying Finished...");					
			result = makeVariableBindingOfSORResult(rs);
			
		}

		return(result.toRDF(new SetOfStatementsImpl()));
		
		
	
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}


	private VariableBinding makeVariableBindingOfSORResult(ResultSet sorResult){
		List<String> variables = new ArrayList<String>();
//make varaible of result		
		for(Iterator<String> it = sorResult.getResultVars().iterator();it.hasNext();){
			variables.add(it.next());
		}
//make bindings of result
		List<Binding> bindings = new ArrayList<Binding>();		
		while(sorResult.hasNext()){			
			BindingSet sorBindings = sorResult.nextBindingSet();
			BindingRow bn = new BindingRow();
			for(Iterator<String> it = sorResult.getResultVars().iterator();it.hasNext();){
				bn.addValue(ValueToNode.convertSorToSesame(sorBindings.getValue(it.next())));
			}
			bindings.add(bn);
		}
		return new VariableBindingImpl(bindings, variables);
	}	
	private ResultSet querySPARQLtoResultSetBySOR(String query) throws RemoteException {
		ResultSet resultSet = null;
		
		String pluginjarfilename ="RSMReasoner-1.0.0-SNAPSHOT-PluginAssembly";
		try{
			String fileSeparator = System.getProperty("file.separator");
			String userDir = System.getProperty("user.dir");
			
			String reasonerPath = "plugins"+fileSeparator+"RoadSignMgmt_reasoner"+fileSeparator;	
			
	//		String systemPropertyPath = fileSeparator+reasonerPath+"conf"+fileSeparator+"system.properties";
			
			String systemPropertyPath = fileSeparator+"plugins"+fileSeparator+pluginjarfilename + fileSeparator +"system.properties";
			
			String sorConfigPath = userDir + systemPropertyPath;
			
			Configuration config = ConfigurationFactory.createConfiguration(sorConfigPath);
			SORChannelFuture conn = SORChannelFutureFactory.createDefaultConnection(config);
			
			System.out.println("[Remote Quering]");
			System.out.println(query);
			
			SORQueryExecution qexec = new SORQueryExecution(query, conn);		
			resultSet = qexec.execSORSelect();
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultSet;		
	}

	
	

}

