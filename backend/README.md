# FraudShield AI — Backend Core

Enterprise Java 17+ & Spring Boot 3.3 REST API Service for Fraud Risk Scoring and AI Explanation.

## Architecture Highlights
- **Spring Boot 3.3.4 & Java 17+**: Robust REST endpoints for transactions, analysis, and administrative oversight.
- **Spring Security & Stateless JWT**: Bearer token authentication with role-based authorization (`ROLE_USER`, `ROLE_ADMIN`).
- **Modular FraudScoringEngine Interface**: Multi-factor scoring engine evaluating 8 distinct risk vectors, with an `MLScoringEngineAdapter` blueprint ready for future XGBoost / Random Forest models.
- **Dual AI Explanation Engine**:
  - `GeminiExplanationService`: Google Gemini GenAI API.
  - `RuleBasedExplanationService`: Resilient local fallback engine.
- **Dual-Mode Database**:
  - Embedded H2 database with automatic schema generation & sample data seeding (zero setup required).
  - Production MySQL 8.0+ configuration profile.

## How to Run Backend

### 1. Zero-Config (Default H2 mode)
From the `backend` directory:

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

The backend starts on:  
👉 **`http://localhost:8080/`**

### 2. Dedicated MySQL Mode
Start your MySQL server and run:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 3. Run Automated Tests
```powershell
.\mvnw.cmd test
```
