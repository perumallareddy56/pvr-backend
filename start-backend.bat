@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
echo Loading environment variables from .env...
if exist .env (
    for /f "usebackq delims=" %%x in (".env") do set %%x
)
echo Starting PVR Prime Naturals Backend (CLEAN BUILD)
"C:\Users\DELL\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd" -B clean spring-boot:run
