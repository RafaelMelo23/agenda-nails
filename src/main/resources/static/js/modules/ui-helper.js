
const Toast = {
    container: null,

    init: function() {
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
    },

    show: function(message, type = 'error', duration = 5000) {
        if (!this.container) this.init();

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const content = document.createElement('span');
        content.innerText = message;

        const closeBtn = document.createElement('button');
        closeBtn.innerHTML = '&times;';
        closeBtn.style.background = 'none';
        closeBtn.style.border = 'none';
        closeBtn.style.fontSize = '20px';
        closeBtn.style.cursor = 'pointer';
        closeBtn.onclick = () => this.remove(toast);

        toast.appendChild(content);
        toast.appendChild(closeBtn);
        this.container.appendChild(toast);

        setTimeout(() => this.remove(toast), duration);
    },

    remove: function(toast) {
        if (!toast.parentNode) return;
        toast.classList.add('toast-fade-out');
        toast.onanimationend = () => {
            if (toast.parentNode) toast.parentNode.removeChild(toast);
        };
    },

    error: function(msg) { this.show(msg, 'error'); },
    success: function(msg) { this.show(msg, 'success'); }
};

const UI = {
    setLoading: function(btn, loading, text) {
        if (loading) {
            btn.setAttribute('data-original-text', btn.innerText);
            btn.innerText = text || 'Carregando...';
            btn.disabled = true;
            btn.style.opacity = '0.7';
        } else {
            btn.innerText = btn.getAttribute('data-original-text') || text;
            btn.disabled = false;
            btn.style.opacity = '1';
        }
    },

    showToast: function(message, type) {
        Toast.show(message, type);
    }
};
