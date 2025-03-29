package eu.larkc.plugin.hadoop;

import java.io.IOException;

import javax.jms.Message;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;


/**
 * A hadoop plugin that will do dictionary encoding
 * @author spyros
 *
 */
public class DictionaryEncodingPlugin extends GenericHadoopPlugin {
	
	private static Logger logger = LoggerFactory.getLogger(Plugin.class);

	public DictionaryEncodingPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(input);
		
		// Get the input		
		Path inputPath= new Path(attval.get(Constants.INPUT));
		inputPath = ensurePathIsOnDFS(config, inputPath);
		Path outputPath=new Path(inputPath.toString()+"/../"+System.currentTimeMillis()+"pool");
		Path tempPath=new Path("/tmp/"+this.getClass().getSimpleName().toString()+System.currentTimeMillis()).makeQualified(FileSystem.get(config));
		
		String[] args=new String[]{inputPath.toString(), tempPath.toUri().toString() , outputPath.toString(), "--samplingPercentage", "10", "--samplingThreshold", "1000"};
		
		runTool(config, "jobs.FilesImportTriples" , args);
		
		// Return pointers to files
		AttributeValueMap r=new AttributeValueMap();
		r.put(Constants.INPUT, outputPath.toString());
		r.put(Constants.LASTMODIFIED, System.currentTimeMillis()+"");
		r.put(Constants.SIZE, "-1");
		
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

	/**
	 * For testing purposes
	 * @param args
	 */
	public static void main(String args[]) {
		
		DictionaryEncodingPlugin dictionaryEncodingPlugin=new DictionaryEncodingPlugin(new URIImpl("http://fake"));
		
		// The commented code is loaded on the config of Hadoop
		AttributeValueMap attv=new AttributeValueMap();
	/*	attv.put(HDFSUSERNAME, "hpckot,hpckot");
		attv.put(SOCKSPROXY, "localhost:10000");
		attv.put(FSSERVER, "hdfs://n110413:9000/");
		attv.put(JOBTRACKER, "n110413:9001");*/
		
		attv.put(Constants.INPUT, "file:///Users/spyros/Documents/workspace/lubm_1"); // Put your input data here
		
		dictionaryEncodingPlugin.initialiseInternal(attv.toRDF());
		dictionaryEncodingPlugin.invoke(attv.toRDF());
	}

	@Override
	public void onMessage(Message message) {
	}
	
}
