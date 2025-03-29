package eu.larkc.plugin.hadoop;

import java.io.File;

import javax.jms.Message;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.AttributeValueMap;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.plugin.Plugin;

public class FileIdentifier extends Plugin {
	private String filePath;
	private long lastModified;
	private Logger log=LoggerFactory.getLogger(FileIdentifier.class);
	/**
	 * 
	 * @param pluginName
	 */
	public FileIdentifier(URI pluginName) {
		super(pluginName);
	}

	@Override
	public void initialiseInternal(SetOfStatements params) {
		// Get the input path passed as a parameter
		AttributeValueMap attVal = DataFactory.INSTANCE.createAttributeValueList(params);
		filePath = attVal.get(Constants.INPUT);
	}

	/**
	 * Returns information about the file passed as an initialization parameter
	 */
	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		AttributeValueMap m=new AttributeValueMap();
		File f;
		try {
			f = new File(filePath).getCanonicalFile();
			if (!f.isDirectory() && f.exists()) {
				lastModified=f.lastModified();
				m.put(Constants.LASTMODIFIED,lastModified+"");
				m.put(Constants.SIZE, f.length()+"");
			}
			else if (f.isDirectory()) { // TODO make it check the entire tree
				lastModified=f.lastModified();
				for (File s:f.listFiles()) {
					if (s.lastModified()>lastModified)
						lastModified=s.lastModified();
				}
				m.put(Constants.LASTMODIFIED, lastModified+"");
			}
			else {
				log.warn("Can not stat input file, assuming that it does not change");
			}
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Problem with input file: " + filePath,e);
		}
		m.put(Constants.INPUT,f.toString());

		return m.toRDF();
	}

	@Override
	public void shutdown() {}

	/* (non-Javadoc)
	 * @see eu.larkc.plugin.Plugin#cacheLookup(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	protected SetOfStatements cacheLookup(SetOfStatements input) {
	/*	SetOfStatements cached=super.cacheLookup(input);
		if (cached==null)
			return null; //We don't have it anyway
		try {
			File f = new File(filePath).getCanonicalFile();
			if (this.lastModified==f.lastModified())
				return cached;
		} catch (IOException e) {
			log.warn("Error stating file", e);
		}*/
		return null;
	}

	@Override
	public void onMessage(Message message) {
	}
	

}
