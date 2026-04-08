window._originalFetch = window.fetch;
window.fetch = async (url, options = {}) => {
    options.headers = options.headers || {};
    const token = Auth.getToken();
    if (token && !options.headers['Authorization']) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }
    const method = (options.method || 'GET').toUpperCase();
    if (method !== 'GET' && !options.headers['X-XSRF-TOKEN']) {
        const getCookie = (name) => {
            const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
            if (match) return match[2];
        };
        const csrfToken = getCookie('XSRF-TOKEN');
        if (csrfToken) {
            options.headers['X-XSRF-TOKEN'] = csrfToken;
        }
    }
    options.credentials = 'include';
    let response = await window._originalFetch(url, options);

    if (response.headers.get('X-Salon-State') === 'CLOSED' && window.location.pathname !== '/offline') {
        App.navigate('/offline');
    }

    const isAuthPath = typeof url === 'string' && url.includes('/api/v1/auth/');
    if (response.status === 401 && !isAuthPath) {
        const refreshed = await Auth.refreshToken();
        if (refreshed) {
            const newToken = Auth.getToken();
            options.headers['Authorization'] = `Bearer ${newToken}`;
            if (method !== 'GET') {
                const getCookie = (name) => {
                    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
                    if (match) return match[2];
                };
                const csrfToken = getCookie('XSRF-TOKEN');
                if (csrfToken) options.headers['X-XSRF-TOKEN'] = csrfToken;
            }
            response = await window._originalFetch(url, options);
        } else {
            return response;
        }
    }
    if (!response.ok) {
        if (response.status !== 401 || isAuthPath) {
            await ErrorHandler.handle(response.clone());
        }
    }
    return response;
};

window.navigate = (path) => App.navigate(path);

const App = {
    initialized: false,
    tenantError: false,
    salon: null,
    currentPath: null,
    templateCache: new Map(),

    init: async function() {
        if (this.initialized) return;
        this.initialized = true;
        await this.initTheme();
        if (this.tenantError) {
             const appContent = document.getElementById('app-content');
             if (appContent) {
                 appContent.innerHTML = '<div class="container" style="padding: 50px; text-align: center;"><h2>URL de acesso inválida</h2><p>Certifique-se de acessar pelo link correto do salão.</p></div>';
             }
             return;
        }
        this.checkAuth();
        if (typeof NotificationService !== 'undefined') {
            NotificationService.init();
        }
        await this.handleRouting(true);
        window.addEventListener('popstate', () => this.handleRouting());
    },

    navigate: function(path) {
        window.history.pushState({}, '', path);
        this.handleRouting();
    },

    handleRouting: async function(isInitial = false) {
        if (this.tenantError) return;
        const path = window.location.pathname;
        const fullPath = path + window.location.search;
        if (this.currentPath === fullPath) return;
        this.currentPath = fullPath;

        if (this.salon && (isInitial || Auth.getToken())) {
            UI.renderGlobalHeader(this.salon);
        }

        const appContent = document.getElementById('app-content');
        if (!appContent) return;

        if (path.startsWith('/admin') || path.startsWith('/perfil') || path.startsWith('/profissional')) {
            if (!Auth.getToken()) {
                this.navigate('/entrar');
                return;
            }
            if (path.startsWith('/admin') && !Auth.hasRole('ADMIN')) {
                this.navigate('/agendar');
                return;
            }
            if (path.startsWith('/profissional') && !Auth.hasRole('PROFESSIONAL')) {
                this.navigate('/agendar');
                return;
            }
        }

        let templatePath = '';
        let scriptPath = '';
        let isModule = false;
        let pageTitle = 'Agendamento';

        const routeMap = {
            '/entrar': { template: '/pages/public/login.html', script: '/js/pages/login.js', title: 'Entrar' },
            '/cadastro': { template: '/pages/public/register.html', script: '/js/pages/register.js', title: 'Cadastro' },
            '/admin/configuracoes': { template: '/pages/admin/settings.html', script: '/js/pages/admin/settings.js', title: 'Configurações', isModule: true },
            '/admin/servicos': { template: '/pages/admin/services.html', script: '/js/pages/admin/services.js', title: 'Serviços' },
            '/': { template: '/pages/booking/index.html', script: '/js/pages/booking.js', title: 'Agendar' },
            '/agendar': { template: '/pages/booking/index.html', script: '/js/pages/booking.js', title: 'Agendar' },
            '/perfil': { template: '/pages/public/profile.html', script: '/js/pages/profile.js', title: 'Meu Perfil' },
            '/profissional/agenda': { template: '/pages/professional/schedule.html', script: '/js/pages/professional/schedule.js', title: 'Minha Agenda', isModule: true },
            '/offline': { template: '/pages/public/offline.html', script: '/js/pages/offline.js', title: 'Indisponível' }
        };

        const route = routeMap[path] || (path.startsWith('/perfil') ? routeMap['/perfil'] : routeMap['/']);
        
        templatePath = route.template;
        scriptPath = route.script;
        isModule = !!route.isModule;
        pageTitle = route.title;

        if (templatePath) {
            document.title = this.salon ? `${this.salon.tradeName} - ${pageTitle}` : pageTitle;

            if (!isInitial && (appContent.innerHTML.trim() === '' || appContent.innerHTML.includes('Carregando...'))) {
                appContent.innerHTML = '<div class="container" style="text-align: center; padding: 50px;"><p>Carregando...</p></div>';
            }

            try {
                let html;
                if (this.templateCache.has(templatePath)) {
                    html = this.templateCache.get(templatePath);
                } else {
                    const res = await fetch(templatePath);
                    if (res.ok) {
                        html = await res.text();
                        this.templateCache.set(templatePath, html);
                    } else if (res.status === 400) {
                        this.tenantError = true;
                        appContent.innerHTML = '<div class="container" style="padding: 50px; text-align: center;"><h2>URL de acesso inválida</h2><p>Certifique-se de acessar pelo link correto do salão.</p></div>';
                        return;
                    }
                }

                if (html) {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const snippetContent = doc.querySelector('main') || doc.body;

                    await new Promise(resolve => {
                        requestAnimationFrame(() => {
                            appContent.innerHTML = snippetContent.innerHTML;

                            const styles = doc.querySelectorAll('link[rel="stylesheet"]');
                            styles.forEach(s => {
                                const href = s.getAttribute('href');
                                if (!document.querySelector(`link[href="${href}"]`)) {
                                    const newLink = document.createElement('link');
                                    newLink.rel = 'stylesheet';
                                    newLink.href = href;
                                    newLink.media = 'print';
                                    newLink.onload = () => { newLink.media = 'all'; };
                                    document.head.appendChild(newLink);
                                }
                            });
                            
                            this.applyBranding();
                            
                            // Double rAF to ensure browser has processed the innerHTML change
                            requestAnimationFrame(() => resolve());
                        });
                    });

                    if (scriptPath) {
                        await this.loadScript(scriptPath, isModule);
                        this.initPage(path);
                    }
                }
            } catch (err) {
                appContent.innerHTML = '<div class="container" style="padding: 50px;">Erro ao carregar página.</div>';
            }
        }
    },

    initPage: function(path) {
        if (path === '/entrar' && typeof initLogin === 'function') initLogin();
        if (path === '/cadastro' && typeof initRegister === 'function') initRegister();
        if (path === '/admin/configuracoes' && typeof adminSettingsApp !== 'undefined') adminSettingsApp.init();
        if (path === '/admin/servicos' && typeof adminServicesApp !== 'undefined') adminServicesApp.init();
        if ((path === '/' || path === '/agendar') && typeof bookingApp !== 'undefined') bookingApp.init();
        if (path.startsWith('/perfil') && typeof initProfile === 'function') initProfile();
        if (path === '/profissional/agenda' && typeof professionalScheduleApp !== 'undefined') professionalScheduleApp.init();
        if (path === '/offline' && typeof initOffline === 'function') initOffline();
    },

    loadScript: function(src, isModule = false) {
        return new Promise((resolve, reject) => {
            const existing = document.querySelector(`script[src="${src}"]`);
            if (existing) {
                 resolve();
                 return;
            }
            const script = document.createElement('script');
            script.src = src;
            script.defer = true;
            if (isModule) {
                script.type = 'module';
            }
            script.onload = resolve;
            script.onerror = reject;
            document.body.appendChild(script);
        });
    },

    initTheme: async function() {
        try {
            const res = await fetch('/api/v1/salon/profile');
            if (res.ok) {
                this.salon = await res.json();
                if (this.salon.primaryColor) {
                    document.documentElement.style.setProperty('--primary', this.salon.primaryColor);
                }
                UI.renderGlobalHeader(this.salon);
                UI.renderGlobalFooter(this.salon);
            } else if (res.status === 400) {
                this.tenantError = true;
            }
        } catch (e) {
        }
    },

    applyBranding: function() {
        if (!this.salon) return;
        document.querySelectorAll('[data-salon-field]').forEach(el => {
            const field = el.getAttribute('data-salon-field');
            if (this.salon[field]) {
                el.innerText = this.salon[field];
            }
        });
    },

    checkAuth: function() {
        const payload = Auth.getPayload();
        if (payload && payload.isFirstLogin && window.location.pathname !== '/entrar') {
            this.showFirstLoginModal();
        }
    },

    showFirstLoginModal: function() {
        if (document.getElementById('first-login-modal')) return;
        const modalHtml = `
            <div id="first-login-modal" class="modal-overlay">
                <div class="modal-content fade-in" style="max-width: 400px;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <span style="font-size: 40px;">🔒</span>
                        <h2 style="margin-top: 10px; color: var(--text-main);">Primeiro Acesso</h2>
                        <p style="font-size: 14px; color: var(--text-muted);">Para sua segurança, você deve alterar sua senha inicial para continuar.</p>
                    </div>
                    <form id="first-login-form">
                        <div class="form-group">
                            <label class="form-label">Nova Senha</label>
                            <input type="password" id="new-password" class="form-input" 
                                   placeholder="Mínimo 6 caracteres" required minlength="6">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Confirmar Nova Senha</label>
                            <input type="password" id="confirm-password" class="form-input" 
                                   placeholder="Repita a nova senha" required minlength="6">
                        </div>
                        <button type="submit" id="btn-change-pass" class="btn btn-primary btn-block">
                            Definir Nova Senha
                        </button>
                    </form>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        const form = document.getElementById('first-login-form');
        const btn = document.getElementById('btn-change-pass');
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            if (newPassword !== confirmPassword) {
                Toast.error('As senhas não coincidem.');
                return;
            }
            btn.disabled = true;
            btn.innerText = 'Processando...';
            try {
                const res = await fetch('/api/v1/user/change-password', {
                    method: 'POST',
                    headers: { 
                        'Content-Type': 'text/plain',
                        'Authorization': `Bearer ${Auth.getToken()}`
                    },
                    body: newPassword
                });
                if (res.ok) {
                    Toast.success('Senha alterada com sucesso! Faça login novamente.');
                    setTimeout(() => Auth.logout(), 2000);
                } else {
                    const err = await res.json();
                    Toast.error(err.messages?.[0] || 'Erro ao alterar senha.');
                }
            } catch (err) {
                Toast.error('Erro de conexão ao alterar senha.');
            } finally {
                btn.disabled = false;
                btn.innerText = 'Definir Nova Senha';
            }
        });
    }
};
document.addEventListener('DOMContentLoaded', () => App.init());