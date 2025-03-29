/*
 * Copyright (C) 2008, 2009 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.larkc.plugin.identify.sindice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.RdfGraph;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.util.RDFConstants;

import eu.larkc.plugin.Plugin;

/**
 * Experimental LarKC Identifier plug-in for finding RDF documents on the web.
 */
public abstract class AbstractSindiceIdentifier extends Plugin {
	
	protected static Logger logger = LoggerFactory.getLogger(Plugin.class);
	
	
	protected URI outputGraphName;
	
	public AbstractSindiceIdentifier(URI pluginName) {
		super(pluginName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Use java.util.logging over slf4j since setting up apache-tomcat6 to use slf4j is non-trivial
	 */
	public static final int NUM_PAGES_PER_REQUEST = 10;
	
	@Override
	public void shutdown() {
	}

	protected abstract String makeSindiceQueryString(SetOfStatements sos, int pageNumber);

	DownloadSession session = new DownloadSession();
	
	protected Collection<RdfGraph> parseResults(String htmlResponsePage) throws Exception {
		if (htmlResponsePage == null || htmlResponsePage.isEmpty()) {
			throw new RuntimeException("HTML response page must not be null nor empy!");
		}
		// Note: Shouldn't the HTML response (which is XML)
		// be parsed (using an XML parser), rather than doing a simple
		// substring? ~Christoph Fuchs
		List<String> urlStrings = getSubString(htmlResponsePage, "<id>", "</id>");
		
		// Remove the first url from the list since it is the sindice api query
		// string (this line could be removed if the response would be parsed
		// properly)
		urlStrings.remove(0);
		
		for (String urlString : urlStrings) {
			session.fetch( urlString );
		}
		return session.getFetchedGraphs();
	}

	private List<String> getSubString(String theInput, String startString, String endString) {
		List <String> result = new ArrayList <String> ();
		int index = theInput.indexOf(startString);
		while (index != -1){
			index += startString.length();
			int endIndex = theInput.indexOf(endString, index);
			result.add(theInput.substring(index, endIndex));
			index = theInput.indexOf(startString, endIndex + endString.length());
		}
		return result;
	}
	
	class DownloadSession {
		
		synchronized void notifyWaiter() {
			notifyAll();
		}
	
		class Fetcher extends Thread {
			Fetcher(URI uri) {
				super( "Sindice RDF downloader" );
				if (uri == null) {
					throw new IllegalArgumentException();
				}
				this.uri = uri;
			}
	
			public void run() {
				RdfGraph remoteGraph = DataFactory.INSTANCE.createRemoteRdfGraph(uri);
				try {
//					RdfStoreConnection con = DataFactory.INSTANCE.createRdfStoreConnection();
		            CloseableIterator<Statement> iter = remoteGraph.getStatements();  
		            ArrayList<Statement> statements = new ArrayList<Statement>();
		            while (iter.hasNext()) {
		                    Statement s = iter.next();
//		                    statements.add(con.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), (URI)s.getContext()));
		    				statements.add( new StatementImpl( s.getSubject(), s.getPredicate(), s.getObject() ) );
		            }
		            iter.close();
		
		            logger.info("Found " + statements.size() + " statements at " + uri);
		            
		            RdfGraph graph = DataFactory.INSTANCE.createRdfGraph(statements, remoteGraph.getName());
		            mFetchedGraphs.add( graph );
	            }
	            catch( RuntimeException e ) {
	            	logger.debug("Cannot download remote RDF: " + e.getMessage());
	            }
				mFetcherCount.decrementAndGet();
				fetchers.remove(this);
				notifyWaiter();
			}
	
			private final URI uri;
		}
		
		synchronized List<RdfGraph> getFetchedGraphs() {
			
			List<RdfGraph> results = new ArrayList<RdfGraph>();
			
			int maxWaitTime = 2000;

			int numberOFExtendedWaitsAllowed = 10;
			
			int extended = 0;
			
			while( maxWaitTime > 0 && mFetcherCount.intValue() > 0 ) {
				long start = System.currentTimeMillis();
				try {
	                wait( maxWaitTime );
                }
                catch( InterruptedException e ) {
                }
				long stop = System.currentTimeMillis();
				int waited = (int)(stop - start);
				maxWaitTime -= waited;
				
				if( maxWaitTime <= 0 && mFetcherCount.intValue() > 0 && mFetchedGraphs.size() == 0 && extended != numberOFExtendedWaitsAllowed){
					maxWaitTime = 2000;
					extended++;
				}
			}
			
			while( mFetchedGraphs.size() > 0 )
				results.add( mFetchedGraphs.remove( 0 ) );
			
			for (Fetcher f : fetchers){
				// TODO: BARRY - thread.stop() doesnt do anythign useful, i can;t work out how to kill the download threads that are still alive when we return. 
			}

			return results;
		}
		
		private final AtomicInteger mFetcherCount = new AtomicInteger( 0 );
		private final List<RdfGraph> mFetchedGraphs = Collections.synchronizedList( new ArrayList<RdfGraph>() ); 
		private final List<Fetcher> fetchers = new ArrayList <Fetcher>();
	
		void fetch( String url ) {
	        Fetcher f = new Fetcher(new URIImpl(url));
	        fetchers.add(f);
	        mFetcherCount.incrementAndGet();
	        f.start();
		}
	}

	protected String urlEncode(String plainText) {
		try {
			return URLEncoder.encode(plainText, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		List<RdfGraph> results = new ArrayList<RdfGraph>();

		logger.debug("Got query: "+input.getStatements().toString());
			
		CloseableIterator<Statement> it=input.getStatements();
		
		try {
			for (int page = 1; page <= NUM_PAGES_PER_REQUEST; ++page) {
				
				logger.debug("Preparing query for Sindice...");
				
				String url = makeSindiceQueryString(input, page);
				
				logger.debug("Response URL string: " + URLDecoder.decode(url, "UTF-8"));
				
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				
				String responseString = "";
				
				if (entity != null) {
					responseString = EntityUtils.toString(entity);
				}
				
				if (responseString.isEmpty()) {
					logger.error("HTTP response (from HttpUtils) is empty!");
				}
				
				logger.debug("Results: "+responseString);
				
				Collection<RdfGraph> pageResults = parseResults(responseString);
				
				if( pageResults.size() == 0 ) {
					break;
				}
				else{
					results.addAll(pageResults);
				}
			}
		} 
		catch (Exception e) {
			logger.error("Failed to identify sources: " + e.getMessage());
		}

		if (results.isEmpty()){
			return input;
		}
		
		logger.info("Returning " + results.size() + " results");
		
		List<Statement> statements=new ArrayList<Statement>(10000);
		
		
		for (RdfGraph is : results) {
			CloseableIterator<Statement> iter=is.getStatements();
			while (iter.hasNext())
				statements.add(iter.next());
		}
		
		return DataFactory.INSTANCE.createRdfGraph(statements,outputGraphName);
	}
	
	
	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		outputGraphName=super.getNamedGraphFromParameters(workflowDescription, RDFConstants.DEFAULTOUTPUTNAME);
	}
}
