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
package eu.larkc.plugin;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.data.VariableBinding;
import eu.larkc.core.data.VariableBindingValue;
import eu.larkc.core.data.VariableBinding.Binding;
import eu.larkc.core.data.VariableBindingImpl;


public class SOStoVBtransformer extends Plugin {
	
	public SOStoVBtransformer() {
		super(new URIImpl("urn:SOStoVBtransformer"));
	}
		    
	public SOStoVBtransformer(URI pluginName) {
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
		logger.debug("Converting SoS...");
		
		List<Binding> bindings = new ArrayList<Binding>();
		CloseableIterator<Statement> iter = input.getStatements();
		int count = 0;
		
		while (iter.hasNext()) {
			Statement s = iter.next();
			List<Value> values = new ArrayList<Value>(7);
			values.add(s.getSubject());
			values.add(s.getPredicate());
			values.add(s.getObject());
			bindings.add(new WrappedBinding(values));
			count++;
			
			//FIXME: A quick hack to fix the timeout exception in sparql endpoint
			if (count >= 1000) {
				break;
			}
		}
				
		List<String> variables = new ArrayList<String>();
		variables.add("s");
		variables.add("p");
		variables.add("o");
		VariableBinding results = new VariableBindingImpl(bindings, variables);
		
		logger.debug("Converted " + count +  " statements to bindings.");
		
		return results.toRDF(new SetOfStatementsImpl());
	}

	@Override
	protected void initialiseInternal(SetOfStatements workflowDescription) {
		//TODO
	}
		
	public static void main(String args[]) {
		String filename = "Reasoner.input";
		if (args.length > 0) {
			filename = args[0];
		}
		SetOfStatements ss = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			ss = (SetOfStatements) in.readObject();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SetOfStatements output = new SOStoVBtransformer(new URIImpl(
				"urn:tmp:1")).invokeInternal(ss);

		if (output != null) {
			CloseableIterator<Statement> it = output.getStatements();

			while (it.hasNext()) {
				Statement s = it.next();
				if (s.getPredicate().equals(new URIImpl("http://larkc.eu/VariableBinding"))) {
					VariableBindingValue vb = (VariableBindingValue) s.getObject();
					
					CloseableIterator<Binding> ivb = vb.v.iterator();
					while (ivb.hasNext()) {
						Binding b = ivb.next();
						logger.info(b.toString());
					}
				}
			}
		}
	}

	@Override
	protected void shutdownInternal() {
		// TODO Auto-generated method stub
		
	}	
	
	public class WrappedBinding implements Binding {

		public List<Value> values = new ArrayList<Value>();

		public WrappedBinding(List<Value> values) {
			if (values == null) {
				throw new IllegalArgumentException("null!");
			}
			this.values = values;
		}

		public List<Value> getValues() {
			return Collections.unmodifiableList(values);
		}

		public String toString() {
			return values.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Binding) {
				Binding b = (Binding) obj;
				return b.getValues().equals(values);
			}
			return super.equals(obj);
		}
	}
}
