@echo off
setlocal
if not exist build\classes mkdir build\classes
dir /s /b src\main\java\*.java > build\sources.txt
javac --release 21 -d build\classes @build\sources.txt
if errorlevel 1 exit /b 1
xcopy /E /I /Y src\main\resources build\classes >nul
jar --create --file build\duoforge-lobby.jar --main-class com.duoforge.lobby.DuoForgeApplication -C build\classes .
if errorlevel 1 exit /b 1
java -jar build\duoforge-lobby.jar
