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
 * A hadoop plugin to transfer files to/from hdfs. TODO TEST
 * @author spyros
 *
 */
public class HDFSPlugin extends GenericHadoopPlugin {
	
	private static Logger logger = LoggerFactory.getLogger(Plugin.class);
	public static final String DIRECTION=HDFSPlugin.class.getCanonicalName() + "-Direction";
	public static final String TOHDFS="TOHDFS";
	public static final String FROMHDFS="FROMHDFS";
	private String direction;
	
	
	public HDFSPlugin(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	public void initialiseInternal(SetOfStatements params) {
		super.initialiseInternal(params);
		
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(params);
		
		direction = attval.get(DIRECTION);
		if (direction==null || ( !direction.equals(TOHDFS) && !direction.equals(FROMHDFS) )) {
			throw new UnsupportedOperationException("DIRECTION not specified. direction="+direction);
		}

	}



	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
		AttributeValueMap attval=DataFactory.INSTANCE.createAttributeValueList(input);
		
		
		// Get the input
		
		Path inputPath= new Path(attval.get(Constants.INPUT));
		Path outputPath=new Path(inputPath.toString()+"/../"+System.currentTimeMillis()+"pool");
		
		if (direction.equals(TOHDFS)) {
			inputPath = ensurePathIsOnDFS(config, inputPath);
		}
		else if (direction.equals(FROMHDFS)) {
			inputPath = ensurePathIsLocal(config, inputPath);
		}
		else {
			throw new UnsupportedOperationException("Error determining direction");
		}
		
		
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
		
		HDFSPlugin dictionaryEncodingPlugin=new HDFSPlugin(new URIImpl("http://fake"));
		
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
		// TODO Auto-generated method stub
		
	}
	
}
