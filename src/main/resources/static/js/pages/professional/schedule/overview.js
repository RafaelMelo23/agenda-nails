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
            'PENDING': { label: 'Pendente', class: 'status-pending' },
            'CONFIRMED': { label: 'Confirmado', class: 'status-confirmed' },
            'FINISHED': { label: 'Finalizado', class: 'status-finished' },
            'CANCELLED': { label: 'Cancelado', class: 'status-cancelled' },
            'MISSED': { label: 'Faltou', class: 'status-missed' }
        };

        const formatDate = (dateStr) => {
            const date = new Date(dateStr);
            const d = date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
            const t = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
            return { date: d, time: t };
        };

        list.innerHTML = appointments.map(a => {
            const s = statusMap[a.status] || { label: a.status, class: '' };
            const dateTime = formatDate(a.startDateAndTime);
            return `
                <tr>
                    <td data-label="Data/Hora">
                        <div class="ov-info-group">
                            <span class="ov-main-text">${dateTime.date}</span>
                            <span class="ov-sub-text">${dateTime.time}</span>
                        </div>
                    </td>
                    <td data-label="Cliente">
                        <div class="ov-info-group">
                            <span class="ov-main-text">${a.clientName}</span>
                            <span class="ov-sub-text">${a.clientPhoneNumber || 'Sem telefone'}</span>
                        </div>
                    </td>
                    <td data-label="Serviço">
                        <div class="ov-info-group">
                            <span class="ov-main-text">${a.mainServiceName}</span>
                            <span class="ov-sub-text">Duração: ${Math.floor(a.mainServiceDurationInSeconds / 60)} min</span>
                        </div>
                    </td>
                    <td data-label="Valor">
                        <div class="ov-info-group">
                            <span class="ov-main-text">R$ ${a.totalValue.toFixed(2).replace('.', ',')}</span>
                            ${a.addOns && a.addOns.length > 0 ? `<span class="ov-sub-text">+ ${a.addOns.length} adicionais</span>` : ''}
                        </div>
                    </td>
                    <td data-label="Status">
                        <span class="status-badge ${s.class}">${s.label}</span>
                    </td>
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
