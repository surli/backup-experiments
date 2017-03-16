#!/bin/sh
conf_file=${1:-"conf/macrobase.yaml"}

set -x

java ${JAVA_OPTS} -cp "legacy/target/classes:frontend/target/classes:frontend/src/main/resources/:contrib/target/classes:assembly/target/*:$CLASSPATH" macrobase.runtime.MacroBaseServer server $conf_file
