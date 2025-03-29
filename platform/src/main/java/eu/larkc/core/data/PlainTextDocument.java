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


/**
 * A plain text document, with text embedded (no caching or remote storage).
 */
public class PlainTextDocument implements NaturalLanguageDocument {

	/**
	 * Constructor.
	 * @param text The plain text associated with this object.
	 */
	public PlainTextDocument( String text ) {
		mText = text;
	}

	public String getText() {
		return mText;
	}

	private final String mText;

	/* (non-Javadoc)
	 * @see eu.larkc.core.data.InformationSet#toRDF(eu.larkc.core.data.SetOfStatements)
	 */
	@Override
	public SetOfStatements toRDF(SetOfStatements data) {
		throw new RuntimeException("Not implemented!");
	}
}
