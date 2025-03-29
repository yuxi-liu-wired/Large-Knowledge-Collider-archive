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
package eu.larkc.core.data.iterator;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;

import eu.larkc.core.data.CloseableIterator;

public class GraphQueryResultCloseableIterator<Statement> implements
		CloseableIterator<Statement> {

	private GraphQueryResult iterator;
	private boolean isClosed;

	public GraphQueryResultCloseableIterator(GraphQueryResult rr) {

		iterator = rr;
		this.isClosed = false;
	}

	public void close() {
		isClosed = true;
		iterator = null;
	}

	public boolean hasNext() {
		if (isClosed) {
			throw new RuntimeException("Iterator is closed!");
		}
		try {
			return iterator.hasNext();
		} catch (QueryEvaluationException e) {
			throw new RuntimeException(e);
		}
	}

	public Statement next() {
		if (isClosed) {
			throw new RuntimeException("Iterator is closed!");
		}
		try {
			return (Statement) iterator.next();
		} catch (QueryEvaluationException e) {
			throw new RuntimeException(e);
		}
	}

	public void remove() {
		if (isClosed) {
			throw new RuntimeException("Iterator is closed!");
		}
		try {
			iterator.remove();
		} catch (QueryEvaluationException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isClosed() {
		return isClosed;
	}
}
