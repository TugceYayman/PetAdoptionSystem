$(document).ready(function () {
    console.log("🚀 Login Page Loaded!");

    // ✅ Show login page initially
    switchToPage('loginPage');

    // ✅ Handle switching between login and register pages
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

        clearLoginErrors();
        clearLoginSuccess();

        const email = $('#loginEmail').val().trim();
        const password = $('#loginPassword').val().trim();

        let errorMessages = [];

        console.log("🔍 Validating login fields...");

        // 🚨 Show "All fields are required" if both fields are empty
        if (!email && !password) {
            console.warn("⚠️ Both fields are empty!");
            showLoginError(["⚠️ All fields are required."]);
            return;
        }

        // ✅ Validate Email and Password Separately
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
            console.warn("❌ Login form validation failed.");
            showLoginError(errorMessages);
            return;
        }

        console.log("✅ Client-side validation passed. Sending request...");

        $.ajax({
            url: '/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email: email, password: password }),
            success: function (response) {
                console.log("✅ Login successful! Storing token and role...");

                localStorage.setItem('token', response.token);
                localStorage.setItem('userRole', response.role);

                showSuccessPopup("✅ Login successful!");

                setTimeout(() => {
                    if (response.role === 'ADMIN') {
                        console.log("✅ Redirecting to Admin Dashboard...");
                        switchToPage('adminDashboardPage');
                        initializeAdminDashboard();
                    } else if (response.role === 'USER') {
                        console.log("✅ Redirecting to User Dashboard...");
                        switchToPage('userDashboardPage');
                        initializeUserDashboard();
                    } else {
                        showLoginError(['❌ Unknown role. Please contact support.']);
                    }
                }, 1000);
            },
            error: function (xhr) {
                const errorMessage = xhr.responseText || '❌ Login failed. Please check your credentials.';
                console.error("❌ Backend login error:", errorMessage);
                showLoginError([`${errorMessage}`]);
            }
        });
    });

    // ✅ Logout function
    function logoutUser() {
        console.log("🚪 Logging out...");
        localStorage.removeItem('token');
        localStorage.removeItem('userRole');

        $(".dashboard-container").addClass("d-none"); // ✅ Hide sidebar after logout
        switchToPage('loginPage');
    }

    $(document).on('click', '#logoutBtn', function () {
        logoutUser();
    });

    // ✅ Function to switch between pages
    function switchToPage(pageId) {
        console.log(`🔄 Switching to page: ${pageId}`);
        $('.page').hide();
        $('#' + pageId).removeClass('d-none').show();
		
		if (pageId === 'loginPage' || pageId === 'registerPage') {
		        $('body').addClass('auth-background'); // Apply background
		    } else {
		        $('body').removeClass('auth-background'); // Remove background for other pages
		    }

        if (pageId === 'userDashboardPage') {
            $(".dashboard-container").removeClass("d-none");
        } else {
            $(".dashboard-container").addClass("d-none");
        }
    }

    // ✅ Initialize User Dashboard
    function initializeUserDashboard() {
        console.log("🔄 Initializing User Dashboard...");

        let token = localStorage.getItem("token");
        if (!token) {
            console.warn("🚨 No authentication token found. Aborting dashboard initialization.");
            return;
        }

        $(".dashboard-container").removeClass("d-none");

        // ✅ Load Available Pets as Default View
        showSection("petsContainer");
        setTimeout(() => {
            loadAvailablePets();
        }, 500);

        // ✅ Assign Sidebar Button Clicks
        $('#viewPetsBtn').off().on('click', function () {
            showSection("petsContainer");
            loadAvailablePets();
        });

        $('#myPetsBtn').off().on('click', function () {
            showSection("myPetsContainer");
            loadMyPets();
        });

        $('#pendingRequestsBtn').off().on('click', function () {
            showSection("pendingRequestsContainer");
            loadPendingRequests();
        });
    }

    // ✅ Initialize Admin Dashboard
    function initializeAdminDashboard() {
        console.log("🚀 Initializing Admin Dashboard...");

        if (!$("#adminDashboardPage").is(":visible")) {
            console.warn("🚨 Admin Dashboard is hidden. Skipping initialization.");
            return;
        }

        // ✅ Load Pets and Adoptions when Admin logs in
        $('#managePetsSection').removeClass('d-none');
        fetchPets();
        fetchAdoptions();

        // ✅ Sidebar Navigation Click Events
        $('#viewPendingRequestsBtn').off().on('click', function () {
            console.log("🔄 Fetching Pending Requests...");
            showAdminSection("pendingRequestsSection");
            fetchPendingRequests();
        });

        $('#managePetsBtn').off().on('click', function () {
            console.log("🐶 Managing Pets...");
            showAdminSection("managePetsSection");
            fetchPets();
        });

        $('#viewAdoptionListBtn').off().on("click", function () {
            console.log("📋 Viewing Adoption List...");
            showAdminSection("adoptionListSection");
            loadAdoptionList();
        });

        $('#logoutBtn').off().on('click', function () {
            $('#logoutModal').modal('show');
        });

        $('#confirmLogout').off().on('click', function () {
            logoutUser();
        });

        console.log("✅ Admin Dashboard Initialized.");
    }

    // ✅ Show Errors in a Single Box Above the Form
    function showLoginError(messages) {
        console.log("❌ Displaying login errors:", messages);

        let errorContainer = $('#loginErrorContainer');
        if (errorContainer.length === 0) {
            errorContainer = $('<div id="loginErrorContainer" class="alert alert-danger text-danger mt-2"></div>');
            $('#loginForm').before(errorContainer);
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

    function clearLoginErrors() {
        console.log("🔄 Clearing all login errors...");
        $('#loginErrorContainer').hide();
    }

    function clearLoginSuccess() {
        console.log("🔄 Clearing success messages...");
        $('.popup-message').remove();
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }
});
