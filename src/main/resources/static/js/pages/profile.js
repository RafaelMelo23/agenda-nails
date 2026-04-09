const profileApp = {
    user: null,
    appointments: [],
    appointmentToCancel: null,
    highlightedId: null,

    setupTabs: function() {
        const tabs = {
            'tab-upcoming': { section: 'section-appointments', list: 'list-upcoming' },
            'tab-history': { section: 'section-appointments', list: 'list-history' },
            'tab-settings': { section: 'section-settings' }
        };

        Object.keys(tabs).forEach(tabId => {
            const btn = document.getElementById(tabId);
            if (btn) {
                btn.onclick = (e) => {
                    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
                    e.target.classList.add('active');

                    document.getElementById('section-appointments').classList.add('hidden');
                    document.getElementById('section-settings').classList.add('hidden');

                    const config = tabs[tabId];
                    document.getElementById(config.section).classList.remove('hidden');

                    if (config.list) {
                        document.getElementById('list-upcoming').classList.add('hidden');
                        document.getElementById('list-history').classList.add('hidden');
                        document.getElementById(config.list).classList.remove('hidden');
                    }
                };
            }
        });
    },

    extractAppointmentId: function() {
        const params = new URLSearchParams(window.location.search);
        let id = params.get('id');
        
        if (!id) {
            const pathParts = window.location.pathname.split('/');
            const manageIdx = pathParts.indexOf('manage');
            if (manageIdx !== -1 && pathParts[manageIdx + 1]) {
                id = pathParts[manageIdx + 1];
            }
        }
        
        this.highlightedId = (id && !isNaN(id)) ? parseInt(id) : null;
    },

    loadProfile: async function() {
        try {
            const res = await fetch('/api/v1/user');
            if (res.ok) {
                this.user = await res.json();
                this.renderProfile();
            }
        } catch (error) {
            console.error('Error loading profile:', error);
        }
    },

    loadAppointments: async function() {
        try {
            const res = await fetch('/api/v1/client/appointments?size=50');
            if (res.ok) {
                const data = await res.json();
                this.appointments = data.content;
                this.renderAppointments();
            }
        } catch (error) {
            console.error('Error loading appointments:', error);
        }
    },

    renderProfile: function() {
        if (!this.user) return;

        document.getElementById('user-name').innerText = this.user.fullName.split(' ')[0];
        document.getElementById('set-name').innerText = this.user.fullName;
        document.getElementById('set-email').innerText = this.user.email;
        document.getElementById('set-phone').innerText = this.user.phoneNumber || 'Não informado';

        if (this.user.needsPasswordChange) {
            UI.showToast('Você está usando uma senha temporária. Por favor, defina uma nova senha agora.', 'info');
            this.showEdit('password');
            // Hide cancel button to "force" change
            const cancelBtn = document.querySelector('#modal-overlay .btn-outline');
            if (cancelBtn) cancelBtn.style.display = 'none';
        }

        if (Auth.hasRole('PROFESSIONAL')) {
            const picContainer = document.getElementById('prof-pic-container');
            const picEl = document.getElementById('prof-pic');
            
            if (picContainer && picEl) {
                picContainer.classList.remove('hidden');
                if (this.user.professionalPicture) {
                    picEl.src = this.user.professionalPicture;
                } else {
                    picEl.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(this.user.fullName)}&background=FB7185&color=fff`;
                }
            }
        }
    },

    triggerPicUpload: function() {
        document.getElementById('pic-upload-input').click();
    },

    handlePicUpload: async function(event) {
        const file = event.target.files[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            Toast.error('Por favor, selecione uma imagem.');
            return;
        }

        if (file.size > 2 * 1024 * 1024) {
            Toast.error('A imagem deve ter no máximo 2MB.');
            return;
        }

        const reader = new FileReader();
        reader.onload = async (e) => {
            const base64 = e.target.result;
            await this.uploadPicture(base64);
        };
        reader.readAsDataURL(file);
    },

    uploadPicture: async function(base64) {
        const picEl = document.getElementById('prof-pic');
        const oldSrc = picEl ? picEl.src : '';
        
        if (picEl) picEl.style.opacity = '0.5';

        try {
            const res = await fetch('/api/v1/professional/profile/picture', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pictureBase64: base64 })
            });

            if (res.ok) {
                Toast.success('Foto de perfil atualizada!');
                await this.loadProfile();
            } else {
                if (picEl) picEl.src = oldSrc;
                const err = await res.json();
                Toast.error(err.message || 'Erro ao atualizar foto.');
            }
        } catch (error) {
            if (picEl) picEl.src = oldSrc;
            Toast.error('Erro de conexão.');
        } finally {
            if (picEl) picEl.style.opacity = '1';
        }
    },

    renderAppointments: function() {
        const listUpcoming = document.getElementById('list-upcoming');
        const listHistory = document.getElementById('list-history');

        const now = new Date();
        let upcoming = this.appointments.filter(a => new Date(a.startDate) >= now && a.status !== 'CANCELLED' && a.status !== 'FINISHED' && a.status !== 'MISSED');
        let history = this.appointments.filter(a => new Date(a.startDate) < now || a.status === 'CANCELLED' || a.status === 'FINISHED' || a.status === 'MISSED');

        if (this.highlightedId) {
            const upIdx = upcoming.findIndex(a => a.id === this.highlightedId);
            if (upIdx !== -1) {
                const highlighted = upcoming.splice(upIdx, 1)[0];
                upcoming.unshift(highlighted);
            } else {
                const histIdx = history.findIndex(a => a.id === this.highlightedId);
                if (histIdx !== -1) {
                    const highlighted = history.splice(histIdx, 1)[0];
                    history.unshift(highlighted);
                    const historyTab = document.getElementById('tab-history');
                    if (historyTab) historyTab.click();
                }
            }
            this.autoScroll();
        }

        listUpcoming.innerHTML = upcoming.length ? upcoming.map(a => this.createApptCard(a)).join('') : '<div class="empty-state">Você não tem agendamentos próximos.</div>';
        listHistory.innerHTML = history.length ? history.map(a => this.createApptCard(a)).join('') : '<div class="empty-state">Nenhum histórico encontrado.</div>';
    },

    createApptCard: function(a) {
        const start = new Date(a.startDate);
        const day = start.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
        const time = start.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
        
        const statusMap = {
            'CONFIRMED': { label: 'Confirmado', class: 'status-confirmed' },
            'PENDING': { label: 'Pendente', class: 'status-pending' },
            'CANCELLED': { label: 'Cancelado', class: 'status-cancelled' },
            'FINISHED': { label: 'Concluído', class: 'status-done' },
            'MISSED': { label: 'Faltou', class: 'status-cancelled' }
        };

        const status = statusMap[a.status] || { label: a.status, class: '' };
        const canCancel = a.status !== 'CANCELLED' && a.status !== 'FINISHED' && a.status !== 'MISSED' && new Date(a.startDate) > new Date();
        const isHighlighted = a.id === this.highlightedId;

        return `
            <div class="appt-card ${isHighlighted ? 'highlighted' : ''}" data-id="${a.id}">
                <div class="appt-header">
                    <div class="appt-date">
                        <span class="date-day">${day}</span>
                        <span class="date-time">${time}</span>
                    </div>
                    <span class="status-badge ${status.class}">${status.label}</span>
                </div>
                <div class="appt-body">
                    <h3>${a.mainServiceName}</h3>
                    <p>Profissional: ${a.professionalName}</p>
                    ${a.addOns && a.addOns.length ? `<p style="margin-top:4px; font-size:11px;">+ ${a.addOns.length} adicionais</p>` : ''}
                </div>
                ${canCancel ? `
                    <div class="appt-actions">
                        <button class="btn-sm btn-cancel-appt" onclick="profileApp.openCancelModal(${a.id})">Cancelar</button>
                    </div>
                ` : ''}
            </div>
        `;
    },

    autoScroll: function() {
        setTimeout(() => {
            const el = document.querySelector('.appt-card.highlighted');
            if (el) {
                el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }, 200);
    },

    openCancelModal: function(id) {
        this.appointmentToCancel = id;
        document.getElementById('modal-cancel').classList.remove('hidden');
    },

    closeCancelModal: function() {
        this.appointmentToCancel = null;
        document.getElementById('modal-cancel').classList.add('hidden');
    },

    confirmCancel: async function() {
        if (!this.appointmentToCancel) return;
        const id = this.appointmentToCancel;
        const btn = document.getElementById('btn-confirm-cancel');
        
        btn.disabled = true;
        btn.innerText = 'Cancelando...';

        try {
            const res = await fetch(`/api/v1/booking/${id}`, {
                method: 'PATCH'
            });

            if (res.ok) {
                Toast.success('Agendamento cancelado com sucesso.');
                this.closeCancelModal();
                await this.loadAppointments();
            } else {
                const err = await res.json();
                Toast.error(err.message || 'Erro ao cancelar agendamento.');
            }
        } catch (error) {
            Toast.error('Erro de conexão.');
        } finally {
            btn.disabled = false;
            btn.innerText = 'Sim, Cancelar';
        }
    },

    showEdit: function(type) {
        const overlay = document.getElementById('modal-overlay');
        const title = document.getElementById('modal-title');
        const editType = document.getElementById('edit-type');
        
        editType.value = type;
        overlay.classList.remove('hidden');

        document.getElementById('email-fields').classList.add('hidden');
        document.getElementById('phone-fields').classList.add('hidden');
        document.getElementById('password-fields').classList.add('hidden');
        document.getElementById('password-confirm-field').classList.remove('hidden');
        document.getElementById('btn-save').classList.remove('hidden');

        if (type === 'email') {
            title.innerText = 'Alterar E-mail';
            document.getElementById('email-fields').classList.remove('hidden');
            document.getElementById('new-email').value = this.user.email;
        } else if (type === 'phone') {
            title.innerText = 'Alterar Telefone';
            document.getElementById('phone-fields').classList.remove('hidden');
            document.getElementById('new-phone').value = this.user.phoneNumber || '';
        } else if (type === 'password') {
            title.innerText = 'Alterar Senha';
            document.getElementById('password-fields').classList.remove('hidden');
            document.getElementById('password-confirm-field').classList.add('hidden');
            document.getElementById('btn-save').innerText = 'Alterar Senha';
        }
    },

    closeModal: function() {
        document.getElementById('modal-overlay').classList.add('hidden');
        document.getElementById('edit-form').reset();
        document.getElementById('btn-save').innerText = 'Salvar';
    },

    handleUpdate: async function(e) {
        e.preventDefault();
        const type = document.getElementById('edit-type').value;
        const password = document.getElementById('confirm-pass').value;
        const btn = document.getElementById('btn-save');

        if (type !== 'password' && !password) {
            Toast.error('Por favor, confirme sua senha atual.');
            return;
        }

        if (type === 'password') {
            const email = document.getElementById('confirm-email').value;
            const newPass = document.getElementById('new-password').value;
            const repeatPass = document.getElementById('repeat-password').value;

            if (!email || !newPass || !repeatPass) {
                Toast.error('Por favor, preencha todos os campos.');
                return;
            }

            if (newPass !== repeatPass) {
                Toast.error('As senhas não coincidem.');
                return;
            }

            if (newPass.length < 6) {
                Toast.error('A senha deve ter pelo menos 6 caracteres.');
                return;
            }
        }

        btn.disabled = true;
        btn.innerText = 'Processando...';

        try {
            let url = '';
            let method = 'PATCH';
            let body = {};

            if (type === 'email') {
                url = '/api/v1/user/email';
                body = { newEmail: document.getElementById('new-email').value, password };
            } else if (type === 'phone') {
                url = '/api/v1/user/phone';
                body = { newPhone: document.getElementById('new-phone').value, password };
            } else if (type === 'password') {
                url = '/api/v1/user/password';
                body = { 
                    email: document.getElementById('confirm-email').value,
                    newPassword: document.getElementById('new-password').value 
                };
            }

            const res = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (res.ok) {
                Toast.success('Dados atualizados com sucesso.');
                if (type === 'password') {
                    Toast.success('Sua senha foi alterada. Por favor, faça login novamente.');
                    setTimeout(() => Auth.logout(), 2000);
                } else {
                    this.closeModal();
                    await this.loadProfile();
                }
            } else {
                const err = await res.json();
                Toast.error(err.message || err.messages?.[0] || 'Erro ao atualizar dados.');
            }
        } catch (error) {
            Toast.error('Erro de conexão.');
        } finally {
            btn.disabled = false;
            btn.innerText = 'Salvar';
        }
    }
};

function initProfile() {
    if (!Auth.getToken()) {
        App.navigate('/entrar');
        return;
    }

    profileApp.setupTabs();
    profileApp.extractAppointmentId();
    
    profileApp.loadProfile();
    profileApp.loadAppointments();

    const confirmBtn = document.getElementById('btn-confirm-cancel');
    if (confirmBtn) {
        confirmBtn.onclick = () => profileApp.confirmCancel();
    }

    window.onpopstate = () => {
        profileApp.extractAppointmentId();
        profileApp.renderAppointments();
    };
}
