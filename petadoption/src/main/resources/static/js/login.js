$(document).ready(function () {
    // ✅ Show login page initially
    switchToPage('loginPage');

    $('#goToRegister').on('click', function (e) {
        e.preventDefault();
        switchToPage('registerPage');
    });

    $('#goToLogin').on('click', function (e) {
        e.preventDefault();
        switchToPage('loginPage');
    });

    // ✅ Handle login form submission
    $('#loginForm').on('submit', function (e) {
        e.preventDefault();

        const email = $('#loginEmail').val().trim();
        const password = $('#loginPassword').val().trim();

        if (!email || !password) {
            showErrorPopup('⚠️ Please enter both email and password.');
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

                showSuccessPopup('✅ Login successful!');

                if (response.role === 'ADMIN') {
                    switchToPage('adminDashboardPage');
                    console.log("✅ Admin logged in, fetching dashboard data...");

                    // ✅ Fetch admin-specific data
                    setTimeout(() => {
                        fetchPets();
                        fetchAdoptions();
                    }, 500);
                } else if (response.role === 'USER') {
                    switchToPage('userDashboardPage');
                    console.log("✅ User logged in, fetching available pets...");
                    
                    // ✅ Fetch user-specific data
                    setTimeout(() => {
                        showPets();
                    }, 500);
                } else {
                    showErrorPopup('❌ Unknown role. Please contact support.');
                }
            },
            error: function (xhr) {
                const errorMessage = xhr.responseText || '❌ Login failed. Please check your credentials.';
                showErrorPopup(`Login failed: ${errorMessage}`);
            }
        });
    });

    function switchToPage(pageId) {
        $('.page').hide();
        $('#' + pageId).removeClass('d-none').show();
    }

    function showErrorPopup(message) {
        alert(message);
    }

    function showSuccessPopup(message) {
        alert(message);
    }
});
