echo off
CALL windowsVariables.bat
%JAVA_BIN%\java -classpath ..\interprolog.jar com.declarativa.interprolog.gui.SWISubprocessEngineWindow %1 %SWI_BIN_DIRECTORY%\swipl
rem pause
echo on
