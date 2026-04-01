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

    const isAuthPath = typeof url === 'string' && url.includes('/api/v1/auth/');

    if (response.status === 401 && !isAuthPath) {
        console.warn('Session expired. Attempting to refresh token...');
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
            console.log('Request retried successfully after token refresh');
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

console.log('Nails Pro initialized');
