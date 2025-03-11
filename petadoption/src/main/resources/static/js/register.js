$(document).ready(function () {
    console.log("🚀 Register Form Script Loaded!");

    $('#registerForm').on('submit', async function (e) {
        e.preventDefault();
        clearRegisterErrors();
        clearRegisterSuccess();

        const name = $('#registerName').val().trim();
        const email = $('#registerEmail').val().trim();
        const password = $('#registerPassword').val().trim();

        let errorMessages = [];

        console.log("🔍 Validating form fields...");

        // 🚨 Show "All fields are required" if everything is empty
        if (!name && !email && !password) {
            console.warn("⚠️ All fields are empty!");
            showRegisterError(["⚠️ All fields are required."]);
            return;
        }

        // ✅ Validate Each Field Individually If At Least One Is Filled
        if (!name) {
            console.warn("⚠️ Name field is empty!");
            errorMessages.push("⚠️ Name is required.");
        }

        if (!email) {
            console.warn("⚠️ Email field is empty!");
            errorMessages.push("⚠️ Email is required.");
        } else if (!validateEmail(email)) {
            console.warn("⚠️ Invalid email format!");
            errorMessages.push("⚠️ Invalid email format.");
        }

        if (!password) {
            console.warn("⚠️ Password field is empty!");
            errorMessages.push("⚠️ Password is required.");
        }

        if (errorMessages.length > 0) {
            console.warn("❌ Form has validation errors. Stopping submission.");
            showRegisterError(errorMessages);
            return;
        }

        console.log("✅ Client-side validation passed. Submitting form...");

        try {
            const response = await fetch('http://localhost:8081/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                console.error("❌ Backend error:", data.message);
                showRegisterError([`❌ ${data.message}`]);
                return;
            }

            showSuccessPopup(`🎉 ${data.message}`);

            // ✅ Clear Fields After Successful Registration
            $('#registerName, #registerEmail, #registerPassword').val('');

            // ✅ Redirect to login after 1.5 seconds
            setTimeout(() => switchToPage('loginPage'), 1500);

        } catch (error) {
            console.error("❌ Network Error:", error);
            showRegisterError(['❌ Unable to connect to server. Please try again later.']);
        }
    });

    // ✅ Show Errors in a Single Error Box Above the Form
    function showRegisterError(messages) {
        console.log("❌ Displaying errors:", messages);

        let errorContainer = $('#registerErrorContainer');
        if (errorContainer.length === 0) {
            errorContainer = $('<div id="registerErrorContainer" class="alert alert-danger text-danger mt-2"></div>');
            $('#registerForm').before(errorContainer);
        }

        errorContainer.html(messages.map(msg => `<div>${msg}</div>`).join(""));
        errorContainer.removeClass('d-none').show();
    }

    // ✅ Show Green Success Popup
    function showSuccessPopup(message) {
        console.log(`✅ Showing success popup: ${message}`);
        const popup = $('<div class="popup-message alert alert-success"></div>')
            .text(message)
            .css({
                "position": "fixed",
                "top": "20px",
                "right": "20px",
                "z-index": "1000",
                "padding": "10px 15px",
                "border-radius": "8px",
                "box-shadow": "0px 4px 10px rgba(0, 0, 0, 0.2)",
                "font-size": "14px",
                "background": "green",
                "color": "white"
            });

        $('body').append(popup);
        setTimeout(() => popup.fadeOut(() => popup.remove()), 3000);
    }

    // ✅ Clear Errors on Input
    $('input').on('input', function () {
        $('#registerErrorContainer').hide(); // Hide error box when typing
    });

    function clearRegisterErrors() {
        console.log("🔄 Clearing all errors...");
        $('#registerErrorContainer').hide();
    }

    function clearRegisterSuccess() {
        console.log("🔄 Clearing success messages...");
        $('.popup-message').remove();
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    $('#goToRegister').on('click', function (e) {
        e.preventDefault();
        switchToPage('registerPage');
    });

    $('#goToLogin').on('click', function (e) {
        e.preventDefault();
        switchToPage('loginPage');
    });

    function switchToPage(pageId) {
        console.log(`🔄 Switching to page: ${pageId}`);
        $('.page').hide();
        $('#' + pageId).removeClass('d-none').show();
    }
});
