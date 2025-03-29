package eu.larkc.plugin.hadoop;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.jms.Message;

import org.apache.hadoop.fs.Path;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;


/**
 * A hadoop plugin that will dictionary encoding
 * @author spyros
 *
 */
public class OWLClosurePlugin extends GenericHadoopPlugin {

	private static Logger log = LoggerFactory.getLogger(OWLClosurePlugin.class);

	public OWLClosurePlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		
	
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(input);
		
		Path inputPath= new Path(attval.get(Constants.INPUT));
		
		String[] args=new String[]{inputPath.toString(), "--fragment", "rdfs", "--rulesStrategy", "fixed", "--duplicatesStrategy", "end"};
		log.info("Running OWLClosurePlugin with arguments "+ Arrays.toString(args));	
		
		try {
			runTool(config, "jobs.Reasoner" , args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		int step=0;
		
		FilesRDFSReasoner rdfsReasoner = new FilesRDFSReasoner();
		rdfsReasoner.setConf(config);
		FilesOWLReasoner owlReasoner = new FilesOWLReasoner();
		owlReasoner.setConf(config);
		try {
			long totalDerivation = 0;
			boolean continueDerivation = true;
			long startTime = System.currentTimeMillis();
			long rdfsDerivation = 0;
			long owlDerivation = 0;
			boolean firstLoop = true;
			
			while (continueDerivation) {
				//Do RDFS reasoning
				if (owlDerivation == 0 && !firstLoop) {
					rdfsDerivation = 0;
				} else {
					FilesRDFSReasoner.step = step;
					rdfsDerivation = rdfsReasoner.launchDerivation(args);
				}
				
				//Do OWL reasoning
				if (rdfsDerivation == 0 && !firstLoop) {
					owlDerivation = 0;
				} else {
					FilesOWLReasoner.step = FilesRDFSReasoner.step;
					owlDerivation = owlReasoner.launchClosure(args);
					step = FilesOWLReasoner.step;
				}
				totalDerivation += owlDerivation + rdfsDerivation;
				continueDerivation = (owlDerivation + rdfsDerivation) > 0;
				firstLoop = false;
			}
			log.info("Number triples derived: " + totalDerivation);
			log.info("Time derivation: " + (System.currentTimeMillis() - startTime));
		} catch (Exception e) {
			log.error("error on running reasoner", e);
		}*/
		
		// Return pointers to files
		AttributeValueMap r=new AttributeValueMap();
		r.put(Constants.INPUT, inputPath.toString());
		r.put(Constants.LASTMODIFIED, System.currentTimeMillis()+"");
		r.put(Constants.SIZE, "-1");
		return r.toRDF();
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}



	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
	}
	
}
