/*
   This file is part of the LarKC platform 
   http://www.larkc.eu/

   Copyright 2010 LarKC project consortium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eu.larkc.core.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of XMLDocument interface.
 * Given the location of an XML file as input it retrieves the document.
 * 
 * @author Daniele Dell'Aglio
 *
 */

public class XMLDocumentImpl implements XMLDocument {
	private static final long serialVersionUID = 1L;
	private URL url;
	private String doc;
	private URL xslt;
	private String graphName;

	public XMLDocumentImpl(String url) {
		try{
			this.url = new URL(url);

			doc=null;
		}
		catch(MalformedURLException e){
			e.printStackTrace();
		}
	}

	public XMLDocumentImpl(URL url) {
		this.url = url;
	}

	public URL getURL(){
		URL ret = null;
		try{
			ret=new URL(url.toString());
		}catch (Exception e) {
		}
		return ret;
	}
	
	@Override
	public void setXslt(URL xslt){
		this.xslt = xslt;
	}
	
	@Override
	public URL getXslt(){
		return xslt;
	}

	@Override
	public String getDocument() {
		if(doc==null){
			try{
				// connect to the url
				HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
				connection.connect();
				// retrieve the XML document
				StringBuffer sb = new StringBuffer();
				InputStream is = connection.getInputStream();
				int ch;
				while ((ch = is.read()) > -1) {
					sb.append((char)ch);
				}
				doc = sb.toString();
			} catch (IOException e) {
				return null;
			}
		}
		return doc;
	}

	@Override
	public String getGraphName() {
		return graphName;
	}

	@Override
	public void setGraphName(String gn) {
		this.graphName=gn;
	}

	/* (non-Javadoc)
	 * @see eu.larkc.core.data.InformationSet#toRDF(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	public SetOfStatements toRDF(SetOfStatements data) {
		throw new RuntimeException("Not implemented!");
	}

}
