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
package eu.larkc.plugin.select;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.Statement;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;

import eu.larkc.plugin.Plugin;

import javax.jms.Message;

public class GrowingDataSetSelecter extends Plugin {
	
	private List<Statement> allStatements = new ArrayList<Statement>();

	public GrowingDataSetSelecter(URI pluginName) {
		super(pluginName);
	}

	@Override
	public void shutdown() {
	}

	public URI getIdentifier() {
		return new URIImpl("urn:"+this.getClass().getName());
	}

	@Override
	protected SetOfStatements invokeInternal(SetOfStatements input) {
		try {
			CloseableIterator<Statement> itInput = input.getStatements();
			
			while( itInput.hasNext() ) {
				Statement statement = itInput.next();
				
				logger.debug("Statement: "+statement.getSubject().stringValue() + ", "+statement.getPredicate().stringValue() + ", "+statement.getObject().stringValue() + "");
				
				allStatements.add( new StatementImpl( statement.getSubject(), statement.getPredicate(), statement.getObject() ) );
			}
			
			SetOfStatementsImpl output = new SetOfStatementsImpl( allStatements );
					
			logger.debug("Nr. of Statements: "+allStatements.size());
			
			return output;
		}
		catch( RuntimeException e ) {
			throw new RuntimeException(e);
		}
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
