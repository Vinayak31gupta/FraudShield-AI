@echo off
title FraudShield AI - Spring Boot Backend
echo ========================================================
echo Starting FraudShield AI Backend on http://localhost:8080
echo ========================================================
cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run
pause
