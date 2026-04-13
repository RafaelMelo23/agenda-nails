export const AppointmentsModule = {
    loadAppointmentsOverview: async function(page = 0) {
        const list = document.getElementById('appointments-overview-list');
        const profSelect = document.getElementById('filter-appt-professional');
        const statusSelect = document.getElementById('filter-appt-status');
        const dateInput = document.getElementById('filter-appt-date');

        if (!list) return;

        const professionalId = profSelect ? profSelect.value : '';
        const status = statusSelect ? statusSelect.value : '';
        const date = dateInput ? dateInput.value : '';

        list.innerHTML = '<tr><td colspan="6" class="empty-state">Carregando agendamentos...</td></tr>';

        try {
            const params = new URLSearchParams();
            if (professionalId) params.append('professionalId', professionalId);
            if (status) params.append('status', status);
            if (date) params.append('date', date);
            params.append('page', page);
            params.append('size', 10);

            const res = await fetch(`/api/v1/admin/appointments/overview?${params.toString()}`);
            if (res.ok) {
                const data = await res.json();
                this.renderAppointmentsOverview(data.content);
                this.renderAppointmentsPagination(data);
            } else {
                list.innerHTML = `<tr><td colspan="6" class="empty-state">Erro ao carregar agendamentos (Status: ${res.status}).</td></tr>`;
            }
        } catch (error) {
            list.innerHTML = '<tr><td colspan="6" class="empty-state">Erro ao carregar agendamentos.</td></tr>';
        }
    },

    renderAppointmentsOverview: function(appointments) {
        const list = document.getElementById('appointments-overview-list');
        if (!list) return;

        if (appointments.length === 0) {
            list.innerHTML = '<tr><td colspan="6" class="empty-state">Nenhum agendamento encontrado para os filtros selecionados.</td></tr>';
            return;
        }

        const statusMap = {
            'PENDING': { label: 'Pendente', class: 'badge-pending' },
            'CONFIRMED': { label: 'Confirmado', class: 'badge-success' },
            'FINISHED': { label: 'Finalizado', class: 'badge-success' },
            'CANCELLED': { label: 'Cancelado', class: 'badge-danger' },
            'MISSED': { label: 'Faltou', class: 'badge-danger' }
        };

        const formatDate = (dateStr) => {
            const date = new Date(dateStr);
            return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
        };

        list.innerHTML = appointments.map(a => {
            const s = statusMap[a.status] || { label: a.status, class: '' };
            return `
                <tr>
                    <td>${formatDate(a.startDateAndTime)}</td>
                    <td data-label="Cliente">${a.clientName}</td>
                    <td data-label="Profissional">${a.professionalName}</td>
                    <td data-label="Serviço">${a.mainServiceName}</td>
                    <td data-label="Valor">R$ ${a.totalValue.toFixed(2).replace('.', ',')}</td>
                    <td data-label="Status"><span class="badge ${s.class}">${s.label}</span></td>
                </tr>
            `;
        }).join('');
    },

    renderAppointmentsPagination: function(data) {
        const container = document.getElementById('appointments-overview-pagination');
        if (!container) return;

        if (data.totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        const currentPage = data.number;
        const totalPages = data.totalPages;
        let html = '';

        // Previous button
        html += `<button class="page-btn" ${data.first ? 'disabled' : ''} onclick="adminSettingsApp.loadAppointmentsOverview(${currentPage - 1})">Anterior</button>`;

        // Page numbers
        const maxVisible = 5;
        let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages - 1, start + maxVisible - 1);

        if (end - start + 1 < maxVisible) {
            start = Math.max(0, end - maxVisible + 1);
        }

        if (start > 0) {
            html += `<button class="page-btn" onclick="adminSettingsApp.loadAppointmentsOverview(0)">1</button>`;
            if (start > 1) html += `<span style="align-self: flex-end; padding-bottom: 5px;">...</span>`;
        }

        for (let i = start; i <= end; i++) {
            const isActive = i === currentPage;
            html += `<button class="page-btn ${isActive ? 'active' : ''}" 
                        style="${isActive ? 'border-color: var(--primary); color: var(--primary); background: var(--primary-light);' : ''}" 
                        onclick="adminSettingsApp.loadAppointmentsOverview(${i})">${i + 1}</button>`;
        }

        if (end < totalPages - 1) {
            if (end < totalPages - 2) html += `<span style="align-self: flex-end; padding-bottom: 5px;">...</span>`;
            html += `<button class="page-btn" onclick="adminSettingsApp.loadAppointmentsOverview(${totalPages - 1})">${totalPages}</button>`;
        }

        html += `<button class="page-btn" ${data.last ? 'disabled' : ''} onclick="adminSettingsApp.loadAppointmentsOverview(${currentPage + 1})">Próxima</button>`;

        container.innerHTML = html;
    },

    populateProfessionalsFilter: async function() {
        const select = document.getElementById('filter-appt-professional');
        if (!select) return;

        try {
            const res = await fetch('/api/v1/admin/professional');
            if (res.ok) {
                const professionals = await res.json();
                const currentVal = select.value;
                select.innerHTML = '<option value="">Todos os Profissionais</option>' + 
                    professionals.map(p => `<option value="${p.id}">${p.name}</option>`).join('');
                select.value = currentVal;
            }
        } catch (e) {}
    }
};
