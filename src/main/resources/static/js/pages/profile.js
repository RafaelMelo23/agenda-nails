document.addEventListener('DOMContentLoaded', () => {
    const token = Auth.getToken();
    if (!token) {
        window.location.href = '/entrar';
        return;
    }

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-name').innerText = payload.sub || 'Cliente';
    } catch (e) {
        console.error('Error decoding token:', e);
    }

    const tabUpcoming = document.getElementById('tab-upcoming');
    const tabHistory = document.getElementById('tab-history');
    const listUpcoming = document.getElementById('list-upcoming');
    const listHistory = document.getElementById('list-history');

    tabUpcoming.addEventListener('click', () => {
        tabUpcoming.classList.add('active');
        tabHistory.classList.remove('active');
        listUpcoming.classList.remove('hidden');
        listHistory.classList.add('hidden');
    });

    tabHistory.addEventListener('click', () => {
        tabHistory.classList.add('active');
        tabUpcoming.classList.remove('active');
        listHistory.classList.remove('hidden');
        listUpcoming.classList.add('hidden');
    });

});

async function fetchAppointments() {

}
