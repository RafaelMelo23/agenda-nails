const loginApp = {
    init: function() {
        const loginForm = document.getElementById('login-form');
        this.btnLogin = document.getElementById('btn-login');

        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }
    },

    handleLogin: async function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        UI.setLoading(this.btnLogin, true, 'Autenticando...');

        try {
            const response = await fetch('/api/v1/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                Auth.setToken(data.jwtToken);

                UI.showToast('Login realizado com sucesso!', 'success');

                setTimeout(() => {
                    const roles = Auth.getUserRoles();
                    if (roles.includes('SUPER_ADMIN') || roles.includes('ADMIN')) {
                        App.navigate('/admin/configuracoes');
                    } else if (roles.includes('PROFESSIONAL')) {
                        App.navigate('/profissional/agenda');
                    } else {
                        App.navigate('/agendar');
                    }
                }, 1000);
            } else {
                const error = await response.json();
                UI.showToast(error.message || 'Dados inválidos.', 'error');
            }
        } catch (error) {
            UI.showToast('Erro ao realizar login.', 'error');
        } finally {
            UI.setLoading(this.btnLogin, false, 'Entrar');
        }
    },

    showRecoveryModal: function() {
        document.getElementById('modal-recovery').classList.remove('hidden');
    },

    closeRecoveryModal: function() {
        document.getElementById('modal-recovery').classList.add('hidden');
    },

    requestWhatsAppRecovery: function() {
        const id = document.getElementById('recovery-id').value;
        if (!id) {
            UI.showToast('Por favor, informe seu e-mail ou nome.', 'error');
            return;
        }

        const supportNumber = App.salon ? App.salon.supportPhoneNumber : '';
        if (!supportNumber) {
            UI.showToast('Suporte indisponível no momento.', 'error');
            return;
        }

        const message = `Olá, meu e-mail/nome é ${id} e gostaria de solicitar uma senha temporária para o Agenda Nails.`;
        const url = `https://wa.me/${supportNumber.replace(/\D/g, '')}?text=${encodeURIComponent(message)}`;
        
        window.open(url, '_blank');
        this.closeRecoveryModal();
    }
};

function initLogin() {
    loginApp.init();
}
