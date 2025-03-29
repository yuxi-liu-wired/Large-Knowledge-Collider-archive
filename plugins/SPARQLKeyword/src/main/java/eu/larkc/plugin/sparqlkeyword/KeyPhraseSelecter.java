package eu.larkc.plugin.sparqlkeyword;

import org.openrdf.model.URI;

import eu.larkc.plugin.sparqlkeyword.annotator.KeyPhraseAnnotator;

public class KeyPhraseSelecter extends BaseLineFTSelecter {
	public KeyPhraseSelecter(URI pluginName) {
		super(pluginName);
	}

	protected String prepareQuery(String word) {
		return "SELECT DISTINCT ?o WHERE { ?s <" + KeyPhraseAnnotator.HAS_KEY_PHRASES.stringValue() + "> ?o . " +
					"<" + word + ":> <http://www.ontotext.com/matchIgnoreCase> ?o}";
	}
}
