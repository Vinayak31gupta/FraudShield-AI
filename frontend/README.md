# FraudShield AI — Frontend Client

Modern, responsive fintech & cybersecurity web interface for the AI-Powered Fraud Detection System.

## Features
- **Modern Fintech & Cybersecurity Aesthetics**: Dark slate luxury theme with glassmorphism, subtle glowing neon borders, and micro-interactions.
- **Visual Analytics with Chart.js**: Safe vs. Fraudulent Donut, Risk Level Breakdown, 7-Day Fraud Risk Trend, and Amount vs. Risk Correlation.
- **One-Click Viva Simulation Presets**:
  - `✓ Safe Scenario ($42.50)`
  - `⚠ Medium Risk Scenario ($750.00)`
  - `✕ High Risk Fraud Scenario ($4,850.00)`
- **Dynamic Risk Score Gauge**: Animated circular SVG gauge (0–100) with color transitions.
- **Printable Incident Reports**: Export clean PDF/Print reports for audit records.

## How to Run Frontend

### Option 1: Double Click or Open in Any Browser
Double-click `index.html` to open it directly in Chrome, Edge, or Firefox.  
*(It automatically detects `file://` or non-8080 ports and communicates with the backend at `http://localhost:8080/api`)*.

### Option 2: VS Code Live Server
1. Open the `frontend` folder in VS Code.
2. Right click on `index.html` and select **"Open with Live Server"** (usually runs on `http://127.0.0.1:5500`).

### Option 3: Python Simple Server
```bash
cd frontend
python -m http.server 3000
```
Open `http://localhost:3000`.

### Option 4: Node / npx http-server
```bash
npx -y http-server frontend -p 3000
```
