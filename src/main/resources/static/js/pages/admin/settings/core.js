export const CoreModule = {
    init: async function() {
        // Load Modals
        try {
            const res = await fetch('/pages/admin/settings-fragments/modals.html');
            if (res.ok) {
                const html = await res.text();
                document.getElementById('settings-modals-area').innerHTML = html;
            }
        } catch (e) {
            console.error('Error loading modals', e);
        }

        // Core initializations
        adminSettingsApp.loadSalonProfile();
        adminSettingsApp.setupColorPicker();
        
        // Restore active tab from hash or default to professionals
        const hash = window.location.hash.substring(1) || 'professionals';
        await adminSettingsApp.switchTab(hash);
    },

    switchTab: async function(tabId) {
        // Update URL Hash
        window.location.hash = tabId;

        // Update Buttons
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('onclick').includes(tabId));
        });

        const contentArea = document.getElementById('settings-content-area');
        let contentEl = document.getElementById(`tab-${tabId}`);
        
        if (!contentEl) {
            try {
                const response = await fetch(`/pages/admin/settings-fragments/${tabId}.html`);
                if (response.ok) {
                    const html = await response.text();
                    contentArea.insertAdjacentHTML('beforeend', html);
                }
            } catch (e) {
                console.error('Error loading fragment', e);
            }
        }

        // Update Visibility
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.toggle('active', content.id === `tab-${tabId}`);
        });

        // Specific Tab Actions
        if (tabId === 'professionals') {
            adminSettingsApp.loadProfessionals();
        } else if (tabId === 'clients') {
            adminSettingsApp.loadClients();
        } else if (tabId === 'insights') {
            adminSettingsApp.loadSalonRevenue();
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