package eu.larkc.endpoint.sparql.exceptions;

public class SparqlQueryRefusedException extends SparqlException {

  private static final long serialVersionUID = 1L;

  public SparqlQueryRefusedException(String message) {
    super(message);
  }
}
