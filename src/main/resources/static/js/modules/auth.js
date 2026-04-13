const Auth = {
    tokenKey: 'nails_pro_token',
    refreshPromise: null,

    getToken: function() {
        return localStorage.getItem(this.tokenKey);
    },

    setToken: function(token) {
        localStorage.setItem(this.tokenKey, token);
    },

    clearToken: function() {
        localStorage.removeItem(this.tokenKey);
    },

    getPayload: function() {
        const token = this.getToken();
        if (!token) return null;
        try {
            return JSON.parse(atob(token.split('.')[1]));
        } catch (e) {
            return null;
        }
    },

    getUserId: function() {
        const payload = this.getPayload();
        return payload ? payload.sub : null;
    },

    getUserRoles: function() {
        const payload = this.getPayload();
        if (!payload || !payload.roles) return [];
        return payload.roles;
    },

    hasRole: function(role) {
        return this.getUserRoles().includes(role);
    },

    isDemo: function() {
        const payload = this.getPayload();
        return payload && payload.tenantId === 'demo-salon-2026';
    },

    refreshToken: async function() {
        if (this.refreshPromise) {
            return this.refreshPromise;
        }

        this.refreshPromise = (async () => {
            const originalFetch = window._originalFetch || window.fetch;
            
            const getTenantId = () => {
                const pathParts = window.location.pathname.split('/');
                return pathParts[1] || null;
            };

            const headers = {
                'Content-Type': 'application/json'
            };

            const tenantId = getTenantId();
            if (tenantId) {
                headers['X-Tenant-Id'] = tenantId;
            }

            try {
                const res = await originalFetch('/api/v1/auth/refresh', {
                    method: 'POST',
                    headers: headers,
                    credentials: 'include'
                });

                if (res.ok) {
                    const token = await res.text();
                    this.setToken(token);
                    return true;
                }
            } catch (e) {
                console.error('Refresh token error:', e);
            } finally {
                this.refreshPromise = null;
            }

            this.logout();
            return false;
        })();

        return this.refreshPromise;
    },

    logout: function() {
        this.clearToken();
        window.location.href = '/entrar';
    }
};
