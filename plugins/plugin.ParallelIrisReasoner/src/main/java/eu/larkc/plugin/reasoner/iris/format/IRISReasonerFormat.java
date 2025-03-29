package eu.larkc.plugin.reasoner.iris.format;

/**
 * <p>
 * ENUM which holds all possible data input formats. Keep in mind that
 * internally, IRIS only uses DATALOG since it is a DATALOG reasoner and nothing
 * more.
 * </p>
 * 
 * @author Iker Larizgoitia
 * 
 */
/*
 * FIXME (README) I'm not even sure if we need this class. Can't the type be
 * determined automatically? ~Gigi
 */
public enum IRISReasonerFormat 
{
	DATALOG,
	
	RDF
}
