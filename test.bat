@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 --release 17 -d out src\supportnote\*.java test\supportnote\*.java
if errorlevel 1 exit /b 1
java -cp out supportnote.SupportNoteTest
