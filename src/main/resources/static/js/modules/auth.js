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

    refreshToken: async function() {
        if (this.refreshPromise) {
            return this.refreshPromise;
        }

        this.refreshPromise = (async () => {
            const originalFetch = window._originalFetch || window.fetch;
            try {
                const res = await originalFetch('/api/v1/auth/refresh', {
                    method: 'POST',
                    credentials: 'include'
                });

                if (res.ok) {
                    const token = await res.text();
                    this.setToken(token);
                    console.log('Token refreshed successfully');
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
