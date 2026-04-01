document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const btnLogin = document.getElementById('btn-login');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            UI.setLoading(btnLogin, true, 'Autenticando...');

            try {
                const response = await fetch('/api/v1/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                });

                if (response.ok) {
                    const token = await response.text();
                    Auth.setToken(token);

                    UI.showToast('Login realizado com sucesso!', 'success');

                    const payload = JSON.parse(atob(token.split('.')[1]));

                    setTimeout(() => {
                        if (payload.role === 'ADMIN' || payload.role === 'SUPER_ADMIN') {
                            window.location.href = '/admin/dashboard';
                        } else if (payload.role === 'PROFESSIONAL') {
                            window.location.href = '/agenda-profissional';
                        } else {
                            window.location.href = '/agendar';
                        }
                    }, 1000);
                }
            } catch (error) {
                console.error('Login error:', error);
            } finally {
                UI.setLoading(btnLogin, false, 'Entrar no Sistema');
            }
        });
    }
});
