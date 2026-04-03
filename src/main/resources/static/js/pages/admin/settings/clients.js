export const ClientsModule = {
    clientsPage: 0,
    clientsSearchTimeout: null,
    crmSearchTimeout: null,

    debounceSearchClients: function() {
        clearTimeout(this.clientsSearchTimeout);
        this.clientsSearchTimeout = setTimeout(() => {
            this.clientsPage = 0;
            this.loadClients();
        }, 500);
    },

    loadClients: async function() {
        const searchInput = document.getElementById('client-search');
        if (!searchInput) return;
        const searchTerm = searchInput.value;
        try {
            const response = await fetch(`/api/v1/admin/client?name=${encodeURIComponent(searchTerm)}&page=${this.clientsPage}&size=10`);
            if (response.ok) {
                const data = await response.json();
                this.renderClients(data.content);
                this.renderPagination(data);
            }
        } catch (error) {
            console.error('Error loading clients:', error);
        }
    },

    renderClients: function(clients) {
        const list = document.getElementById('clients-list');
        if (!list) return;
        if (clients.length === 0) {
            list.innerHTML = '<tr><td colspan="4" class="empty-state">Nenhum cliente encontrado.</td></tr>';
            return;
        }

        list.innerHTML = clients.map(client => `
            <tr>
                <td>
                    <strong style="${client.userStatus === 'BANNED' ? 'text-decoration: line-through; opacity: 0.6;' : ''}">${client.fullName}</strong>
                </td>
                <td data-label="Telefone">${client.phoneNumber || 'N/A'}</td>
                <td data-label="Histórico">
                    <div class="client-history-cell" style="display: flex; align-items: center; gap: 8px;">
                        ${client.missedAppointments > 0 ? 
                            `<span class="badge badge-danger">⚠ ${client.missedAppointments} Faltas</span>` : 
                            '<span style="color:green; font-weight: 600; font-size: 0.85rem;">★ Regular</span>'}
                        <button class="btn btn-secondary btn-sm" onclick="adminSettingsApp.openHistoryModal(${client.clientId}, '${client.fullName}')" title="Ver Histórico Completo">Agendamentos</button>
                    </div>
                </td>
                <td data-label="Ação">
                    ${client.userStatus === 'ACTIVE' ? 
                        `<button class="btn-outline-danger btn-sm" onclick="adminSettingsApp.handleUpdateClientStatus(${client.clientId}, 'BANNED', this)">Banir</button>` : 
                        `<button class="btn btn-secondary btn-sm" onclick="adminSettingsApp.handleUpdateClientStatus(${client.clientId}, 'ACTIVE', this)">Desbloquear</button>`
                    }
                </td>
            </tr>
        `).join('');
    },

    openHistoryModal: function(clientId, name) {
        document.getElementById('client-history-modal-title').textContent = `Histórico: ${name}`;
        document.getElementById('client-history-modal').classList.remove('hidden');
        this.currentHistoryClientId = clientId;
        this.loadAppointmentHistory(clientId, 0);
    },

    closeHistoryModal: function() {
        document.getElementById('client-history-modal').classList.add('hidden');
        this.currentHistoryClientId = null;
    },

    loadAppointmentHistory: async function(clientId, page) {
        const container = document.getElementById('client-history-container');
        if (!container) return;
        container.innerHTML = '<p class="empty-state">Carregando histórico...</p>';
        try {
            const response = await fetch(`/api/v1/admin/client/${clientId}/appointments?page=${page}&size=5&sort=startDate,desc`);
            if (response.ok) {
                const data = await response.json();
                this.renderAppointmentHistory(data);
                this.renderHistoryPagination(data);
            }
        } catch (error) {
            container.innerHTML = '<p class="empty-state">Erro ao carregar histórico.</p>';
        }
    },

    renderAppointmentHistory: function(data) {
        const container = document.getElementById('client-history-container');
        if (!container) return;
        const appointments = data.content;
        
        if (!appointments || appointments.length === 0) {
            container.innerHTML = '<p class="empty-state">Este cliente ainda não possui agendamentos.</p>';
            return;
        }

        const formatDate = (dateStr) => {
            const date = new Date(dateStr);
            return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        };

        const statusMap = {
            'SCHEDULED': { label: 'Agendado', class: 'badge-success' },
            'CONFIRMED': { label: 'Confirmado', class: 'badge-success' },
            'COMPLETED': { label: 'Finalizado', class: 'badge-success' },
            'CANCELLED': { label: 'Cancelado', class: 'badge-danger' },
            'MISSED': { label: 'Faltou', class: 'badge-danger' }
        };

        container.innerHTML = `
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Data</th>
                        <th>Serviço</th>
                        <th>Profissional</th>
                        <th>Valor</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${appointments.map(a => {
                        const s = statusMap[a.status] || { label: a.status, class: '' };
                        const services = [a.mainServiceName, ...(a.addOnServiceNames || [])].join(', ');
                        return `
                            <tr>
                                <td data-label="Data">${formatDate(a.startDateAndTime)}</td>
                                <td data-label="Serviço">
                                    <span title="${services}">${a.mainServiceName}${a.addOnServiceNames?.length ? ' (+)' : ''}</span>
                                </td>
                                <td data-label="Profissional">${a.professionalName}</td>
                                <td data-label="Valor">R$ ${a.totalValue?.toFixed(2) || '0,00'}</td>
                                <td data-label="Status"><span class="badge ${s.class}">${s.label}</span></td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        `;
    },

    renderHistoryPagination: function(data) {
        const container = document.getElementById('history-pagination');
        if (!container) return;
        if (data.totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        container.innerHTML = `
            <button class="page-btn" ${data.first ? 'disabled' : ''} onclick="adminSettingsApp.loadAppointmentHistory(${this.currentHistoryClientId}, ${data.number - 1})">Anterior</button>
            <span style="font-size: 0.85rem; color: #666;">Página ${data.number + 1} de ${data.totalPages}</span>
            <button class="page-btn" ${data.last ? 'disabled' : ''} onclick="adminSettingsApp.loadAppointmentHistory(${this.currentHistoryClientId}, ${data.number + 1})">Próxima</button>
        `;
    },

    renderPagination: function(data) {
        const container = document.getElementById('clients-pagination');
        if (!container) return;
        if (data.totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        container.innerHTML = `
            <button class="page-btn" ${data.first ? 'disabled' : ''} onclick="adminSettingsApp.changeClientsPage(${this.clientsPage - 1})">Anterior</button>
            <span style="font-size: 0.85rem; color: #666;">Página ${data.number + 1} de ${data.totalPages}</span>
            <button class="page-btn" ${data.last ? 'disabled' : ''} onclick="adminSettingsApp.changeClientsPage(${this.clientsPage + 1})">Próxima</button>
        `;
    },

    changeClientsPage: function(page) {
        this.clientsPage = page;
        this.loadClients();
    },

    handleUpdateClientStatus: async function(clientId, status, btn) {
        const action = status === 'BANNED' ? 'banir' : 'desbloquear';
        const confirmed = await this.showConfirm(`${status === 'BANNED' ? 'Banir' : 'Desbloquear'} Cliente`, `Tem certeza que deseja ${action} este cliente?`);
        if (!confirmed) return;

        this.setLoading(btn, true);
        try {
            const response = await fetch(`/api/v1/admin/client/status/${clientId}/${status}`, { method: 'PATCH' });
            if (response.ok) {
                Toast.success(`Cliente ${status === 'BANNED' ? 'banido' : 'desbloqueado'} com sucesso!`);
                await this.loadClients();
            }
        } finally {
            this.setLoading(btn, false);
        }
    },

    // --- CRM ---
    debounceSearchCrmClients: function() {
        clearTimeout(this.crmSearchTimeout);
        this.crmSearchTimeout = setTimeout(() => {
            this.searchCrmClients();
        }, 500);
    },

    searchCrmClients: async function() {
        const searchInput = document.getElementById('crm-client-search');
        if (!searchInput) return;
        const searchTerm = searchInput.value;
        const resultsContainer = document.getElementById('crm-client-results');
        
        if (!searchTerm || searchTerm.length < 3) {
            resultsContainer.style.display = 'none';
            return;
        }

        try {
            const response = await fetch(`/api/v1/admin/client?name=${encodeURIComponent(searchTerm)}&page=0&size=5`);
            if (response.ok) {
                const data = await response.json();
                const clients = data.content;
                
                if (clients.length === 0) {
                    resultsContainer.innerHTML = '<div style="padding: 10px; color: var(--text-muted);">Nenhum cliente encontrado.</div>';
                } else {
                    resultsContainer.innerHTML = clients.map(c => `
                        <div style="padding: 10px; border-bottom: 1px solid var(--border); cursor: pointer; hover: background: var(--bg-body);"
                             onclick="adminSettingsApp.selectCrmClient(${c.clientId}, '${c.fullName}')">
                            <strong>${c.fullName}</strong> <span style="color: var(--text-muted); font-size: 0.85rem;">${c.phoneNumber || ''}</span>
                        </div>
                    `).join('');
                }
                resultsContainer.style.display = 'block';
            }
        } catch (error) {
            console.error('Error searching crm clients:', error);
        }
    },

    selectCrmClient: function(clientId, clientName) {
        document.getElementById('crm-client-search').value = clientName;
        document.getElementById('crm-client-results').style.display = 'none';
        this.loadClientCrmInfo(clientId);
    },

    loadClientCrmInfo: async function(clientId) {
        const detailsContainer = document.getElementById('crm-client-details');
        if (!detailsContainer) return;
        detailsContainer.style.display = 'block';
        
        const fields = ['crm-name', 'crm-phone', 'crm-total-spent', 'crm-last-visit', 'crm-completed', 'crm-canceled', 'crm-missed'];
        fields.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.innerText = 'Carregando...';
        });

        try {
            const response = await fetch(`/api/v1/admin/insight/clients/${clientId}`);
            if (response.ok) {
                const data = await response.json();
                document.getElementById('crm-name').innerText = data.name || '-';
                document.getElementById('crm-phone').innerText = data.phoneNumber || '-';
                document.getElementById('crm-total-spent').innerText = `R$ ${(data.totalSpent || 0).toLocaleString('pt-BR', {minimumFractionDigits: 2})}`;
                
                let visitDate = '-';
                if (data.lastVisitDate) {
                    visitDate = new Date(data.lastVisitDate).toLocaleDateString('pt-BR');
                }
                document.getElementById('crm-last-visit').innerText = visitDate;
                
                document.getElementById('crm-completed').innerText = data.completedAppointments || 0;
                document.getElementById('crm-canceled').innerText = data.canceledAppointments || 0;
                document.getElementById('crm-missed').innerText = data.missedAppointments || 0;
            } else {
                fields.forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.innerText = 'Erro';
                });
            }
        } catch (error) {
            console.error('Failed to load client CRM info', error);
        }
    }
};