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
package eu.larkc.core.pluginManager;

/**
 * This class represents a control message, containing the command and the path
 * id.
 * 
 * @author Norbert Lanzanasto
 * 
 */
public class ControlMessage {

	private Message message;
	private String pathId;

	/**
	 * Constructor.
	 * 
	 * @param m
	 *            the message
	 * @param p
	 *            the path id
	 */
	public ControlMessage(Message m, String p) {
		message = m;
		pathId = p;
	}

	/**
	 * Returns the message.
	 * 
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * Returns the path id.
	 * 
	 * @return path id
	 */
	public String getPathId() {
		return pathId;
	}

}
