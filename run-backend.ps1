# Run FraudShield AI Backend
Set-Location "$PSScriptRoot\backend"
Write-Host "Starting FraudShield AI Spring Boot Backend on http://localhost:8080..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run
