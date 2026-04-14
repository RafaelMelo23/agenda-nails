export const OverviewModule = {
    loadOverview: async function(page = 0) {
        const list = document.getElementById('overview-list');
        const statusSelect = document.getElementById('filter-ov-status');
        const dateInput = document.getElementById('filter-ov-date');

        if (!list) return;

        const status = statusSelect ? statusSelect.value : '';
        const date = dateInput ? dateInput.value : '';

        list.innerHTML = '<tr><td colspan="5" class="empty-state">Carregando...</td></tr>';

        try {
            const params = new URLSearchParams();
            if (status) params.append('status', status);
            if (date) params.append('date', date);
            params.append('page', page);
            params.append('size', 10);

            const res = await fetch(`/api/v1/professional/appointments/overview?${params.toString()}`);
            if (res.ok) {
                const data = await res.json();
                this.renderOverview(data.content);
                this.renderPagination(data);
            } else {
                list.innerHTML = `<tr><td colspan="5" class="empty-state">Erro ao carregar agendamentos (Status: ${res.status}).</td></tr>`;
            }
        } catch (error) {
            list.innerHTML = '<tr><td colspan="5" class="empty-state">Erro ao carregar histórico.</td></tr>';
        }
    },

    renderOverview: function(appointments) {
        const list = document.getElementById('overview-list');
        if (!list) return;

        if (appointments.length === 0) {
            list.innerHTML = '<tr><td colspan="5" class="empty-state">Nenhum agendamento encontrado para os filtros selecionados.</td></tr>';
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
                    <td data-label="Data/Hora">${formatDate(a.startDateAndTime)}</td>
                    <td data-label="Cliente">${a.clientName}</td>
                    <td data-label="Serviço">${a.mainServiceName}</td>
                    <td data-label="Valor">R$ ${a.totalValue.toFixed(2).replace('.', ',')}</td>
                    <td data-label="Status"><span class="badge ${s.class}">${s.label}</span></td>
                </tr>
            `;
        }).join('');
    },

    renderPagination: function(data) {
        const container = document.getElementById('overview-pagination');
        if (!container) return;

        if (data.totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        container.innerHTML = `
            <button class="page-btn" ${data.first ? 'disabled' : ''} onclick="professionalScheduleApp.loadOverview(${data.number - 1})">Anterior</button>
            <span style="font-size: 0.85rem; color: #666;">Página ${data.number + 1} de ${data.totalPages}</span>
            <button class="page-btn" ${data.last ? 'disabled' : ''} onclick="professionalScheduleApp.loadOverview(${data.number + 1})">Próxima</button>
        `;
    }
};
