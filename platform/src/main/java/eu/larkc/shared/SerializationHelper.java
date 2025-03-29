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
package eu.larkc.shared;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.larkc.core.data.CloseableIterator;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.executor.Executor;

/**
 * Utility class for RDF serialization.
 * 
 * @author Christoph Fuchs
 * 
 */
public class SerializationHelper {

	private static Logger logger = LoggerFactory.getLogger(Executor.class);

	/**
	 * Prints the list of statements to the given output Stream. Call with
	 * <code>System.out</code> to print to stdout.
	 * 
	 * @param stmtList
	 *            the list of statements
	 * @param outputStream
	 *            the output stream to which to print. Call with
	 *            <code>System.out</code> to print to stdout.
	 * @param format
	 *            the RDFFormat which is used for serialization
	 */
	public static void printSetOfStatements(Collection<Statement> stmtList,
			OutputStream outputStream, RDFFormat format) {
		SailRepository therepository = new SailRepository(new MemoryStore());
		try {
			therepository.initialize();
			RepositoryConnection con = therepository.getConnection();
			con.add(stmtList);
			try {
				RDFWriter writer = Rio.createWriter(format, outputStream);
				con.export(writer);
			} catch (RDFHandlerException e) {
				logger.warn("RDFHandlerException occured: {}", e.getMessage());
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (RepositoryException e) {
			logger.warn("RepositoryException occured: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Prints the list of statements to the given output Stream. Call with
	 * <code>System.out</code> to print to stdout.
	 * 
	 * @param stmtList
	 *            the list of statements
	 * @param outputStream
	 *            the output stream to which to print. Call with
	 *            <code>System.out</code> to print to stdout.
	 * @param writer
	 *            the RDF/XML writer to serialize the statements
	 */
	public static void printSetOfStatements(Collection<Statement> stmtList,
			OutputStream outputStream, RDFXMLWriter writer) {

		SailRepository therepository = new SailRepository(new MemoryStore());
		try {
			therepository.initialize();
			RepositoryConnection con = therepository.getConnection();
			con.add(stmtList);
			try {
				con.export(writer);
			} catch (RDFHandlerException e) {
				logger.warn("RDFHandlerException occured: {}", e.getMessage());
				e.printStackTrace();
			} finally {
				con.close();
			}
		} catch (RepositoryException e) {
			logger.warn("RepositoryException occured: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Prints the list of statements to the given output Stream. Call with
	 * <code>System.out</code> to print to stdout.
	 * 
	 * @param statements
	 *            the set of statements
	 * @param outputStream
	 *            the output stream to which to print. Call with
	 *            <code>System.out</code> to print to stdout.
	 * @param format
	 *            the RDFFormat which is used for serialization
	 */
	public static void printSetOfStatements(SetOfStatements statements,
			OutputStream outputStream, RDFFormat format) {

		Set<Statement> stmntList = convertToSet(statements);
		printSetOfStatements(stmntList, outputStream, format);
	}

	/**
	 * Prints the list of statements to the given output Stream. Call with
	 * <code>System.out</code> to print to stdout.
	 * 
	 * @param statements
	 *            the set of statements
	 * @param outputStream
	 *            the output stream to which to print. Call with
	 *            <code>System.out</code> to print to stdout.
	 * @param writer
	 *            the RDF/XML writer to serialize the statements
	 */
	public static void printSetOfStatements(SetOfStatements statements,
			OutputStream outputStream, RDFXMLWriter writer) {

		Set<Statement> stmntList = convertToSet(statements);
		printSetOfStatements(stmntList, outputStream, writer);
	}

	/**
	 * Converts a <code>eu.larkc.core.data.SetOfStatements</code> to a
	 * <code>java.util.Set&lt;Statement&gt;</code>
	 * 
	 * @param statements
	 *            the <code>eu.larkc.core.data.SetOfStatements</code> to convert
	 * @return a <code>java.util.Set&lt;Statement&gt;</code> containing the
	 *         statements
	 */
	public static Set<Statement> convertToSet(SetOfStatements statements) {
		Set<Statement> stmntSet = new HashSet<Statement>();

		CloseableIterator<Statement> statementsIterator = statements
				.getStatements();
		while (statementsIterator.hasNext()) {
			stmntSet.add(statementsIterator.next());
		}
		return stmntSet;
	}

}
