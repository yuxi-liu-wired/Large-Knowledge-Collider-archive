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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

/**
 * @author vassil
 */
public class LabelledGroupOfStatementsImpl implements LabelledGroupOfStatements {

	private static final long serialVersionUID = 1L;
	private transient RdfStoreConnection con;
	private URI label;
	private int transId;

	/**
	 * Constructor
	 * 
	 * @param con
	 *            connection to a local repository where the labelled group of
	 *            statements is created.
	 */
	public LabelledGroupOfStatementsImpl(RdfStoreConnection con) {
		if (con == null) {
			throw new IllegalArgumentException("null!");
		}
		this.con = con;
		this.label = generateLabel();
	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            is explicit named of the labelled group of statements.
	 * @param con
	 *            connection to a local repository
	 * @param validate
	 *            if this group already exists
	 */
	public LabelledGroupOfStatementsImpl(URI label, RdfStoreConnection con,
			boolean validate) {
		if (con == null || label == null) {
			throw new IllegalArgumentException("null!");
		}
		if (validate) {
			CloseableIterator<Statement> iter = con.search(null, null, null,
					null, label);
			if (iter.hasNext()) {
				validate = false;
			}
			iter.close();
			if (validate) {
				throw new IllegalArgumentException("The label is already used!");
			}
		}
		this.con = con;
		this.label = label;
	}

	public boolean excludeStatement(Resource subj, URI pred, Value obj,
			URI graph) {
		if (con.deassociateStatements(subj, pred, obj, graph, label)) {
			transId++;
			return true;
		}
		return false;
	}

	public boolean excludeStatement(Statement s) {
		if (s != null && s instanceof URI == false) {
			return false;
		}
		if (con.deassociateStatements(s.getSubject(), s.getPredicate(), s
				.getObject(), (URI) s.getContext(), label)) {
			transId++;
			return true;
		}
		return false;
	}

	public URI getLabel() {
		return label;
	}

	public RdfStoreConnection getRdfStoreConnection() {
		return con;
	}

	public boolean includeStatement(Resource subj, URI pred, Value obj,
			URI graph) {
		if (con.associateStatements(subj, pred, obj, graph, label)) {
			transId++;
			return true;
		}
		return false;
	}

	public boolean includeStatement(Statement s) {
		if (con.associateStatements(s.getSubject(), s.getPredicate(), s
				.getObject(), (URI) s.getContext(), label)) {
			transId++;
			return true;
		}
		return false;
	}

	public CloseableIterator<Statement> getStatements() {
		return con.search(null, null, null, null, label);
	}

	public boolean equals(Object o) {
		if (o instanceof LabelledGroupOfStatementsImpl == false) {
			return false;
		}
		if (label.equals(((LabelledGroupOfStatementsImpl) o).label) == false) {
			return false;
		}
		return transId == ((LabelledGroupOfStatementsImpl) o).transId;
	}

	public int hashCode() {
		// the mutable hashcodes are risky (transId is excluded)
		return label.hashCode();
	}

	private URI generateLabel() {
		Random rand = new Random();
		return new URIImpl("http://labelledgroup.larkc.eu/" + rand.nextDouble());
	}

	// Custom serialization methods
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(label);
		Iterator<Statement> iter = getStatements();
		out.writeBoolean(iter.hasNext());
		while (iter.hasNext()) {
			Statement s = iter.next();
			out.writeObject(s);
			out.writeBoolean(iter.hasNext());
		}
		out.flush();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		con = DataFactory.INSTANCE.createRdfStoreConnection();
		label = (URI) in.readObject();
		while (in.readBoolean()) {
			Statement s = (Statement) in.readObject();
			includeStatement(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.larkc.core.data.InformationSet#toRDF(eu.larkc.core.data.SetOfStatements
	 * )
	 */
	@Override
	public SetOfStatements toRDF(SetOfStatements data) {
		throw new RuntimeException("Not implemented!");
	}
}
