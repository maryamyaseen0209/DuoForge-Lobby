$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path build\classes | Out-Null
Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object { $_.FullName } | Set-Content build\sources.txt
& javac --release 21 -d build\classes "@build\sources.txt"
Copy-Item -Recurse -Force src\main\resources\* build\classes\
& jar --create --file build\duoforge-lobby.jar --main-class com.duoforge.lobby.DuoForgeApplication -C build\classes .
& java -jar build\duoforge-lobby.jar
