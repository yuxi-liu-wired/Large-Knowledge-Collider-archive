package larkcwizard;


public class PluginTemplates {

	public static enum LarKCPlugin { 
		IDENTIFIER (IDENTIFIER_NAME, IDENTIFIER_DESC, PLUGIN_SRC, "identify"),
		SELECTER ( SELECTER_NAME,SELECTER_DESC, PLUGIN_SRC, "select"), 
		TRANSFORMER(TRANSFORMER_NAME, TRANSFORMER_DESC, PLUGIN_SRC, "transform"), 
		INFOSET_TRANSFORMER(INFOSET_TRANSFORMER_NAME, INFOSET_TRANSFORMER_DESC, PLUGIN_SRC,"transform"), 
		REASONER(REASONER_NAME, REASONER_DESC, PLUGIN_SRC,"reason"), 
		DECIDER( DECIDER_NAME,DECIDER_DESC, PLUGIN_SRC,"decider");
		
		private String desc;
		private String src;
		private String defpackage;
		private String pluginName;
		
		LarKCPlugin(String _name, String _desc, String _src, String _package){
			desc=_desc;
			src=_src;
			defpackage= _package;
			pluginName = _name;
		}
		
		public String description(){
			return desc;
		}
		public String defaultPackage(){
			return "eu.larkc.plugin."+defpackage;
		}
		public String sourceTemplate(){
			return src;
		}
		public String pluginName(){
			return pluginName;
		}
		public String capitalPluginName(){
			StringBuilder newName = new StringBuilder();
			newName.append(Character.toUpperCase(pluginName.charAt(0)));
			newName.append(pluginName.substring(1));
	
			return newName.toString();
		}
	}
	
	final static String IDENTIFIER_NAME = "Identifier";
	final static String SELECTER_NAME = "Selecter";
	final static String TRANSFORMER_NAME = "InformationSetTransformer";
	final static String INFOSET_TRANSFORMER_NAME = "InformationSetTransformer";
	final static String REASONER_NAME = "Reasoner";
	final static String DECIDER_NAME = "Decider";
	
	final static String IDENTIFIER_DESC = "Identifier plug-in";
	final static String SELECTER_DESC = "Selecter plug-in";
	final static String TRANSFORMER_DESC = "Transformer plug-in";
	final static String INFOSET_TRANSFORMER_DESC = "Datatransformer plug-in";
	final static String REASONER_DESC = "Reasoner plug-in";
	final static String DECIDER_DESC = "Decider plug-in";
	
	public static String PLUGIN_SRC =
		
		"package DEFPACKAGE;\n\n" +

		"import org.openrdf.model.URI;\n\n" +
		
				
		"import eu.larkc.core.data.SetOfStatements;\n" +
		"import eu.larkc.plugin.Plugin;\n\n" +
	
		"/**\n" +
		" * This is an identifier template, created by LarKC plug-in Wizard\n" + 
		" * @author LarKC plug-in Wizard\n" +
		" *\n" +
		" */\n" +
		"public class TEMPLATE extends Plugin {\n\n" +
	
	
		"	public TEMPLATE(URI pluginName) {\n" +
		"		super(pluginName);\n" +
		"	}\n\n" +
	
	
		"	@Override\n" +
		"	protected void initialiseInternal(SetOfStatements workflowDescription) {\n" +
		"	}\n\n" +
	
		"	public SetOfStatements invokeInternal(SetOfStatements input) {\n" +
		"		// TODO Auto-generated method stub\n" +
	
		"		// the plug-in code\n" +
		"		System.out.println(\"Hello from identifier!\");\n\n" +
	
		"		return input;\n" +
		"	}\n\n" +
	
		"   @Override \n" +
		"	public void shutdownInternal() {\n" +
		"	}\n\n" +
		
		"}";
		
		
	public static String WSDL =
		"<wsdl:description>\n"+
		"<wsdl:interface name=\"plugin_type\"\n"+
		"	sawsdl:modelReference=\"http://larkc.eu/plugin#Plugin_type\">\n"+
		"</wsdl:interface>\n"+
	
		"<wsdl:binding name=\"larkcbinding\" type=\"http://larkc.eu/wsdl-binding\" />\n"+

		"<!-- SPECIFIC TO THIS PLUGIN_TYPE -->\n"+
		"<wsdl:service name=\"urn:PACKAGE\" interface=\"plugin_type\"\n"+
		"sawsdl:modelReference=\"http://larkc.eu/plugin#TEMPLATE\" >\n"+
		"    <wsdl:endpoint location=\"java:PACKAGE\" />\n"+
		"</wsdl:service>\n"+
		"</wsdl:description>\n";
	
	public static String RDF =
		"@prefix larkc: <http://larkc.eu/plugin#> .\n"+
		"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"+
		"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"+

		"	larkc:TEMPLATE \n"+
		"		rdf:type            rdfs:Class ;\n"+
		"		rdfs:subClassOf     larkc:Plugin_type .\n";
	

	public static String getPluginSource(LarKCPlugin pType, String _name) {
		String sSource = pType.sourceTemplate().replaceAll("TEMPLATE", _name);
		
		return sSource.replaceAll("DEFPACKAGE", pType.defaultPackage());
	}
	
	public static String getPluginWsdl(LarKCPlugin pType, String _name) {
		String sWSDL = WSDL.replaceAll("TEMPLATE", _name);
		sWSDL = sWSDL.replaceAll("Plugin_type", pType.capitalPluginName());
		sWSDL = sWSDL.replaceAll("plugin_type", pType.pluginName());
		return sWSDL.replaceAll("PACKAGE", pType.defaultPackage()+"."+_name);
	}
	
	public static String getPluginRdf(LarKCPlugin pType, String _name) {
		String sWSDL = RDF.replaceAll("TEMPLATE", _name);
		sWSDL = sWSDL.replaceAll("Plugin_type", pType.capitalPluginName());
		return sWSDL.replaceAll("plugin_type", pType.pluginName());
	}
		
}
