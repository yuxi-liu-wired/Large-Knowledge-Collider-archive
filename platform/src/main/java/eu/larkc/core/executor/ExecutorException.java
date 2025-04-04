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
package eu.larkc.core.executor;

/**
 * This exception indicates that there was an error while initializing the
 * executor. The message specifies the error.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class ExecutorException extends Exception {

	/**
	 * Version UID.
	 */
	private static final long serialVersionUID = 1L;
	private String text;

	/**
	 * Custom constructor which takes a text as input.
	 * 
	 * @param text
	 *            text that indicates the error in the workflow graph.
	 */
	public ExecutorException(String text) {
		this.text = text;
	}

	public String toString() {
		return text;
	}

	@Override
	public String getMessage() {
		return text;
	}
}
