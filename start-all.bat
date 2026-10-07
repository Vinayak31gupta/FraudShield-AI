@echo off
title FraudShield AI - All-In-One Launcher
echo ======================================================================
echo          FraudShield AI: AI-Powered Fraud Detection System            
echo ======================================================================
echo [1/2] Launching Spring Boot Backend (Port 8080)...
start "FraudShield AI Backend" cmd /k "cd /d ""%~dp0backend"" && mvnw.cmd spring-boot:run"

echo [2/2] Waiting for backend to initialize before opening browser...
timeout /t 6 /nobreak >nul

echo Opening FraudShield AI web interface...
start http://localhost:8080/

echo ======================================================================
echo Application launched!
echo - Web Dashboard: http://localhost:8080/
echo - H2 Database Console: http://localhost:8080/h2-console
echo - Default Admin: admin@fraudshield.ai / Admin@123
echo - Default User:  john.doe@example.com / User@123
echo ======================================================================
timeout /t 5 >nul
