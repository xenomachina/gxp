#!/bin/sh
GXP_JAR="gxp-0.2.4-beta.jar"
set -ex
java -cp "$GXP_JAR" com.google.gxp.compiler.cli.Gxpc --output_language java com/google/tutorial/*.gxp
javac -cp "$GXP_JAR" com/google/tutorial/*.java
