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
package eu.larkc.plugin.sindiceidentifier;

import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.query.TriplePattern;
import eu.larkc.core.util.RDFConstants;

/**
 * Experimental LarKC Identifier plug-in for finding RDF documents on the web.
 */
public class SindiceIdentifier extends AbstractSindiceIdentifier {
	
	public SindiceIdentifier(URI pluginName) {
		super(pluginName);
	}

	@Override
	protected String makeSindiceQueryString(SetOfStatements sos, int pageNumber) {
		if(sos != null)
		{
			StringBuilder sindiceQuery = new StringBuilder();

			sindiceQuery.append("q=");
			
			CloseableIterator<Statement> it=sos.getStatements();
			
			boolean first = true;
			
			while (it.hasNext()) {
				Statement s = it.next();
				
				if (first) {
					first = false;
				}
				else{
					sindiceQuery.append("+AND+");
				}			
				
				sindiceQuery.append(urlEncodeConstraint(new TriplePattern(s.getSubject(), s.getPredicate(), s.getObject())));	
			}
			
			it.close();

			sindiceQuery.append("&qt=advanced");
			sindiceQuery.append("&format=atom");
			sindiceQuery.append("&page=").append(pageNumber);
	
			return "http://api.sindice.com/v2/search" + "?" + sindiceQuery.toString();
		}
		
		return "";
	}

	private String urlEncodeConstraint(TriplePattern constraint) {
		return urlEncode(convertTerm(constraint.getSubject()) + " " + convertTerm(constraint.getPredicate()) + " " + convertTerm(constraint.getObject()));
	}

	//TODO
	private String convertTerm(Value theValue) {
		if (theValue == null || theValue.stringValue().startsWith(RDFConstants.LARKC_NAMESPACE+"var")) { //  exclude vars
			return "*";
		}
		
		if(theValue instanceof BNode) {
				return "*";
		}
		
		if (theValue instanceof URI) {
			if(theValue.stringValue().equals(DataFactory.LARKC_NS+ "SetOfStatements"))
				return "*";
			else if(theValue.stringValue().equals(RDFConstants.LARKC_ATTVALUE.stringValue()))
				return "*";
			else
				return "<" + theValue.toString() + ">";
		}
		
		return theValue.toString();
	}

	public URI getIdentifier() {
		return new URIImpl("urn:"+this.getClass().getCanonicalName());
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}
}
