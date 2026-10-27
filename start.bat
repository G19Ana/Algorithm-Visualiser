@echo off
start "Algorithm Visualiser - Backend" cmd /k "cd /d "%~dp0backend" && "%USERPROFILE%\maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run"
start "Algorithm Visualiser - Frontend" cmd /k "cd /d "%~dp0frontend" && npm start"
timeout /t 6 /nobreak >nul
start http://localhost:4200
