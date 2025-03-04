$(document).ready(function () {
    // Attach form submit handler
    $('#loginForm').on('submit', function (e) {
        e.preventDefault(); // Prevent default form submission

        const email = $('#email').val().trim();
        const password = $('#password').val().trim();

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
                // Store token and role in localStorage
                localStorage.setItem('token', response.token);
                localStorage.setItem('userRole', response.role);

                showSuccessPopup('Login successful!');

                // Redirect based on user role
                if (response.role === 'ADMIN') {
                    window.location.href = 'admin-dashboard.html';
                } else {
                    window.location.href = 'my-adoptions.html';
                }
            },
            error: function (xhr) {
                const errorMessage = xhr.responseText || 'Login failed. Please check your credentials.';
                showErrorPopup(`Login failed: ${errorMessage}`);
            }
        });
    });

    // Function to show error popups
    function showErrorPopup(message) {
        const errorPopup = $('<div class="alert alert-danger popup-message"></div>').text(message);
        $('body').append(errorPopup);
        setTimeout(function () {
            errorPopup.fadeOut(function () { errorPopup.remove(); });
        }, 3000);
    }

    // Function to show success popups
    function showSuccessPopup(message) {
        const successPopup = $('<div class="alert alert-success popup-message"></div>').text(message);
        $('body').append(successPopup);
        setTimeout(function () {
            successPopup.fadeOut(function () { successPopup.remove(); });
        }, 3000);
    }
});
