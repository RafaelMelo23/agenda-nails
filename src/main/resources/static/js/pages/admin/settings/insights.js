export const InsightsModule = {
    loadSalonRevenue: async function() {
        try {
            const res = await fetch('/api/v1/admin/insight/salon/revenue');
            if (res.ok) {
                const data = await res.json();
                const mRev = document.getElementById('insight-monthly-revenue');
                if (mRev) mRev.innerText = `R$ ${(data.monthlyRevenue || 0).toLocaleString('pt-BR', {minimumFractionDigits: 2})}`;
                
                const wRev = document.getElementById('insight-weekly-revenue');
                if (wRev) wRev.innerText = `R$ ${(data.weeklyRevenue || 0).toLocaleString('pt-BR', {minimumFractionDigits: 2})}`;
                
                const mApp = document.getElementById('insight-monthly-appointments');
                if (mApp) mApp.innerText = data.monthlyAppointmentsCount || 0;
                
                const aTic = document.getElementById('insight-average-ticket');
                if (aTic) aTic.innerText = `R$ ${(data.averageTicket || 0).toLocaleString('pt-BR', {minimumFractionDigits: 2})}`;
            } else {
                const fields = ['insight-monthly-revenue', 'insight-weekly-revenue', 'insight-monthly-appointments', 'insight-average-ticket'];
                fields.forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.innerText = 'Erro';
                });
            }
        } catch (e) {
            console.error('Failed to load salon revenue', e);
        }
    }
};