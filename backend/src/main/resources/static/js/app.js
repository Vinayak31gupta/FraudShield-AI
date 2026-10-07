/**
 * FraudShield AI - Full-Stack Client Application Logic
 * Integrates REST APIs, JWT Security, Dynamic SPA Routing, Chart.js, and Live Scenarios
 */

// ============================================================================
// BACKEND API CONFIGURATION
// ============================================================================
// 1. For LOCAL development: Leave BACKEND_API_BASE_URL as '' (empty).
//    The client will automatically route requests to http://localhost:8080/api.
//
// 2. For PRODUCTION / VERCEL deployment:
//    Paste your deployed Spring Boot backend URL into BACKEND_API_BASE_URL below.
//    Example: const BACKEND_API_BASE_URL = 'https://fraudshield-backend.onrender.com';
//
// 3. Dynamic Override (Optional):
//    You can also set the backend URL directly in the browser DevTools console:
//    localStorage.setItem('fs_api_base', 'https://your-backend-url.com');
// ============================================================================
const BACKEND_API_BASE_URL = 'https://qewwp-2401-9640-1002-ac5a-2-2-2-2.run.pinggy-free.link'; // <-- Put your deployed Spring Boot URL here

const API_BASE = (() => {
    // 1. Check explicit production backend URL configuration first
    if (BACKEND_API_BASE_URL && BACKEND_API_BASE_URL.trim() !== '') {
        const cleaned = BACKEND_API_BASE_URL.trim().replace(/\/+$/, '');
        return cleaned.endsWith('/api') ? cleaned : `${cleaned}/api`;
    }

    // 2. Check dynamic runtime override from localStorage
    const runtimeOverride = localStorage.getItem('fs_api_base');
    if (runtimeOverride && runtimeOverride.trim() !== '') {
        const cleaned = runtimeOverride.trim().replace(/\/+$/, '');
        return cleaned.endsWith('/api') ? cleaned : `${cleaned}/api`;
    }

    // 3. Local Development / Same-origin fallback:
    // If frontend is bundled or served directly from Spring Boot port 8080
    if (window.location.origin && window.location.origin.includes(':8080')) {
        return '/api';
    }

    // 4. Default standalone frontend running on local machine (e.g., Live Server, localhost:3000, or file://)
    return 'http://localhost:8080/api';
})();

console.log('[FraudShield AI] Active Backend API Base:', API_BASE);

const App = {
    state: {
        token: localStorage.getItem('fs_token') || null,
        user: JSON.parse(localStorage.getItem('fs_user') || 'null'),
        currentView: 'landing',
        lastAnalysis: null,
        charts: {}
    },

    init() {
        this.bindEvents();
        this.checkAuth();
        this.handleHashChange();
        window.addEventListener('hashchange', () => this.handleHashChange());
    },

    bindEvents() {
        // Quick Scenario Presets
        const btnSafe = document.getElementById('btn-preset-safe');
        const btnMedium = document.getElementById('btn-preset-medium');
        const btnHigh = document.getElementById('btn-preset-high');

        if (btnSafe) btnSafe.addEventListener('click', () => this.loadScenario('safe'));
        if (btnMedium) btnMedium.addEventListener('click', () => this.loadScenario('medium'));
        if (btnHigh) btnHigh.addEventListener('click', () => this.loadScenario('high'));

        // Transaction Analysis Form
        const formTxn = document.getElementById('transaction-form');
        if (formTxn) {
            formTxn.addEventListener('submit', (e) => this.handleTransactionSubmit(e));
        }

        // Auth Form Submissions
        const formLogin = document.getElementById('form-login');
        if (formLogin) {
            formLogin.addEventListener('submit', (e) => this.handleLogin(e));
        }

        const formRegister = document.getElementById('form-register');
        if (formRegister) {
            formRegister.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Quick Credentials Buttons
        const quickUser = document.getElementById('btn-quick-user');
        const quickAdmin = document.getElementById('btn-quick-admin');
        if (quickUser) quickUser.addEventListener('click', () => this.fillLogin('john.doe@example.com', 'User@123'));
        if (quickAdmin) quickAdmin.addEventListener('click', () => this.fillLogin('admin@fraudshield.ai', 'Admin@123'));
    },

    async checkAuth() {
        if (!this.state.token) {
            this.updateAuthUI(false);
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/auth/me`, {
                headers: this.getAuthHeaders()
            });

            if (res.ok) {
                const user = await res.json();
                this.state.user = user;
                localStorage.setItem('fs_user', JSON.stringify(user));
                this.updateAuthUI(true);
            } else {
                this.logout(false);
            }
        } catch (e) {
            console.error('Failed to verify authentication token:', e);
            this.updateAuthUI(false);
        }
    },

    getAuthHeaders() {
        const headers = { 'Content-Type': 'application/json' };
        if (this.state.token) {
            headers['Authorization'] = `Bearer ${this.state.token}`;
        }
        return headers;
    },

    handleHashChange() {
        const hash = window.location.hash.replace('#', '') || 'landing';
        this.navigate(hash);
    },

    navigate(view) {
        // Auth Guards
        const protectedViews = ['dashboard', 'analyze', 'result', 'history', 'profile'];
        if (protectedViews.includes(view) && !this.state.token) {
            this.showToast('Please sign in to access this feature.', 'info');
            this.openAuthModal('login');
            return;
        }

        if (view === 'admin') {
            if (!this.state.token) {
                this.showToast('Please sign in with an Administrator account.', 'info');
                this.openAuthModal('login');
                return;
            }
            if (this.state.user?.role !== 'ROLE_ADMIN') {
                this.showToast('Access denied: Administrator privileges required.', 'error');
                this.navigate('dashboard');
                return;
            }
        }

        // Hide all views
        document.querySelectorAll('.app-view').forEach(el => el.style.display = 'none');
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

        const targetView = document.getElementById(`view-${view}`);
        if (targetView) {
            targetView.style.display = 'block';
            this.state.currentView = view;
            window.location.hash = view;

            // Highlight nav link
            const navLink = document.querySelector(`.nav-item[data-view="${view}"]`);
            if (navLink) navLink.classList.add('active');

            // View-specific loader
            if (view === 'dashboard') this.loadDashboardData();
            if (view === 'history') this.loadTransactionHistory();
            if (view === 'admin') this.loadAdminData();
            if (view === 'analyze' && !document.getElementById('txn-ref').value) this.generateRandomRef();
            if (view === 'profile') this.loadProfileData();

            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    },

    updateAuthUI(isAuthenticated) {
        const authContainer = document.getElementById('nav-auth-container');
        if (!authContainer) return;

        if (isAuthenticated && this.state.user) {
            const isAdmin = this.state.user.role === 'ROLE_ADMIN';
            authContainer.innerHTML = `
                <div class="user-menu-btn" onclick="App.navigate('profile')">
                    <div class="user-avatar">${this.state.user.fullName.charAt(0).toUpperCase()}</div>
                    <span style="font-size: 0.88rem; font-weight: 600;">${this.state.user.fullName}</span>
                    <span class="badge ${isAdmin ? 'badge-admin' : 'badge-safe'} badge-pill">${isAdmin ? 'ADMIN' : 'USER'}</span>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="App.logout()">Logout</button>
            `;

            // Admin Link visibility
            const adminNavItem = document.getElementById('nav-admin-link');
            if (adminNavItem) adminNavItem.style.display = isAdmin ? 'flex' : 'none';
        } else {
            authContainer.innerHTML = `
                <button class="btn btn-secondary btn-sm" onclick="App.openAuthModal('login')">Sign In</button>
                <button class="btn btn-primary btn-sm" onclick="App.openAuthModal('register')">Get Started</button>
            `;
            const adminNavItem = document.getElementById('nav-admin-link');
            if (adminNavItem) adminNavItem.style.display = 'none';
        }
    },

    // Authentication Handlers
    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.message || 'Login failed');
            }

            const data = await res.json();
            this.setSession(data);
            this.closeAuthModal();
            this.showToast(`Welcome back, ${data.fullName}!`, 'success');
            this.navigate('dashboard');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const fullName = document.getElementById('reg-name').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;
        const role = document.getElementById('reg-role').value;

        try {
            const res = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullName, email, password, role })
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.message || 'Registration failed');
            }

            const data = await res.json();
            this.setSession(data);
            this.closeAuthModal();
            this.showToast('Account successfully created!', 'success');
            this.navigate('dashboard');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    setSession(authData) {
        this.state.token = authData.token;
        this.state.user = {
            email: authData.email,
            fullName: authData.fullName,
            role: authData.role
        };
        localStorage.setItem('fs_token', authData.token);
        localStorage.setItem('fs_user', JSON.stringify(this.state.user));
        this.updateAuthUI(true);
    },

    logout(showNotice = true) {
        this.state.token = null;
        this.state.user = null;
        localStorage.removeItem('fs_token');
        localStorage.removeItem('fs_user');
        this.updateAuthUI(false);
        if (showNotice) this.showToast('You have been logged out.', 'info');
        this.navigate('landing');
    },

    fillLogin(email, password) {
        document.getElementById('login-email').value = email;
        document.getElementById('login-password').value = password;
    },

    openAuthModal(tab = 'login') {
        const modal = document.getElementById('auth-modal');
        if (modal) {
            modal.classList.add('active');
            this.switchAuthTab(tab);
        }
    },

    closeAuthModal() {
        const modal = document.getElementById('auth-modal');
        if (modal) modal.classList.remove('active');
    },

    switchAuthTab(tab) {
        document.querySelectorAll('.auth-tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.auth-tab-content').forEach(c => c.style.display = 'none');

        const btn = document.getElementById(`tab-btn-${tab}`);
        const content = document.getElementById(`auth-content-${tab}`);
        if (btn) btn.classList.add('active');
        if (content) content.style.display = 'block';
    },

    // Transaction & Fraud Submission
    async handleTransactionSubmit(e) {
        e.preventDefault();
        const submitBtn = document.getElementById('btn-submit-txn');
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <svg class="animate-spin" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="animation: spin 1s linear infinite;"><circle cx="12" cy="12" r="10" stroke-opacity="0.25"></circle><path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"></path></svg>
            Analyzing Risk Indicators...
        `;

        const payload = {
            transactionReference: document.getElementById('txn-ref').value,
            amount: parseFloat(document.getElementById('txn-amount').value),
            currency: document.getElementById('txn-currency').value,
            transactionType: document.getElementById('txn-type').value,
            merchantCategory: document.getElementById('txn-merchant').value,
            location: document.getElementById('txn-location').value,
            usualLocation: document.getElementById('txn-usual-location').value,
            deviceType: document.getElementById('txn-device').value,
            isNewDevice: document.getElementById('txn-new-device').checked,
            failedAttempts: parseInt(document.getElementById('txn-failed-attempts').value) || 0,
            accountAgeDays: parseInt(document.getElementById('txn-account-age').value) || 30,
            transactionFrequency: parseInt(document.getElementById('txn-frequency').value) || 1,
            ipAddress: document.getElementById('txn-ip').value || '127.0.0.1'
        };

        try {
            // 1. Create Transaction
            const txnRes = await fetch(`${API_BASE}/transactions`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify(payload)
            });

            if (!txnRes.ok) {
                const err = await txnRes.json();
                throw new Error(err.message || 'Transaction creation failed');
            }

            const txnData = await txnRes.json();

            // 2. Trigger Fraud Risk Analysis & AI Explanation
            const analysisRes = await fetch(`${API_BASE}/fraud/analyze/${txnData.id}`, {
                method: 'POST',
                headers: this.getAuthHeaders()
            });

            if (!analysisRes.ok) {
                const err = await analysisRes.json();
                throw new Error(err.message || 'Fraud analysis failed');
            }

            const analysisData = await analysisRes.json();
            this.state.lastAnalysis = analysisData;
            this.renderAnalysisResult(analysisData, txnData);
            this.navigate('result');
            this.showToast('Fraud risk assessment completed!', 'success');
        } catch (err) {
            this.showToast(err.message, 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = `
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                Analyze Transaction
            `;
        }
    },

    renderAnalysisResult(analysis, txn) {
        document.getElementById('res-txn-id').innerText = analysis.transactionReference;
        document.getElementById('res-score-num').innerText = analysis.riskScore;
        document.getElementById('res-probability').innerText = `${analysis.fraudProbability}%`;

        // Update Gauge
        const circle = document.getElementById('res-gauge-circle');
        const score = analysis.riskScore;
        const circumference = 2 * Math.PI * 80; // r=80
        const offset = circumference - (score / 100) * circumference;

        let strokeColor = 'var(--safe-green)';
        let badgeClass = 'badge-safe';

        if (score > 70) {
            strokeColor = 'var(--danger-red)';
            badgeClass = 'badge-high';
        } else if (score > 30) {
            strokeColor = 'var(--warning-amber)';
            badgeClass = 'badge-medium';
        }

        circle.style.strokeDasharray = `${circumference} ${circumference}`;
        circle.style.strokeDashoffset = offset;
        circle.style.stroke = strokeColor;
        document.getElementById('res-score-num').style.color = strokeColor;

        // Risk Level & Status
        const levelBadge = document.getElementById('res-risk-level-badge');
        levelBadge.className = `badge ${badgeClass}`;
        levelBadge.innerText = `${analysis.riskLevel} RISK`;

        const statusBadge = document.getElementById('res-status-badge');
        statusBadge.className = `badge ${badgeClass}`;
        statusBadge.innerText = analysis.status.replace(/_/g, ' ');

        // Recommendation & AI Explanation
        document.getElementById('res-recommendation').innerText = analysis.recommendation;
        document.getElementById('res-ai-explanation').innerText = `"${analysis.aiExplanation}"`;
        document.getElementById('res-engine-tag').innerText = `Engine: ${analysis.engineType}`;

        // Risk Factors Checklist
        const factorsContainer = document.getElementById('res-factors-list');
        if (analysis.riskFactors && analysis.riskFactors.length > 0) {
            factorsContainer.innerHTML = analysis.riskFactors.map(f => `
                <div class="factor-item">
                    <div class="factor-left">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="${f.severity === 'HIGH' ? 'var(--danger-red)' : f.severity === 'MEDIUM' ? 'var(--warning-amber)' : 'var(--safe-green)'}" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                        <div>
                            <div style="font-weight: 600; font-size: 0.92rem;">${f.description}</div>
                            <div class="mono" style="font-size: 0.72rem; color: var(--text-dim);">${f.factorCode}</div>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span class="badge ${f.severity === 'HIGH' ? 'badge-high' : f.severity === 'MEDIUM' ? 'badge-medium' : 'badge-safe'} badge-pill">${f.severity}</span>
                        <span class="mono" style="font-weight: 700; color: var(--text-muted); font-size: 0.85rem;">+${f.weightContribution} pts</span>
                    </div>
                </div>
            `).join('');
        } else {
            factorsContainer.innerHTML = `
                <div class="factor-item" style="color: var(--safe-green);">
                    <div class="factor-left">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                        <span>No anomalous behavioral risk factors detected. Standard parameters satisfied.</span>
                    </div>
                </div>
            `;
        }
    },

    // Presets Loader
    loadScenario(type) {
        this.generateRandomRef();
        if (type === 'safe') {
            document.getElementById('txn-amount').value = '42.50';
            document.getElementById('txn-type').value = 'PURCHASE';
            document.getElementById('txn-merchant').value = 'RETAIL';
            document.getElementById('txn-location').value = 'San Francisco, USA';
            document.getElementById('txn-usual-location').value = 'San Francisco, USA';
            document.getElementById('txn-device').value = 'iPhone 15 Pro';
            document.getElementById('txn-new-device').checked = false;
            document.getElementById('txn-failed-attempts').value = '0';
            document.getElementById('txn-account-age').value = '365';
            document.getElementById('txn-frequency').value = '1';
            document.getElementById('txn-ip').value = '192.168.1.45';
            this.showToast('Loaded: Safe Everyday Transaction Preset', 'info');
        } else if (type === 'medium') {
            document.getElementById('txn-amount').value = '750.00';
            document.getElementById('txn-type').value = 'PURCHASE';
            document.getElementById('txn-merchant').value = 'ELECTRONICS';
            document.getElementById('txn-location').value = 'Seattle, USA';
            document.getElementById('txn-usual-location').value = 'San Francisco, USA';
            document.getElementById('txn-device').value = 'MacBook Pro';
            document.getElementById('txn-new-device').checked = false;
            document.getElementById('txn-failed-attempts').value = '1';
            document.getElementById('txn-account-age').value = '180';
            document.getElementById('txn-frequency').value = '3';
            document.getElementById('txn-ip').value = '73.189.42.10';
            this.showToast('Loaded: Medium Risk (Location & Spike) Preset', 'info');
        } else if (type === 'high') {
            document.getElementById('txn-amount').value = '4850.00';
            document.getElementById('txn-type').value = 'ONLINE_TRANSFER';
            document.getElementById('txn-merchant').value = 'CRYPTO_EXCHANGE';
            document.getElementById('txn-location').value = 'London, UK';
            document.getElementById('txn-usual-location').value = 'San Francisco, USA';
            document.getElementById('txn-device').value = 'Linux Terminal Workstation';
            document.getElementById('txn-new-device').checked = true;
            document.getElementById('txn-failed-attempts').value = '4';
            document.getElementById('txn-account-age').value = '12';
            document.getElementById('txn-frequency').value = '7';
            document.getElementById('txn-ip').value = '185.220.101.4';
            this.showToast('Loaded: High-Risk Account Takeover Fraud Preset', 'info');
        }
    },

    generateRandomRef() {
        const refInput = document.getElementById('txn-ref');
        if (refInput) {
            refInput.value = 'TXN-' + Math.floor(100000 + Math.random() * 900000);
        }
    },

    // User Dashboard Data & Chart.js Integration
    async loadDashboardData() {
        try {
            const res = await fetch(`${API_BASE}/transactions/dashboard-stats`, {
                headers: this.getAuthHeaders()
            });

            if (!res.ok) throw new Error('Failed to load dashboard metrics');
            const data = await res.json();

            // Populate Stats Cards
            document.getElementById('dash-total-txns').innerText = data.totalTransactions;
            document.getElementById('dash-safe-txns').innerText = data.safeTransactions;
            document.getElementById('dash-medium-txns').innerText = data.mediumRiskTransactions;
            document.getElementById('dash-high-txns').innerText = data.highRiskTransactions;
            document.getElementById('dash-avg-score').innerText = `${data.averageRiskScore} / 100`;

            // Populate Recent Transactions Table
            const tbody = document.getElementById('dash-recent-tbody');
            if (tbody && data.recentTransactions) {
                tbody.innerHTML = data.recentTransactions.map(t => `
                    <tr>
                        <td class="mono" style="font-weight: 600; color: var(--neon-cyan);">${t.transactionReference}</td>
                        <td>${new Date(t.createdAt).toLocaleDateString()} ${new Date(t.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
                        <td class="mono" style="font-weight: 700;">$${t.amount.toFixed(2)}</td>
                        <td>${t.location}</td>
                        <td><span style="font-size: 0.85rem; color: var(--text-muted);">${t.deviceType}</span></td>
                        <td>
                            <div style="display: flex; align-items: center; gap: 0.5rem;">
                                <span class="mono" style="font-weight: 700;">${t.riskScore !== null ? t.riskScore : '-'}</span>
                                <div style="width: 45px; height: 6px; background: rgba(255,255,255,0.1); border-radius: 3px; overflow: hidden;">
                                    <div style="width: ${t.riskScore || 0}%; height: 100%; background: ${t.riskScore > 70 ? 'var(--danger-red)' : t.riskScore > 30 ? 'var(--warning-amber)' : 'var(--safe-green)'}"></div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <span class="badge ${t.riskLevel === 'HIGH' ? 'badge-high' : t.riskLevel === 'MEDIUM' ? 'badge-medium' : 'badge-safe'} badge-pill">
                                ${t.riskLevel || 'UNSCORED'}
                            </span>
                        </td>
                        <td>
                            <span class="badge ${t.status === 'BLOCKED' ? 'badge-blocked' : t.status === 'FLAGGED_FOR_REVIEW' ? 'badge-review' : 'badge-approved'} badge-pill">
                                ${t.status.replace(/_/g, ' ')}
                            </span>
                        </td>
                        <td>
                            <button class="btn btn-secondary btn-sm" onclick="App.viewTransactionModal(${t.id})">Details</button>
                        </td>
                    </tr>
                `).join('');
            }

            // Render Charts
            this.renderDashboardCharts(data);
        } catch (e) {
            console.error(e);
            this.showToast('Unable to refresh dashboard metrics.', 'error');
        }
    },

    renderDashboardCharts(data) {
        // Destroy existing instances to prevent ghost canvas artifacts
        if (this.state.charts.safeDoughnut) this.state.charts.safeDoughnut.destroy();
        if (this.state.charts.riskBar) this.state.charts.riskBar.destroy();
        if (this.state.charts.trendLine) this.state.charts.trendLine.destroy();
        if (this.state.charts.amountAnalysis) this.state.charts.amountAnalysis.destroy();

        // 1. Safe vs Fraud Donut
        const ctxDoughnut = document.getElementById('chart-safe-donut');
        if (ctxDoughnut) {
            this.state.charts.safeDoughnut = new Chart(ctxDoughnut, {
                type: 'doughnut',
                data: {
                    labels: ['Safe / Approved', 'Medium Risk', 'High Risk / Fraud'],
                    datasets: [{
                        data: [data.safeTransactions, data.mediumRiskTransactions, data.highRiskTransactions],
                        backgroundColor: ['#10B981', '#F59E0B', '#EF4444'],
                        borderWidth: 0,
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'bottom', labels: { color: '#94A3B8', font: { family: 'Inter' } } }
                    },
                    cutout: '72%'
                }
            });
        }

        // 2. Risk Level Distribution Bar
        const ctxBar = document.getElementById('chart-risk-dist');
        if (ctxBar) {
            this.state.charts.riskBar = new Chart(ctxBar, {
                type: 'bar',
                data: {
                    labels: ['Low (0-30)', 'Medium (31-70)', 'High (71-100)'],
                    datasets: [{
                        label: 'Evaluated Transactions',
                        data: [data.safeTransactions, data.mediumRiskTransactions, data.highRiskTransactions],
                        backgroundColor: ['rgba(16, 185, 129, 0.75)', 'rgba(245, 158, 11, 0.75)', 'rgba(239, 68, 68, 0.75)'],
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { display: false }, ticks: { color: '#94A3B8' } },
                        y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94A3B8', stepSize: 1 } }
                    }
                }
            });
        }

        // 3. 7-Day Fraud Trend Line
        const ctxLine = document.getElementById('chart-fraud-trend');
        if (ctxLine && data.monthlyTrends) {
            const labels = Object.keys(data.monthlyTrends);
            const values = Object.values(data.monthlyTrends);

            this.state.charts.trendLine = new Chart(ctxLine, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Average Risk Score',
                        data: values,
                        borderColor: '#06B6D4',
                        backgroundColor: 'rgba(6, 182, 212, 0.1)',
                        fill: true,
                        tension: 0.4,
                        pointBackgroundColor: '#06B6D4',
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { display: false }, ticks: { color: '#94A3B8' } },
                        y: { min: 0, max: 100, grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94A3B8' } }
                    }
                }
            });
        }

        // 4. Transaction Amount vs Risk
        const ctxAmount = document.getElementById('chart-amount-analysis');
        if (ctxAmount && data.recentTransactions) {
            const labels = data.recentTransactions.slice(0, 6).map(t => t.transactionReference);
            const amounts = data.recentTransactions.slice(0, 6).map(t => t.amount);
            const scores = data.recentTransactions.slice(0, 6).map(t => t.riskScore || 0);

            this.state.charts.amountAnalysis = new Chart(ctxAmount, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Amount ($)',
                            data: amounts,
                            backgroundColor: 'rgba(59, 130, 246, 0.65)',
                            borderRadius: 6,
                            yAxisID: 'y'
                        },
                        {
                            label: 'Risk Score',
                            data: scores,
                            type: 'line',
                            borderColor: '#EF4444',
                            pointBackgroundColor: '#EF4444',
                            tension: 0.3,
                            yAxisID: 'y1'
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { labels: { color: '#94A3B8' } } },
                    scales: {
                        x: { ticks: { color: '#94A3B8' } },
                        y: { type: 'linear', position: 'left', grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94A3B8' } },
                        y1: { type: 'linear', position: 'right', min: 0, max: 100, grid: { display: false }, ticks: { color: '#EF4444' } }
                    }
                }
            });
        }
    },

    // Transaction History View
    async loadTransactionHistory(page = 0) {
        try {
            const status = document.getElementById('filter-history-status')?.value || '';
            const search = document.getElementById('filter-history-search')?.value || '';

            const res = await fetch(`${API_BASE}/transactions?page=${page}&size=12`, {
                headers: this.getAuthHeaders()
            });

            if (!res.ok) throw new Error('Failed to fetch history');
            const pageData = await res.json();

            const tbody = document.getElementById('history-tbody');
            if (tbody) {
                if (pageData.content.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="10" style="text-align: center; padding: 2rem; color: var(--text-dim);">No transactions found matching criteria.</td></tr>`;
                    return;
                }

                tbody.innerHTML = pageData.content.map(t => `
                    <tr>
                        <td class="mono" style="font-weight: 600; color: var(--neon-cyan);">${t.transactionReference}</td>
                        <td>${new Date(t.createdAt).toLocaleDateString()}</td>
                        <td class="mono" style="font-weight: 700;">$${t.amount.toFixed(2)}</td>
                        <td><span class="badge badge-pill badge-secondary" style="background: rgba(255,255,255,0.06);">${t.merchantCategory}</span></td>
                        <td>${t.location}</td>
                        <td><span style="font-size: 0.85rem; color: var(--text-muted);">${t.deviceType}</span></td>
                        <td><span class="mono" style="font-weight: 700;">${t.riskScore !== null ? t.riskScore : '-'}</span></td>
                        <td>
                            <span class="badge ${t.riskLevel === 'HIGH' ? 'badge-high' : t.riskLevel === 'MEDIUM' ? 'badge-medium' : 'badge-safe'} badge-pill">
                                ${t.riskLevel || 'PENDING'}
                            </span>
                        </td>
                        <td>
                            <span class="badge ${t.status === 'BLOCKED' ? 'badge-blocked' : t.status === 'FLAGGED_FOR_REVIEW' ? 'badge-review' : 'badge-approved'} badge-pill">
                                ${t.status.replace(/_/g, ' ')}
                            </span>
                        </td>
                        <td>
                            <div style="display: flex; gap: 0.35rem;">
                                <button class="btn btn-secondary btn-sm" onclick="App.viewTransactionModal(${t.id})">View</button>
                                <button class="btn btn-outline-cyan btn-sm" onclick="App.reanalyzeTransaction(${t.id})">Re-Analyze</button>
                            </div>
                        </td>
                    </tr>
                `).join('');
            }
        } catch (e) {
            console.error(e);
            this.showToast('Failed to load transaction history', 'error');
        }
    },

    async reanalyzeTransaction(id) {
        try {
            this.showToast('Triggering AI fraud re-evaluation...', 'info');
            const res = await fetch(`${API_BASE}/fraud/analyze/${id}`, {
                method: 'POST',
                headers: this.getAuthHeaders()
            });

            if (!res.ok) throw new Error('Re-analysis failed');
            const analysis = await res.json();
            this.state.lastAnalysis = analysis;
            this.renderAnalysisResult(analysis, {});
            this.navigate('result');
            this.showToast('Re-analysis completed successfully!', 'success');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async viewTransactionModal(id) {
        try {
            const res = await fetch(`${API_BASE}/transactions/${id}`, {
                headers: this.getAuthHeaders()
            });

            if (!res.ok) throw new Error('Failed to load transaction details');
            const txn = await res.json();

            const modalContent = document.getElementById('txn-modal-content');
            modalContent.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; border-bottom: 1px solid var(--border-glass); padding-bottom: 1rem;">
                    <div>
                        <h3 style="font-size: 1.25rem;">Transaction Inspection</h3>
                        <span class="mono" style="color: var(--neon-cyan); font-size: 0.9rem;">${txn.transactionReference}</span>
                    </div>
                    <span class="badge ${txn.status === 'BLOCKED' ? 'badge-high' : txn.status === 'FLAGGED_FOR_REVIEW' ? 'badge-medium' : 'badge-safe'}">
                        ${txn.status.replace(/_/g, ' ')}
                    </span>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem; font-size: 0.9rem;">
                    <div><span style="color: var(--text-muted);">Amount:</span> <strong class="mono" style="font-size: 1.1rem; color: #fff;">$${txn.amount.toFixed(2)} ${txn.currency}</strong></div>
                    <div><span style="color: var(--text-muted);">Type:</span> <strong>${txn.transactionType}</strong></div>
                    <div><span style="color: var(--text-muted);">Merchant:</span> <strong>${txn.merchantCategory}</strong></div>
                    <div><span style="color: var(--text-muted);">Initiated:</span> <strong>${new Date(txn.createdAt).toLocaleString()}</strong></div>
                    <div><span style="color: var(--text-muted);">Location:</span> <strong>${txn.location}</strong></div>
                    <div><span style="color: var(--text-muted);">Habitual Location:</span> <strong>${txn.usualLocation}</strong></div>
                    <div><span style="color: var(--text-muted);">Device Type:</span> <strong>${txn.deviceType}</strong></div>
                    <div><span style="color: var(--text-muted);">New Device:</span> <strong>${txn.newDevice ? 'Yes (Unrecognized)' : 'No (Familiar)'}</strong></div>
                    <div><span style="color: var(--text-muted);">Failed Attempts:</span> <strong>${txn.failedAttempts}</strong></div>
                    <div><span style="color: var(--text-muted);">Account Age:</span> <strong>${txn.accountAgeDays} days</strong></div>
                    <div><span style="color: var(--text-muted);">Frequency Burst:</span> <strong>${txn.transactionFrequency} txns / window</strong></div>
                    <div><span style="color: var(--text-muted);">Source IP:</span> <strong class="mono">${txn.ipAddress || 'Unknown'}</strong></div>
                </div>

                ${txn.fraudAnalysis ? `
                    <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 8px; padding: 1rem; margin-bottom: 1.5rem;">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                            <span style="font-weight: 700; color: var(--neon-cyan);">AI Fraud Evaluation</span>
                            <span class="badge ${txn.fraudAnalysis.riskLevel === 'HIGH' ? 'badge-high' : txn.fraudAnalysis.riskLevel === 'MEDIUM' ? 'badge-medium' : 'badge-safe'}">
                                Risk Score: ${txn.fraudAnalysis.riskScore}/100
                            </span>
                        </div>
                        <p style="font-size: 0.92rem; color: #E2E8F0; font-style: italic; margin-bottom: 0.5rem;">"${txn.fraudAnalysis.aiExplanation}"</p>
                        <div style="font-size: 0.85rem; color: var(--text-muted);">
                            <strong>Recommended Action:</strong> ${txn.fraudAnalysis.recommendation}
                        </div>
                    </div>
                ` : ''}

                <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
                    <button class="btn btn-secondary btn-sm" onclick="document.getElementById('txn-modal').classList.remove('active')">Close</button>
                    <button class="btn btn-primary btn-sm" onclick="document.getElementById('txn-modal').classList.remove('active'); App.reanalyzeTransaction(${txn.id})">Re-Assess With AI</button>
                </div>
            `;

            document.getElementById('txn-modal').classList.add('active');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // Admin Dashboard Data
    async loadAdminData() {
        try {
            const [statsRes, usersRes, logsRes] = await Promise.all([
                fetch(`${API_BASE}/admin/statistics`, { headers: this.getAuthHeaders() }),
                fetch(`${API_BASE}/admin/users`, { headers: this.getAuthHeaders() }),
                fetch(`${API_BASE}/admin/audit-logs`, { headers: this.getAuthHeaders() })
            ]);

            if (!statsRes.ok || !usersRes.ok || !logsRes.ok) throw new Error('Failed to load administrative feeds');

            const stats = await statsRes.json();
            const users = await usersRes.json();
            const logs = await logsRes.json();

            // KPIs
            document.getElementById('admin-total-users').innerText = stats.totalUsers;
            document.getElementById('admin-total-txns').innerText = stats.totalTransactions;
            document.getElementById('admin-safe-txns').innerText = stats.safeTransactions;
            document.getElementById('admin-suspicious-txns').innerText = stats.suspiciousTransactions;
            document.getElementById('admin-high-risk-txns').innerText = stats.highRiskTransactions;
            document.getElementById('admin-fraud-rate').innerText = `${stats.fraudDetectionRate}%`;

            // Users Table
            const userTbody = document.getElementById('admin-users-tbody');
            if (userTbody) {
                userTbody.innerHTML = users.map(u => `
                    <tr>
                        <td class="mono" style="font-weight: 600;">#${u.id}</td>
                        <td><strong>${u.fullName}</strong></td>
                        <td>${u.email}</td>
                        <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-safe'} badge-pill">${u.role.replace('ROLE_', '')}</span></td>
                        <td>${u.transactionCount} transactions</td>
                        <td>${new Date(u.createdAt).toLocaleDateString()}</td>
                    </tr>
                `).join('');
            }

            // High-Risk Incident Feed
            const alertsContainer = document.getElementById('admin-alerts-container');
            if (alertsContainer && stats.highRiskAlerts) {
                alertsContainer.innerHTML = stats.highRiskAlerts.map(a => `
                    <div class="factor-item" style="border-left: 3px solid var(--danger-red); margin-bottom: 0.5rem;">
                        <div class="factor-left">
                            <span class="badge badge-high badge-pill">${a.riskScore}/100</span>
                            <div>
                                <div style="font-weight: 600;">${a.transactionReference} flagged as ${a.status}</div>
                                <div style="font-size: 0.8rem; color: var(--text-muted);">${a.aiExplanation.substring(0, 110)}...</div>
                            </div>
                        </div>
                        <button class="btn btn-secondary btn-sm" onclick="App.viewTransactionModal(${a.transactionId})">Inspect</button>
                    </div>
                `).join('');
            }

            // Audit Logs
            const logsTbody = document.getElementById('admin-audit-tbody');
            if (logsTbody) {
                logsTbody.innerHTML = logs.map(l => `
                    <tr>
                        <td class="mono" style="font-size: 0.8rem; color: var(--text-dim);">${new Date(l.timestamp).toLocaleTimeString()}</td>
                        <td><strong>${l.userEmail || 'Anonymous'}</strong></td>
                        <td><span class="mono" style="font-size: 0.8rem; color: var(--neon-cyan);">${l.action}</span></td>
                        <td>${l.resource}</td>
                        <td class="mono" style="font-size: 0.8rem;">${l.ipAddress || '-'}</td>
                        <td style="font-size: 0.85rem; color: var(--text-muted);">${l.details}</td>
                    </tr>
                `).join('');
            }
        } catch (e) {
            console.error(e);
            this.showToast('Unable to fetch admin dashboard statistics.', 'error');
        }
    },

    // User Profile
    async loadProfileData() {
        try {
            const res = await fetch(`${API_BASE}/users/profile`, {
                headers: this.getAuthHeaders()
            });
            if (!res.ok) throw new Error('Profile fetch failed');
            const data = await res.json();

            document.getElementById('prof-name').innerText = data.fullName;
            document.getElementById('prof-email').innerText = data.email;
            document.getElementById('prof-role').innerText = data.role.replace('ROLE_', '');
            document.getElementById('prof-txns').innerText = `${data.transactionCount} transactions evaluated`;
            document.getElementById('prof-joined').innerText = `Member since ${new Date(data.createdAt).toLocaleDateString()}`;
        } catch (e) {
            console.error(e);
        }
    },

    // Utilities
    downloadReport() {
        window.print();
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let iconSvg = '';
        if (type === 'success') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--safe-green)" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>';
        } else if (type === 'error') {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--danger-red)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';
        } else {
            iconSvg = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--neon-cyan)" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
        }

        toast.innerHTML = `
            ${iconSvg}
            <div style="font-size: 0.9rem; font-weight: 500; color: #fff;">${message}</div>
        `;

        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

document.addEventListener('DOMContentLoaded', () => App.init());
