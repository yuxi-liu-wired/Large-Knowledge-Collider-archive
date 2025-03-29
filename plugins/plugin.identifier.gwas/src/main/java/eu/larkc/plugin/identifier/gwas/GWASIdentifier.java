package eu.larkc.plugin.identifier.gwas;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

import eu.larkc.core.data.DataFactory;
import eu.larkc.core.data.SetOfStatements;
import eu.larkc.core.data.SetOfStatementsImpl;
import eu.larkc.core.query.SPARQLQuery;
import eu.larkc.plugin.Plugin;
import gate.lifesci.scoring.SNPKnowledge;
import gate.lifesci.scoring.gwas.KeywordPriorScoreForService;

/**
 * <p>
 * Generated LarKC plug-in skeleton for
 * <code>eu.larkc.plugin.identifier.gwas.GWASIdentifier</code>. Use this class
 * as an entry point for your plug-in development.
 * </p>
 */
public class GWASIdentifier extends Plugin {

  /**
   * Constructor.
   * 
   * @param pluginUri
   *          a URI representing the plug-in type, e.g.
   *          <code>eu.larkc.plugin.myplugin.MyPlugin</code>
   */
  public GWASIdentifier(URI pluginUri) {
    super(pluginUri);
    // TODO Auto-generated constructor stub
  }

  /**
   * Called on plug-in initialisation. The plug-in instances are initialised on
   * workflow initialisation.
   * 
   * @param workflowDescription
   *          set of statements containing plug-in specific information which
   *          might be needed for initialization (e.g. plug-in parameters).
   */
  @Override
  protected void initialiseInternal(SetOfStatements workflowDescription) {
    // TODO Auto-generated method stub
    logger.info("GWASIdentifier initialized. Hello World!");
  }

  /**
   * Called on plug-in invokation. The actual "work" should be done in this
   * method.
   * 
   * @param input
   *          a set of statements containing the input for this plug-in
   * @return a set of statements containing the output of this plug-in
   */
  @Override
  protected SetOfStatements invokeInternal(SetOfStatements input) {
    logger.info("GWASIdentifier working.");

    String mimirDir =
            System.getProperty("mimir.index.dir", "/data/lld/mimir-indices");

    // final GWASQuery query = new GWASQuery();
    
    SPARQLQuery sparqlQuery = DataFactory.INSTANCE.createSPARQLQuery(input);

    String original = sparqlQuery.toString();

    final GWASQuery query = new GWASQuery();

    Pattern p = Pattern.compile("([0-9])\\s+gwas:hasKeyword\\s+\"([^\"]+)\"");
    Matcher m = p.matcher(original);

    while(m.find()) {
      query.keywords.add(m.group(2) + ";" + m.group(1));
    }

    p = Pattern.compile("gwas:searchInRif\\s+\"([^\"]+)\"");
    m = p.matcher(original);

    if(m.find()) query.searchInRIF = Boolean.parseBoolean(m.group(1));

    p = Pattern.compile("useUMLS\\s+\"([^\"]+)\"");
    m = p.matcher(original);

    if(m.find()) query.useUMLS = Boolean.parseBoolean(m.group(1));

    p = Pattern.compile("searchMode\\s+\"([^\"]+)\"");
    m = p.matcher(original);

    if(m.find()) query.searchMode = Integer.parseInt(m.group(1));

    p = Pattern.compile("dateConstraint\\s+\"([^\"]+)\"");
    m = p.matcher(original);

    if(m.find()) query.dateConstraint = Integer.parseInt(m.group(1));

    p = Pattern.compile("gwas:hasSnpId\\s+\"([^\"]+)\"");
    m = p.matcher(original);

    while(m.find()) {
      query.snpIDs.add(m.group(1));
    }

    System.out.println("About to process: " + query.snpIDs.size());

    final Set<Statement> statements = new HashSet<Statement>();

    try {
      final KeywordPriorScoreForService kpsfs =
              new KeywordPriorScoreForService(
                      query.keywords.toArray(new String[]{}), true,
                      query.searchMode, query.dateConstraint);

      int threads = Math.max(2, Runtime.getRuntime().availableProcessors()) - 1;
      System.err.println("Using " + threads + " Threads");
      final CountDownLatch ct = new CountDownLatch(threads);

      long startTime = System.currentTimeMillis();

      File snpIndexDir = new File(mimirDir, "snp-index");
      final Iterator<String> it =
              (query.snpIDs.size() != 0
                      ? query.snpIDs.iterator()
                      : new LineIterator((new File(snpIndexDir,
                              "indexedSnps.txt")).toURI().toURL()));

      for(int i = 0; i < threads; ++i) {
        new Thread() {
          public void run() {
            SNPKnowledge bk = new SNPKnowledge(query.searchInRIF);
            try {
              String snp = getNextSNP(it);
              while(snp != null) {
                try {

                  System.out.println("Processing snp with id: " + snp);

                  bk.search(snp.substring(snp.lastIndexOf("/") + 1), false,
                          true, 0, -1, query.searchMode, query.dateConstraint);

                  double score = kpsfs.score(bk);

                  store(statements, snp, score);

                  snp = getNextSNP(it);

                } catch(Exception e) {
                  e.printStackTrace();
                }
              }
            } finally {
              ct.countDown();
            }
          }
        }.start();
      }

      ct.await();

      System.err.println("Finished " + statements.size() + " SNPs in "
              + (System.currentTimeMillis() - startTime) + " ms");

    } catch(Exception e) {
      e.printStackTrace();
    }

    return new SetOfStatementsImpl(statements);
  }

  /**
   * Called on plug-in destruction. Plug-ins are destroyed on workflow deletion.
   * Free an resources you might have allocated here.
   */
  @Override
  protected void shutdownInternal() {
    // TODO Auto-generated method stub
  }

  private synchronized String getNextSNP(Iterator<String> it) {
    try {
      if(!it.hasNext()) return null;
      return it.next();
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private synchronized void store(Set<Statement> statements, String snp,
          double score) {
    statements.add(new StatementImpl(new URIImpl(
            "http://linkedlifedata.com/resource/hapmap/snp/" + snp),
            new URIImpl("urn:snpHasScore"), new NumericLiteralImpl(score)));
  }
}
