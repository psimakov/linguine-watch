@ECHO OFF

REM
REM This is a sample file that shows how to run MySqlReplTest;
REM Review %OY_LW_HOME%, %MYSQL_REPL_PROPS% variables; edit 
REM the content of %MYSQL_REPL_PROPS% properties file to 
REM match your environment.
REM 

SET OY_LW_HOME=c:/mystuff/oy-lw-1.2
SET MYSQL_REPL_PROPS="%OY_LW_HOME%/src/com/oy/shared/lw/samples/mysqlrepl/MySqlReplTest.props"

SET MYSQL_REPL_CP="%OY_LW_HOME%/lib/oy-lw-1.1.jar";"%OY_LW_HOME%/lib/mysql-connector-java-3.0.14-production-bin.jar"
SET MYSQL_REPL_MAIN=com.oy.shared.lw.samples.mysqlrepl.MySqlReplTest
 
java -classpath %MYSQL_REPL_CP% %MYSQL_REPL_MAIN% %MYSQL_REPL_PROPS%