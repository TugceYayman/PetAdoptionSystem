$(document).ready(function () {
    // Hide all pages by default
    $('.page').hide();

    // Show login page initially
    showPage('loginPage');

    // Check if user is already logged in (e.g., from a previous session)
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('userRole');
    if (token && role) {
        navigateToDashboard(role);
    }

    function showPage(pageId) {
        $('.page').hide();
        $('#' + pageId).show();
    }

    // Handle navigation based on role
    function navigateToDashboard(role) {
        if (role === 'ADMIN') {
            showPage('adminDashboardPage');
            loadAdminData(); // Load admin-specific data
        } else if (role === 'CSR') {
            showPage('csrDashboardPage');
            loadCsrData(); // Load CSR data
        } else if (role === 'SUPPORT') {
            showPage('supportDashboardPage');
            loadSupportData(); // Load Support Engineer data
        } else if (role === 'NETWORK') {
            showPage('networkDashboardPage');
            loadNetworkData(); // Load Network Engineer data
        } else {
            showPage('petListPage');  // Default for regular user
            loadPetsForUser();
        }
    }

    window.showPage = showPage; // Make globally accessible
    window.navigateToDashboard = navigateToDashboard;
});
