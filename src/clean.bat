@echo off

echo Clearing...

del *.class /s >nul

if NOT "%1"=="-nobaks" del *.bak /s >nul

@echo on
