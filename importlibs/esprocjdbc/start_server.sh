#!/bin/bash

export START_HOME="/opt/app/raqsoft"
export JAVA_HOME="$START_HOME/common"
export EXECJAVA="$START_HOME/common/jre/bin/java"
export EXECJAVAW="$START_HOME/common/jre/bin/javaw"

# parameter ip port
$EXECJAVA -Xms128m -Xmx8520m  -cp ./:./lib -Djava.ext.dirs=./lib:$START_HOME/common/jdbc -Dstart.home="$START_HOME/esProc" com.esproc.jdbc.EsprocJdbcServer $1 $2
