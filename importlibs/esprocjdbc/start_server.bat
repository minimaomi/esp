@echo off
set START_HOME="D:\Program Files\raqsoft"
set JAVA_HOME="D:\Program Files\raqsoft\common"
set EXECJAVA="D:\Program Files\raqsoft\common\jre\bin\java"
set EXECJAVAW="D:\Program Files\raqsoft\common\jre\bin\javaw"

rem parameter ip port
start "dm" %EXECJAVA% -Xms128m -Xmx8520m  -cp .;lib; -Djava.ext.dirs=./lib;%START_HOME%\common\jdbc; -Dstart.home=%START_HOME%\esProc com.esproc.jdbc.EsprocJdbcServer %1 %2
