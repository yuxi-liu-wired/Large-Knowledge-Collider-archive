package eu.larkc.plugin.hadoop;

import javax.jms.Message;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;


/**
 * A hadoop plugin that will dictionary encoding
 * @author spyros
 *
 */
public class DictionaryDecodingPlugin extends GenericHadoopPlugin {
	

	private static Logger logger = LoggerFactory.getLogger(Plugin.class);

	public DictionaryDecodingPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(input);

		
		// Get the input		
		String inputPath=attval.get(Constants.INPUT); 
		String outputPath = inputPath+"/output-decompressed";
		
		String[] args=new String[]{inputPath, outputPath, "--samplingPercentage", "10", "--samplingThreshold", "1000"};

		logger.debug("Exporting triples from " + inputPath + " to " + outputPath);
		
		runTool(config, "jobs.FilesExportTriples" , args);
		
		// Return pointers to files
		AttributeValueMap r=new AttributeValueMap();
		r.put(Constants.INPUT, outputPath);
		r.put(Constants.LASTMODIFIED, System.currentTimeMillis()+"");
		r.put(Constants.SIZE, "-1");
		
		return r.toRDF();
		}
		catch (Exception e) {
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

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		// TODO Auto-generated method stub
		
	}
}
