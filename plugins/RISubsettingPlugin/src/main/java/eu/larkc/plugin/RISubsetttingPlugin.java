//FIXME: Include licensing header
package eu.larkc.plugin;

import de.mpg.mpib.larkc.ri.utils.RandomIndexingSubsetting;
import eu.larkc.core.data.*;
import eu.larkc.core.data.workflow.WorkflowDescriptionPredicates;
import eu.larkc.core.qos.QoSInformation;
import eu.larkc.core.query.Query;
import eu.larkc.core.query.SPARQLQuery;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;





/**
 * The Random Indexing ubsettting Plugin gets a Sparql query as input, and does following:
 *  1) Extracts the keywords from that query, append them to a single string;
 *  2) Calls the method "getResultsUsingRandomIndexing(...)" in the class RandomIndexingSubsetting.java (external library)
 *  3) That method itself processes the query, creating RDF statements with the
 *     most similar documents found in the dbpedia semantic space;
 *  4) Adds all statements to a SetOfStatements object, returns that object to the
 *     calling class  ( this plugin )
 */
public class RISubsetttingPlugin extends Plugin {

    public RISubsetttingPlugin(URI pluginName) {
        super(pluginName);

    }

    private static Logger logger = LoggerFactory.getLogger(RISubsetttingPlugin.class);

    protected String titleIndexPath = "/tmp";
    protected String pathToTermvectors = "/tmp";
    protected String pathToDocvetors = "/tmp";
    protected int numberOfSimilarDocs = 10;


    private SetOfStatements getsimilarDocs(Query theQuery) {

        if (theQuery instanceof SPARQLQuery) {
            // extract keywords from input query
            Collection<String> keywords = SparqlToKeywords.extractKeywords(theQuery.toString());
            StringBuilder keywordConcatenated = new StringBuilder();

            // append all keywords to a single String ...
            for (Object o : keywords) {
                keywordConcatenated.append((String) o + " ");
            }
            List<Statement> statementsList = new ArrayList<Statement>();
            try {

                RandomIndexingSubsetting.setPathToDocvetors(pathToDocvetors);
                RandomIndexingSubsetting.setPathToTermvectors(pathToTermvectors);
                RandomIndexingSubsetting.setTitleIndexPath(titleIndexPath);
                RandomIndexingSubsetting.loadVectors();
             // call the external class "RandomIndexingSubsetting.java", which will search similar docs (in the wikipedia space)to the sentence we created from the keywords
                AtomicReferenceArray<String[]> result = new AtomicReferenceArray<String[]>(RandomIndexingSubsetting.getResultsUsingRandomIndexing(keywordConcatenated.toString(), numberOfSimilarDocs));


                if (null != result && result.length() > 0) {
                    logger.info("Created  " + result.length()
                            + " RDF Statements from the Query");
                    int count = 1;
                    for (int k = 0; k < result.length(); k++) {
                        String[] oneStatementAsString = result.get(k);
                        statementsList.add(new StatementImpl(
                                new URIImpl(oneStatementAsString[0]),
                                new URIImpl(oneStatementAsString[1]),
                                new URIImpl(oneStatementAsString[2])));
                        count++;
                    }
                    return new SetOfStatementsImpl(statementsList);

                } else {
                    logger.info("No docs were found.");
                }

                return null;

            } catch (Exception ex) {
                logger.error(ex.toString());
            }
        } else {
            //TODO: Throw exception
        }

        return null;
    }

    public URI getIdentifier() {
        return new URIImpl("urn:" + this.getClass().getName());
    }

    public QoSInformation getQoSInformation() {
        return new QoSInformation() {
        };
    }

    //For internal testing purposes only
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();


        logger.info("Starting RI Search...");


        RISubsetttingPlugin ri = new RISubsetttingPlugin(new URIImpl("urn:eu.larkc.plugin.RISubsetttingPlugin"));
                 ri.pathToTermvectors = "E:\\arbeit\\mpi\\fromInstitute\\frombulgariaserver\\larkc2.5\\termvectors.bin" ;
                ri.pathToDocvetors = "E:\\arbeit\\mpi\\fromInstitute\\frombulgariaserver\\larkc2.5\\docvectors.bin" ;
                 ri. titleIndexPath = "C:\\Users\\user\\work\\mpi\\corpora\\titles_index_march10";

        String sparqlQuery = "SELECT ?s ?p ?o" +
                " WHERE {" +
                "{ ?s ?p ?o . ?s ?p \"anarchism \"} }";

        logger.info("Using '" + sparqlQuery + "' as input");

        SPARQLQuery inputQuery = DataFactoryImpl.INSTANCE.createSPARQLQuery(sparqlQuery);

        logger.info("Searching similar docs in semantic space ...");

        SetOfStatements keywords = ri.invoke(inputQuery.toRDF());

        if (keywords != null) {
            CloseableIterator<Statement> it = keywords.getStatements();

            while (it.hasNext())
                logger.info(it.next().toString());
        }

        logger.info("Finished search in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    protected void initialiseInternal(SetOfStatements workflowDescription) {
        if (workflowDescription != null) {
            CloseableIterator<Statement> it = workflowDescription.getStatements();

            while (it.hasNext()) {
                Statement stmt = it.next();

                if (stmt != null) {
                    if (logger.isDebugEnabled())
                        logger.debug(stmt.toString());

                    if (stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC + "pathToTermvectors")) {
                        pathToTermvectors = stmt.getObject().stringValue();
                    } else if (stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC + "numberOfSimilarDocs")) {
                        numberOfSimilarDocs = Integer.parseInt(stmt.getObject().stringValue());
                    } else if (stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC + "pathToTermvectors")) {
                        pathToTermvectors = stmt.getObject().stringValue();
                    } else if (stmt.getPredicate().stringValue().equals(WorkflowDescriptionPredicates.LARKC + "titleIndexPath")) {
                        titleIndexPath = stmt.getObject().stringValue();
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("pathToTermvectors: " + pathToTermvectors);
            logger.debug("pathToDocvetors: " + pathToDocvetors);
            logger.debug("titleIndexPath: " + titleIndexPath);
            logger.debug("numberOfSimilarDocs: " + numberOfSimilarDocs);
        }
    }

    @Override
    protected SetOfStatements invokeInternal(SetOfStatements input) {
        // TODO Auto-generated method stub
        return getsimilarDocs(DataFactory.INSTANCE.createSPARQLQuery(input));
    }

    @Override
    protected void shutdownInternal() {
        // TODO Auto-generated method stub

    }
}
