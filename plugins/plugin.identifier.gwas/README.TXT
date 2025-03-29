To compile the plugin you need to issue the command

  mvn assembly:assembly

This will generate the file

  plugin.GWASIdentifier-0.0.1-SNAPSHOT-LarkcPluginAssembly.jar

in the target directory. This file will then need to be copied into the plugins
folder of the platform.

There is currently a problem with library conflicts between the LarKC platform
and this plugin. The version of Log4J in the platform is out of date and
missing some methods. To get around this problem you can download a patched
copy of platform-2.5.0-SNAPSHOT-LarkcAssembly.jar (see below for ftp location).

The plugin requires access to some external data which is currently provided
via system properties. This requires editing the run_larkc startup script

java -Xmx1024M -Dgate.home=[path to gate-home] -Dmimir.index.dir=[path to mimir-indices] -jar target/platform-2.5.0-SNAPSHOT-LarkcAssembly.jar -plugindir=plugins

The gate.home property should be set to the location of the gate-home
directory included with the plugin. The mimir.index.dir should be set to the
location of the unpacked mimir-indices.tag.gz file (available via ftp). You
also need to increase the memory available to at least 1024M in order for the
plugin to work correctly.

Once the platform is running you then need to deploy the workflow available
in the gwas-workflow.rdf file.

You can then submit SPARQL queries to the end point in order to generate
a score for each SNP. The following shows an example query for a small
set of keywords ans SNP IDs.

   PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
   PREFIX gwas: <http://www.gate.ac.uk/gwas#>
   PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
   SELECT * WHERE {
    gwas:x rdf:type gwas:Experiment .
    gwas:x gwas:hasName "experiment1" .
    gwas:x gwas:hasKeywordGroup gwas:g1 .
    gwas:g1 gwas:hasKeyword "lung" .
    gwas:g1 gwas:hasKeyword "cancer" .
    gwas:x gwas:searchInRif "false" .
    gwas:x gwas:useUMLS "false" .
    gwas:x gwas:searchMode "1" .  
    gwas:x gwas:dateConstraint "20110412" . 
    gwas:x gwas:hasSnpId "rs1051730" .
    gwas:x gwas:hasSnpId "rs8034191" .
    gwas:x gwas:hasSnpId "rs3117582" .
    gwas:x gwas:hasSnpId "rs4324798" .
    gwas:x gwas:hasSnpId "rs401681" .
    gwas:x gwas:hasSnpId "rs2736100"
   }

Note that there currently seems to be a problem with the main LarKC web
interface which makes it impossible to submit SPARQL queries to an endpoint.
To get around this problem I've provided a very simple web interface in
larkc-gwas.html. Just load this file into a browser and edit the endpoint
field to match the deployed workflow.

All files mentioned above can be downloaded from the University of Sheffield's
anon_ftp server ftp.dcs.shef.ac.uk -- login with the username "ftp" and an
empty password. All the files mentioned above can then be found in /home/mark
