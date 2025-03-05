$(document).ready(function () {

    // When the document is ready, show login page initially
    switchToPage('loginPage');

    // Attach event handler for the "create account" link in login page
    $('#goToRegister').on('click', function (e) {
        e.preventDefault();
        switchToPage('registerPage');
    });

    // Attach event handler for the "login here" link in register page
    $('#goToLogin').on('click', function (e) {
        e.preventDefault();
        switchToPage('loginPage');
    });

    // Attach form submit handler for login
    $('#loginForm').on('submit', function (e) {
        e.preventDefault();

        const email = $('#loginEmail').val().trim();
        const password = $('#loginPassword').val().trim();

        if (!email || !password) {
            showErrorPopup('Please enter both email and password.');
            return;
        }

        $.ajax({
            url: '/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email: email, password: password }),
            success: function (response) {
                localStorage.setItem('token', response.token);
                localStorage.setItem('userRole', response.role);

                showSuccessPopup('Login successful!');

                if (response.role === 'ADMIN') {
                    switchToPage('adminDashboardPage');
                    loadAdminData();
                } else {
                    switchToPage('petListPage');
                    loadPets();
                }
            },
            error: function (xhr) {
                const errorMessage = xhr.responseText || 'Login failed. Please check your credentials.';
                showErrorPopup(`Login failed: ${errorMessage}`);
            }
        });
    });

    // Function to switch pages (hide all, show selected one)
    function switchToPage(pageId) {
        $('.page').hide();
        $('#' + pageId).removeClass('d-none').show();
    }

    // Error popup helper
    function showErrorPopup(message) {
        const errorPopup = $('<div class="alert alert-danger popup-message"></div>').text(message);
        $('body').append(errorPopup);
        setTimeout(function () {
            errorPopup.fadeOut(function () { errorPopup.remove(); });
        }, 3000);
    }

    function showSuccessPopup(message) {
        const successPopup = $('<div class="alert alert-success popup-message"></div>').text(message);
        $('body').append(successPopup);
        setTimeout(function () {
            successPopup.fadeOut(function () { successPopup.remove(); });
        }, 3000);
    }

    // Example functions to load data (optional if you have them in your index.js/admin-dashboard.js)
    function loadPets() {
        $.ajax({
            url: '/api/pets',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            success: function (pets) {
                renderPets(pets);
            },
            error: function () {
                showErrorPopup('Failed to load pets.');
            }
        });
    }

    function loadAdminData() {
        $.ajax({
            url: '/api/pets',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            success: function (pets) {
                renderPetsForAdmin(pets);
            }
        });

        $.ajax({
            url: '/api/adoptions',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
            success: function (adoptions) {
                renderAdoptions(adoptions);
            }
        });
    }
});
