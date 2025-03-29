package eu.larkc.plugin.reasoner.iris.extractor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.storage.IRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple IRISExtractor implementation. This class tries to load a datalog
 * file and parses it using the IRIS parser. If successful, facts, rules and
 * queries can be extracted.
 * 
 * @author Christoph Fuchs, Iker Larizgoitia
 * 
 */
public class IRISDatalogExtractor implements IRISExtractor {

	private Parser parser;
	private Logger logger = LoggerFactory.getLogger(IRISDatalogExtractor.class);

	/**
	 * Constructor. Tries to read a datalog file and instantiates an IRIS parser
	 * to extract facts, rules & queries.
	 * 
	 * @param datalogFile
	 *            the URL of the datalog file
	 * @throws ParserException
	 * @throws IOException
	 */
	public IRISDatalogExtractor(URL datalogFile) throws ParserException,
			IOException {

		// Read the file provided
		File file;
		try {
			file = new File(datalogFile.toURI());
			String datalogString = FileUtils.readFileToString(file);

			// factsString = this.replaceNamespaces(factsString);

			// Use the IRIS parser to parse the file
			this.parser = new Parser();
			this.parser.parse(datalogString);

		} catch (URISyntaxException e) {
			// Ignored
		}

	}

	@Override
	public Map<IPredicate, IRelation> getFacts() {
		return parser.getFacts();
	}

	@Override
	public List<IRule> getRules() {
		return parser.getRules();
	}

	@Override
	public List<IQuery> getQueries() {
		return parser.getQueries();
	}

	public IRISDatalogExtractor(String datalogString) throws ParserException,
			IOException {

		logger.debug("Trying to parse string: {}", datalogString);

		// Use the IRIS parser to parse the file
		this.parser = new Parser();
		this.parser.parse(datalogString);
	}
}
