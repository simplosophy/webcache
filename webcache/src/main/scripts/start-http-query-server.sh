#!/usr/bin/bash
WEBCACHE_CLASSPATH=$WEBCACHE_HOME/classes:$WEBCACHE_HOME/lib/jcl-over-slf4j-1.6.1.jar:$WEBCACHE_HOME/lib/logback-classic-1.0.1.jar:$WEBCACHE_HOME/lib/logback-core-1.0.1.jar:$WEBCACHE_HOME/lib/slf4j-api-1.6.1.jar:$WEBCACHE_HOME/lib/netty-3.4.2.Final.jar:$WEBCACHE_HOME/lib/protobuf-java-2.4.1.jar:$WEBCACHE_HOME/lib/fastjson-1.1.17.jar:$WEBCACHE_HOME/lib/gson-2.2.2.jar
$JAVA_HOME/bin/java -Xms20G -Xmx20G -classpath $WEBCACHE_CLASSPATH com.oneboxtech.se.webcache.service.HttpQueryServer
