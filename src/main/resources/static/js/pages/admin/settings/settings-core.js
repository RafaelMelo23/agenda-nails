export const CoreModule = {
    init: async function() {
        const contentArea = document.getElementById('settings-content-area');
        const modalsArea = document.getElementById('settings-modals-area');

        if (!contentArea || !modalsArea) {
            return;
        }

        // Sequential loading to avoid race conditions and ensure DOM stability
        const fragments = ['modals', 'professionals', 'clients', 'salon', 'insights'];
        
        for (const tabId of fragments) {
            try {
                const response = await fetch(`/pages/admin/settings-fragments/${tabId}.html`);
                if (response.ok) {
                    const html = await response.text();
                    if (tabId === 'modals') {
                        modalsArea.innerHTML = html;
                    } else {
                        contentArea.insertAdjacentHTML('beforeend', html);
                    }
                }
            } catch (e) {
                console.error(`Error loading fragment ${tabId}:`, e);
            }
        }

        // Wait for next frame to ensure DOM is fully painted
        await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));

        adminSettingsApp.setupColorPicker();
        
        const hash = window.location.hash.substring(1) || 'professionals';
        this.switchTabUI(hash);
        this.loadTabData(hash);
    },

    switchTab: async function(tabId) {
        window.location.hash = tabId;
        this.switchTabUI(tabId);
        this.loadTabData(tabId);
    },

    switchTabUI: function(tabId) {
        document.querySelectorAll('.tab-btn').forEach(btn => {
            const onClick = btn.getAttribute('onclick') || '';
            btn.classList.toggle('active', onClick.includes(tabId));
        });

        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.toggle('active', content.id === `tab-${tabId}`);
        });
    },

    loadTabData: function(tabId) {
        if (tabId === 'professionals') {
            adminSettingsApp.loadProfessionals();
        } else if (tabId === 'clients') {
            adminSettingsApp.loadClients();
        } else if (tabId === 'insights') {
            adminSettingsApp.loadSalonRevenue();
        } else if (tabId === 'salon') {
            adminSettingsApp.loadSalonProfile();
        }
    },

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
        if (!name) return '??';
        return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    }
};