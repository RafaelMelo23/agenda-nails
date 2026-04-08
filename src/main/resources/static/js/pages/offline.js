const offlinePage = {
    init: function() {
        this.renderOfflineInfo();
    },

    renderOfflineInfo: function() {
        const salon = App.salon;
        if (!salon) return;

        const warningMsgEl = document.getElementById('offline-warning-message');
        if (warningMsgEl && salon.warningMessage) {
            warningMsgEl.innerText = salon.warningMessage;
        }

        const whatsappLink = document.getElementById('offline-whatsapp-link');
        if (whatsappLink && salon.comercialPhone) {
            const cleanPhone = salon.comercialPhone.replace(/\D/g, '');
            whatsappLink.href = `https://wa.me/${cleanPhone}`;
        }
        
        // Hide header and footer for cleaner look if needed
        // document.body.classList.add('tenant-error');
    }
};

function initOffline() {
    offlinePage.init();
}

// Global exposure for main.js initPage
window.initOffline = initOffline;
