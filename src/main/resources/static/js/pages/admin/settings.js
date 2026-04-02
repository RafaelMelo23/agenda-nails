const adminSettingsApp = {
    clientsPage: 0,
    clientsSearchTimeout: null,

    init: function() {
        const profList = document.getElementById('professionals-list');
        if (profList) {
            adminSettingsApp.loadProfessionals();
            adminSettingsApp.loadClients();
            adminSettingsApp.loadSalonProfile();
            adminSettingsApp.setupColorPicker();
        }
    },

    loadSalonProfile: async function() {
        try {
            const res = await fetch('/api/v1/admin/salon/profile');
            if (res.ok) {
                const salon = await res.json();
                const form = document.getElementById('salon-profile-form');
                if (form) {
                    form.querySelector('[name="tradeName"]').value = salon.tradeName || '';
                    form.querySelector('[name="slogan"]').value = salon.slogan || '';
                    form.querySelector('[name="comercialPhone"]').value = salon.comercialPhone || '';
                    form.querySelector('[name="primaryColor"]').value = salon.primaryColor || '#E91E63';
                    form.querySelector('.color-text').value = (salon.primaryColor || '#E91E63').toUpperCase();
                    form.querySelector('[name="fullAddress"]').value = salon.fullAddress || '';
                }
            }
        } catch (e) {
            console.error('Error loading profile:', e);
        }
    },

    // --- Helpers ---
    setLoading: function(btn, loading) {
        if (!btn) return;
        if (loading) {
            btn.classList.add('btn-loading');
            btn.disabled = true;
        } else {
            btn.classList.remove('btn-loading');
            btn.disabled = false;
        }
    },

    showConfirm: function(title, message) {
        return new Promise((resolve) => {
            const modal = document.getElementById('confirm-modal');
            const btnOk = document.getElementById('confirm-ok');
            const btnCancel = document.getElementById('confirm-cancel');
            
            document.getElementById('confirm-title').textContent = title;
            document.getElementById('confirm-message').textContent = message;
            
            modal.classList.remove('hidden');

            const cleanup = (result) => {
                modal.classList.add('hidden');
                btnOk.onclick = null;
                btnCancel.onclick = null;
                resolve(result);
            };

            btnOk.onclick = () => cleanup(true);
            btnCancel.onclick = () => cleanup(false);
        });
    },

    getInitials: function(name) {
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    },

    // --- Professionals ---
    loadProfessionals: async function() {
        const list = document.getElementById('professionals-list');
        try {
            const response = await fetch('/api/v1/admin/professional');
            if (response.ok) {
                const professionals = await response.json();
                adminSettingsApp.renderProfessionals(professionals);
            }
        } catch (error) {
            list.innerHTML = '<tr><td colspan="5" class="empty-state">Erro ao carregar profissionais.</td></tr>';
        }
    },

    renderProfessionals: function(professionals) {
        const list = document.getElementById('professionals-list');
        if (professionals.length === 0) {
            list.innerHTML = '<tr><td colspan="5" class="empty-state">Nenhum profissional cadastrado.</td></tr>';
            return;
        }

        list.innerHTML = professionals.map(prof => `
            <tr>
                <td>
                    <div class="prof-cell">
                        <div class="prof-initials">${adminSettingsApp.getInitials(prof.name)}</div>
                        <div>
                            <strong>${prof.name}</strong><br>
                            <span style="font-size: 0.8rem; color: #666;">${prof.isFirstLogin ? 'Pendente Primeiro Acesso' : 'Profissional'}</span>
                        </div>
                    </div>
                </td>
                <td>${prof.email}</td>
                <td><span class="tag">Todos</span></td>
                <td><span class="badge ${prof.isActive ? 'badge-success' : 'badge-danger'}">${prof.isActive ? 'Ativo' : 'Inativo'}</span></td>
                <td>
                    ${prof.isActive ? 
                        `<button class="btn-outline-danger" onclick="adminSettingsApp.handleDeactivateProfessional(${prof.id}, this)">Desativar</button>` : 
                        `<button class="btn btn-secondary btn-sm" onclick="adminSettingsApp.handleActivateProfessional(${prof.id}, this)">Ativar</button>`}
                </td>
            </tr>
        `).join('');
    },

    handleDeactivateProfessional: async function(id, btn) {
        const confirmed = await adminSettingsApp.showConfirm('Desativar Profissional', 'Tem certeza que deseja desativar esta profissional?');
        if (!confirmed) return;

        adminSettingsApp.setLoading(btn, true);
        try {
            const response = await fetch(`/api/v1/admin/professional/${id}/deactivate`, { method: 'PATCH' });
            if (response.ok) {
                Toast.success('Profissional desativada com sucesso!');
                await adminSettingsApp.loadProfessionals();
            }
        } finally {
            adminSettingsApp.setLoading(btn, false);
        }
    },

    handleActivateProfessional: async function(id, btn) {
        const confirmed = await adminSettingsApp.showConfirm('Ativar Profissional', 'Deseja reativar esta profissional?');
        if (!confirmed) return;

        adminSettingsApp.setLoading(btn, true);
        try {
            const response = await fetch(`/api/v1/admin/professional/${id}/activate`, { method: 'PATCH' });
            if (response.ok) {
                Toast.success('Profissional ativada com sucesso!');
                await adminSettingsApp.loadProfessionals();
            }
        } finally {
            adminSettingsApp.setLoading(btn, false);
        }
    },

    openProfessionalModal: function() {
        document.getElementById('professional-modal').classList.remove('hidden');
        adminSettingsApp.loadServicesForModal();
    },

    closeProfessionalModal: function() {
        document.getElementById('professional-modal').classList.add('hidden');
        document.getElementById('professional-form').reset();
    },

    loadServicesForModal: async function() {
        const list = document.getElementById('services-checkbox-list');
        try {
            const response = await fetch('/api/v1/salon/service');
            if (response.ok) {
                const services = await response.json();
                if (services.length === 0) {
                    list.innerHTML = '<p class="empty-state">Nenhum serviço disponível.</p>';
                    return;
                }
                list.innerHTML = services.map(s => `
                    <label class="checkbox-item">
                        <input type="checkbox" name="services" value="${s.id}">
                        <span>${s.name}</span>
                    </label>
                `).join('');
            }
        } catch (error) {
            list.innerHTML = '<p class="empty-state">Erro ao carregar serviços.</p>';
        }
    },

    handleCreateProfessional: async function(event) {
        event.preventDefault();
        const form = event.target;
        const btn = form.querySelector('button[type="submit"]');
        const selectedServices = Array.from(form.querySelectorAll('input[name="services"]:checked')).map(cb => parseInt(cb.value));

        if (selectedServices.length === 0) {
            Toast.error('Selecione pelo menos um serviço.');
            return;
        }

        const data = {
            fullName: document.getElementById('prof-name').value,
            email: document.getElementById('prof-email').value,
            servicesOfferedByProfessional: selectedServices
        };

        adminSettingsApp.setLoading(btn, true);
        try {
            const response = await fetch('/api/v1/admin/professional', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                Toast.success('Profissional cadastrada com sucesso!');
                adminSettingsApp.closeProfessionalModal();
                await adminSettingsApp.loadProfessionals();
            }
        } finally {
            adminSettingsApp.setLoading(btn, false);
        }
    },

    // --- Clients ---
    debounceSearchClients: function() {
        clearTimeout(adminSettingsApp.clientsSearchTimeout);
        adminSettingsApp.clientsSearchTimeout = setTimeout(() => {
            adminSettingsApp.clientsPage = 0;
            adminSettingsApp.loadClients();
        }, 500);
    },

    loadClients: async function() {
        const searchTerm = document.getElementById('client-search').value;
        try {
            const response = await fetch(`/api/v1/admin/client?name=${encodeURIComponent(searchTerm)}&page=${adminSettingsApp.clientsPage}&size=10`);
            if (response.ok) {
                const data = await response.json();
                adminSettingsApp.renderClients(data.content);
                adminSettingsApp.renderPagination(data);
            }
        } catch (error) {
            console.error('Error loading clients:', error);
        }
    },

    renderClients: function(clients) {
        const list = document.getElementById('clients-list');
        if (clients.length === 0) {
            list.innerHTML = '<tr><td colspan="4" class="empty-state">Nenhum cliente encontrado.</td></tr>';
            return;
        }

        list.innerHTML = clients.map(client => `
            <tr>
                <td style="${client.userStatus === 'BANNED' ? 'text-decoration: line-through; opacity: 0.6;' : ''}">
                    <strong>${client.fullName}</strong>
                </td>
                <td>${client.phoneNumber || 'N/A'}</td>
                <td>
                    ${client.missedAppointments > 0 ? 
                        `<span class="badge badge-danger">⚠ ${client.missedAppointments} Faltas</span>` : 
                        '<span style="color:green; font-weight: 600; font-size: 0.9rem;">★ Cliente</span>'}
                </td>
                <td>
                    ${client.userStatus === 'ACTIVE' ? 
                        `<button class="btn-outline-danger" onclick="adminSettingsApp.handleUpdateClientStatus(${client.clientId}, 'BANNED', this)">Banir</button>` : 
                        `<button class="btn btn-secondary btn-sm" onclick="adminSettingsApp.handleUpdateClientStatus(${client.clientId}, 'ACTIVE', this)">Desbloquear</button>`
                    }
                </td>
            </tr>
        `).join('');
    },

    renderPagination: function(data) {
        const container = document.getElementById('clients-pagination');
        if (data.totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        container.innerHTML = `
            <button class="page-btn" ${data.first ? 'disabled' : ''} onclick="adminSettingsApp.changeClientsPage(${adminSettingsApp.clientsPage - 1})">Anterior</button>
            <span style="font-size: 0.85rem; color: #666;">Página ${data.number + 1} de ${data.totalPages}</span>
            <button class="page-btn" ${data.last ? 'disabled' : ''} onclick="adminSettingsApp.changeClientsPage(${adminSettingsApp.clientsPage + 1})">Próxima</button>
        `;
    },

    changeClientsPage: function(page) {
        adminSettingsApp.clientsPage = page;
        adminSettingsApp.loadClients();
    },

    handleUpdateClientStatus: async function(clientId, status, btn) {
        const action = status === 'BANNED' ? 'banir' : 'desbloquear';
        const confirmed = await adminSettingsApp.showConfirm(`${status === 'BANNED' ? 'Banir' : 'Desbloquear'} Cliente`, `Tem certeza que deseja ${action} este cliente?`);
        if (!confirmed) return;

        adminSettingsApp.setLoading(btn, true);
        try {
            const response = await fetch(`/api/v1/admin/client/status/${clientId}/${status}`, { method: 'PATCH' });
            if (response.ok) {
                Toast.success(`Cliente ${status === 'BANNED' ? 'banido' : 'desbloqueado'} com sucesso!`);
                await adminSettingsApp.loadClients();
            }
        } finally {
            adminSettingsApp.setLoading(btn, false);
        }
    },

    // --- Salon Profile ---
    setupColorPicker: function() {
        const picker = document.querySelector('input[name="primaryColor"]');
        const text = document.querySelector('.color-text');
        if (picker && text) {
            picker.addEventListener('input', (e) => {
                text.value = e.target.value.toUpperCase();
            });
        }
    },

    handleSaveProfile: async function(event) {
        event.preventDefault();
        const form = event.target;
        const btn = form.querySelector('button[type="submit"]');
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        adminSettingsApp.setLoading(btn, true);
        try {
            const response = await fetch('/api/v1/admin/salon/profile', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                Toast.success('Configurações salvas com sucesso!');
                await adminSettingsApp.loadSalonProfile();
                // Also trigger a theme refresh globally if possible, or let it be for now.
                if (typeof App !== 'undefined' && App.initTheme) {
                    await App.initTheme();
                }
            }
        } finally {
            adminSettingsApp.setLoading(btn, false);
        }
    }
};

// adminSettingsApp.init() will be called by App.initPage()
