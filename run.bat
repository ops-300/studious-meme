@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 --release 17 -d out src\supportnote\*.java
if errorlevel 1 (
 echo コンパイルに失敗しました。JDK 17以降の設定を確認してください。
 pause
 exit /b 1
)
java -cp out supportnote.App %*
pause
