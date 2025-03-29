#!/bin/bash
# mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar

# org.semweb4j:rdf2go.api:jar:4.7.3:compile
# org.semweb4j:rdf2go.impl.sesame23:jar:4.7.3:compile
# hadoop-common:hadoop-common:jar:0.21.0:compile
# hadoop-mapred:hadoop-mapred:jar:0.21.0:compile
# hadoop-mapred:hadoop-mapred:jar:sources:0.21.0:compile
# hadoop-mapred-test:hadoop-mapred-test:jar:0.21.0:compile
# hadoop-hdfs:hadoop-hdfs:jar:0.21.0:compile
# hadoop-hdfs:hadoop-hdfs:jar:sources:0.21.0:compile
# hadoop-hdfs-test:hadoop-hdfs-test:jar:0.21.0:compile
# hadoop-hdfs-test:hadoop-hdfs-test:jar:sources:0.21.0:compile
# hadoop-common-test:hadoop-common-test:jar:0.21.0:compile
# org.openrdf:openrdf-sesame:jar:2.3.2:compile
# org.openrdf.sesame:sesame-runtime-osgi:jar:2.3.1:compile

# Hadoop
mvn install:install-file -Dfile=hadoop-common-0.21.0.jar -DgroupId=hadoop-common -DartifactId=hadoop-common -Dversion=0.21.0 -Dpackaging=jar
mvn install:install-file -Dfile=hadoop-common-test-0.21.0.jar -DgroupId=hadoop-common-test -DartifactId=hadoop-common-test -Dversion=0.21.0 -Dpackaging=jar
mvn install:install-file -Dfile=hadoop-hdfs-0.21.0.jar -DgroupId=hadoop-hdfs -DartifactId=hadoop-hdfs -Dversion=0.21.0 -Dpackaging=jar
mvn install:install-file -Dfile=hadoop-hdfs-0.21.0-sources.jar -DgroupId=hadoop-hdfs -DartifactId=hadoop-hdfs -Dversion=0.21.0 -Dpackaging=jar -Dclassifier=sources
mvn install:install-file -Dfile=hadoop-hdfs-test-0.21.0.jar -DgroupId=hadoop-hdfs-test -DartifactId=hadoop-hdfs-test -Dversion=0.21.0 -Dpackaging=jar
mvn install:install-file -Dfile=hadoop-hdfs-test-0.21.0-sources.jar -DgroupId=hadoop-hdfs-test -DartifactId=hadoop-hdfs-test -Dversion=0.21.0 -Dpackaging=jar -Dclassifier=sources
mvn install:install-file -Dfile=hadoop-mapred-0.21.0.jar -DgroupId=hadoop-mapred -DartifactId=hadoop-mapred -Dversion=0.21.0 -Dpackaging=jar
mvn install:install-file -Dfile=hadoop-mapred-0.21.0-sources.jar -DgroupId=hadoop-mapred -DartifactId=hadoop-mapred -Dversion=0.21.0 -Dpackaging=jar -Dclassifier=sources
mvn install:install-file -Dfile=hadoop-mapred-test-0.21.0.jar -DgroupId=hadoop-mapred-test -DartifactId=hadoop-mapred-test -Dversion=0.21.0 -Dpackaging=jar

# RDF2go
mvn install:install-file -Dfile=rdf2go.api-4.7.3.jar -DgroupId=org.semweb4j -DartifactId=rdf2go.api -Dversion=4.7.3 -Dpackaging=jar
mvn install:install-file -Dfile=rdf2go.impl.sesame23-4.7.3.jar -DgroupId=org.semweb4j -DartifactId=rdf2go.impl.sesame23 -Dversion=4.7.3 -Dpackaging=jar

# Sesame
mvn install:install-file -Dfile=openrdf-sesame-2.3.2.jar -DgroupId=org.openrdf -DartifactId=openrdf-sesame -Dversion=2.3.2 -Dpackaging=jar
mvn install:install-file -Dfile=sesame-runtime-osgi-2.3.1.jar -DgroupId=org.openrdf.sesame -DartifactId=sesame-runtime-osgi -Dversion=2.3.1 -Dpackaging=jar
