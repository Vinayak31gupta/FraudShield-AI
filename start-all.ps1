# FraudShield AI - PowerShell All-In-One Launcher
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "         FraudShield AI: AI-Powered Fraud Detection System            " -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

Write-Host "`n[1/2] Launching Spring Boot Backend on http://localhost:8080..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\backend'; .\mvnw.cmd spring-boot:run"

Write-Host "[2/2] Waiting for Spring Boot server initialization..." -ForegroundColor Green
Start-Sleep -Seconds 6

Write-Host "Opening FraudShield AI web interface in your default browser..." -ForegroundColor Yellow
Start-Process "http://localhost:8080/"

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "Application launched successfully!" -ForegroundColor Green
Write-Host " - Web Interface:        http://localhost:8080/"
Write-Host " - H2 Database Console:  http://localhost:8080/h2-console"
Write-Host " - Admin Credentials:    admin@fraudshield.ai / Admin@123"
Write-Host " - User Credentials:     john.doe@example.com / User@123"
Write-Host "======================================================================" -ForegroundColor Cyan
