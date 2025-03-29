package eu.larkc.plugin.pariris;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.terms.TermFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import at.sti2.rif4j.parser.xml.XmlParser;
import at.sti2.rif4j.rule.Document;
import at.sti2.rif4j.translator.iris.RifToIrisTranslator;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.MultiMapReducePlanner;
import cascading.operation.DebugLevel;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;
import eu.larkc.iris.evaluation.EvaluationContext;
import eu.larkc.iris.evaluation.bottomup.DistributedBottomUpEvaluationStrategyFactory;
import eu.larkc.iris.evaluation.bottomup.naive.DistributedEvaluatorFactory;
import eu.larkc.iris.imports.Importer;
import eu.larkc.iris.rules.compiler.CascadingRuleCompiler;
import eu.larkc.iris.rules.compiler.FlowAssembly;
import eu.larkc.iris.rules.compiler.IDistributedCompiledRule;
import eu.larkc.iris.storage.FactsConfigurationFactory;
import eu.larkc.iris.storage.FactsFactory;

/**
 * Evaluator which extends the cascading test to avoid re-implementation of the
 * code which is necessary to set up the environment for parallel execution.
 * 
 * @author Christoph Fuchs; adapted from code by Valer Roman and Florian Fischer
 * 
 */
public class CascadingEvaluator {

	// Use seperate logger for better output filtering possibilities
	Logger logger = LoggerFactory.getLogger(CascadingEvaluator.class);
	
	// Cluster testing property
	public static final String CLUSTER_TESTING_PROPERTY = "test.cluster.enabled";

	// Flag to enable cluster
	private boolean enableCluster;

	// Knowledge base related
	private URL pathToFacts;
	private URL pathToRules;
	private List<IRule> rules;
	private List<IQuery> queries;

	// Cluster related
	transient private static MiniDFSCluster dfs;
	transient private static MiniMRCluster mr;
	
	// Number of map and reduce tasks
	private int numMapTasks = 1;
	private int numReduceTasks = 1;
	
	// Other private fields
	private static FileSystem fileSys;
	private JobConf jobConf;
	private RifToIrisTranslator translator;
	private eu.larkc.iris.Configuration defaultConfiguration;
	private long duration;

	public CascadingEvaluator(URL factBaseURL, URL ruleBaseURL) {
		this.pathToFacts = factBaseURL;
		this.pathToRules = ruleBaseURL;
		try {
			setUpEvaluator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setUpEvaluator() throws IOException, SAXException, ParserConfigurationException {
		logger.debug("Cascading evaluator: Setting up...");
		
		// Configure the evaluation strategy
		setUpEvaluationStrategy();

		// If we have a job configuration already, there is nothing to do anymore.
		if (jobConf != null)
			return;

		// Create a new configuration for hadoop and our job
		Configuration conf = new Configuration();
		defaultConfiguration.hadoopConfiguration = conf;
		setUpJobConfiguration(conf);

		// Configure flow properties
		setUpFlowProperties();

		// Apply our job configuration to the default configuration
		defaultConfiguration.jobConf = jobConf;

		// Configure fact properties
		FactsFactory.PROPERTIES = "/facts-configuration-test.properties";
		FactsConfigurationFactory.STORAGE_PROPERTIES = "/facts-storage-configuration-test.properties";
		
		// Set up the knowledge base consisting of a set of facts and a set of rules
		setUpKnowledgeBase();
		
		// Create queries
		queries = translator.getQueries();

		logger.debug("Cascading evaluator: Done.");
	}

	private void setUpKnowledgeBase() throws FileNotFoundException,
			SAXException, IOException, ParserConfigurationException {
		logger.debug("Knowledge Base: Setting up...");
		
		// Read the RIF file
		logger.debug("Knowledge Base: Reading rules file (RIF)...");
		File rulesFile = new File(pathToRules.getPath());
		Reader rifXmlFileReader = new FileReader(rulesFile);
		XmlParser parser = new XmlParser();
		logger.debug("Knowledge Base: Parsing document...");
		Document rifDocument = parser.parseDocument(rifXmlFileReader);

		// Translate RIF to IRIS
		logger.debug("Knowledge Base: Translating RIF format to IRIS...");
		translator = new RifToIrisTranslator();
		translator.translate(rifDocument);
		
		// Create facts
		logger.debug("Knowledge Base: Setting up facts...");
		try {
			setUpFacts();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Create rules
		logger.debug("Knowledge Base: Getting rules...");
		rules = translator.getRules();

		logger.debug("Knowledge Base: Done.");
	}

	private void setUpFacts() throws IOException {
		defaultConfiguration.project = "ParallelIrisReasoner";
		Importer importer = new Importer(defaultConfiguration);
		if (enableCluster) {
			importer.importFromFile(pathToFacts.getPath(), "import");
		} else {
			importer.processNTriple(pathToFacts.getPath(), "import");
		}
	}

	private void setUpFlowProperties() {
		logger.debug("Flow properties: Setting up...");

		String cascadingKey = "cascading.serialization.tokens";
		String cascadingValue = "130=eu.larkc.iris.storage.IRIWritable,131=eu.larkc.iris.storage.PredicateWritable,132=eu.larkc.iris.storage.StringTermWritable";
		defaultConfiguration.flowProperties.put(cascadingKey, cascadingValue);

		// Polling interval should speed up execution
		Flow.setJobPollingInterval(defaultConfiguration.flowProperties, 500);
		FlowConnector.setDebugLevel(defaultConfiguration.flowProperties,
				DebugLevel.VERBOSE);
		MultiMapReducePlanner.setJobConf(defaultConfiguration.flowProperties,
				jobConf);

		logger.debug("Flow properties: Done.");
	}

	private void setUpJobConfiguration(Configuration conf) throws IOException {
		logger.debug("Job configuration: Setting up...");

		if (!enableCluster) {
			jobConf = new JobConf();
		} else {
			System.setProperty("test.build.data", "build");
			new File("build/test/log").mkdirs();
			System.setProperty("hadoop.log.dir", "build/test/log");

			dfs = new MiniDFSCluster(conf, 1, true, null);
			fileSys = dfs.getFileSystem();
			mr = new MiniMRCluster(1, fileSys.getUri().toString(), 1);

			jobConf = mr.createJobConf();

			jobConf.set("mapred.child.java.opts", "-Xmx512m");
			jobConf.setMapSpeculativeExecution(false);
			jobConf.setReduceSpeculativeExecution(false);
		}

		jobConf.setBoolean("mapred.input.dir.recursive", true);
		jobConf.setNumMapTasks(numMapTasks);
		jobConf.setNumReduceTasks(numReduceTasks);

		logger.debug("Job configuration: Done.");
	}

	private void setUpEvaluationStrategy() {
		logger.debug("Evaluation strategy: Setting up...");

		defaultConfiguration = new eu.larkc.iris.Configuration();
		defaultConfiguration.evaluationStrategyFactory = new DistributedBottomUpEvaluationStrategyFactory(
				new DistributedEvaluatorFactory());

		logger.debug("Evaluation strategy: Done.");
	}

	public List<IAtom> evaluate() throws Exception {
		List<IAtom> evaluation = null;
		for (IQuery query : queries) {
			if (evaluation == null) {
				// First query, no previous evaluations are available
				evaluation = evaluate(query, new ArrayList<IVariable>(), defaultConfiguration);
			} else {
				// Consecutive queries, add to previous evaluation(s)
				evaluation.addAll(evaluate(query, new ArrayList<IVariable>(), defaultConfiguration));
			}
		}
		return evaluation;
	}
	
	public List<IAtom> evaluate(IQuery query) throws Exception {
		// Use default configuration.
		return evaluate(query, new ArrayList<IVariable>(), defaultConfiguration);
	}
	
	private List<IAtom> evaluate(IQuery query, List<IVariable> outputVariables,
			eu.larkc.iris.Configuration configuration) throws Exception {
		// Create strategy using factory.
		long begin = System.currentTimeMillis();
		//FIXME create a factory for the distributed environment without the facts parameter
		IEvaluationStrategy strategy = configuration.evaluationStrategyFactory
				.createEvaluator(rules, configuration); 

		// Since this is a bottom-up evaluation there is no actual result returned.
		// The facts are stored in the defaultConfiguration and will be
		// retrieved later
		strategy.evaluateQuery(query, outputVariables);

		duration = System.currentTimeMillis() - begin;
		logger.debug("Evaluation took {} ms.", duration);

		return retrieveResultAsAtoms();
	}
	
	private List<IAtom> retrieveResultAsAtoms() throws EvaluationException, IOException {
		CascadingRuleCompiler crc = new CascadingRuleCompiler(defaultConfiguration);
		IDistributedCompiledRule dcr = crc.compile(rules.get(0));
		dcr.evaluate(new EvaluationContext(1, 1, 1));
		FlowAssembly fa = dcr.getFlowAssembly();
		
		TupleEntryIterator tei = fa.openSink();

		List<IAtom> atoms = new LinkedList<IAtom>();
		while (tei.hasNext()) {
			TupleEntry te = tei.next();
			Tuple tuple = te.getTuple();
			logger.info("Tuple entry: {}", tuple.toString());
			IAtom atom = convertHadoopTupleToIrisAtom(tuple);
			logger.info("Converted to atom: {}", atom);
			atoms.add(atom);
		}
		
		return atoms;
	}

	private IAtom convertHadoopTupleToIrisAtom(Tuple tuple) {
		List<Object> elements = Tuple.elements(tuple);
		logger.info("Converting the following cascading tuple to an IRIS tuple: {}", elements.toString());
		
		IBasicFactory bf = BasicFactory.getInstance();

		// First tuple element is always the predicate
		Object rawPredicate = elements.get(0);
		String cleanRawPredicate = stripIriFromString(rawPredicate.toString());
		
		List<ITerm> terms = new LinkedList<ITerm>();
		for (Object rawTupleElement : elements) {
			if (rawTupleElement.equals(elements.get(0))) {
				// The first tuple element represents the predicate
				continue;
			}

			// All other tuple elements are variable bindings
			// Example: p(?x, ?y). 
			// If there are 3 tuple elements (p, 'x', 'y'), the first tuple element
			// represents the predicate name (p), the other 2 elements represent
			// the variable bindings of the predicate, e.g. values of ?x and ?y
			String cleanRawTupleElement = stripIriFromString(rawTupleElement.toString());
			IStringTerm term = TermFactory.getInstance().createString(cleanRawTupleElement);
			terms.add(term);
		}
		
		ITuple irisTuple = bf.createTuple(terms);
		
		// Create the predicate
		IPredicate irisPredicate = bf.createPredicate(cleanRawPredicate, elements.size() - 1);
		
		// Create the atom
		IAtom irisAtom = bf.createAtom(irisPredicate, irisTuple);
		
		return irisAtom;
	}
	
	private String stripIriFromString(String input) {
		return input.substring(4, input.length()-1);
	}
	
}
