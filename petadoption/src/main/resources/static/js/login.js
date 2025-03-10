$(document).ready(function () {
    console.log("🚀 Login Page Loaded");

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

                setTimeout(() => {
                    if (response.role === 'ADMIN') {
                        switchToPage('adminDashboardPage');
                        console.log("✅ Admin logged in, fetching dashboard data...");
                    } else if (response.role === 'USER') {
                        switchToPage('userDashboardPage');
                        console.log("✅ User logged in, initializing dashboard...");

                        // ✅ FIX: Ensure user dashboard loads properly
                        initializeUserDashboard();
                    } else {
                        showErrorPopup('❌ Unknown role. Please contact support.');
                    }
                }, 1000);
            },
            error: function (xhr) {
                const errorMessage = xhr.responseText || '❌ Login failed. Please check your credentials.';
                showErrorPopup(`Login failed: ${errorMessage}`);
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
	    $('.page').hide(); // ✅ Hide all pages
	    $('#' + pageId).removeClass('d-none').show();

	    if (pageId === 'userDashboardPage') {
	        $(".dashboard-container").removeClass("d-none"); // ✅ Show sidebar only for logged-in users
	    } else {
	        $(".dashboard-container").addClass("d-none"); // ✅ Hide sidebar for non-logged-in users
	    }
	}


    // ✅ Initialize User Dashboard After Login
    function initializeUserDashboard() {
        console.log("🔄 Initializing User Dashboard...");

        // ✅ Ensure Available Pets is the default view
        $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");
        $("#petsContainer").removeClass("d-none");

        // ✅ Load Available Pets AFTER Making Sure Section is Visible
        setTimeout(() => {
            loadAvailablePets();
        }, 500);

        // ✅ Ensure Button Click Handlers Are Assigned
        $('#viewPetsBtn').off().on('click', function () {
            console.log("🐶 Viewing Available Pets");
            $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");
            $("#petsContainer").removeClass("d-none");
            loadAvailablePets();
        });

        $('#myPetsBtn').off().on('click', function () {
            console.log("🏠 Viewing My Pets");
            $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");
            $("#myPetsContainer").removeClass("d-none");
            loadMyPets();
        });

        $('#pendingRequestsBtn').off().on('click', function () {
            console.log("⏳ Viewing Pending Requests");
            $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");
            $("#pendingRequestsContainer").removeClass("d-none");
            loadPendingRequests();
        });
    }

    function showErrorPopup(message) {
        alert(message);
    }

    function showSuccessPopup(message) {
        alert(message);
    }
});
