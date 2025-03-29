package eu.larkc.plugin.hadoop.filterplugin;

import java.io.IOException;

import javax.jms.Message;

import org.apache.hadoop.fs.Path;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
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



	
}
