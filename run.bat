@echo off
cd /d "%~dp0src"
rem MySQL JDBC driver (standalone - no slf4j needed)
set CP=.;..\lib\mysql-connector-j-9.6.0.jar

echo Compiling...
javac -cp "%CP%" *.java
if errorlevel 1 exit /b 1

echo Running...
java -Ddb.password=%DB_PASSWORD% -cp "%CP%" EncoderGUI
