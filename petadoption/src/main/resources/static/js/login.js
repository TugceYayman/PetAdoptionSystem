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


	function initializeUserDashboard() {
	    console.log("🔄 Initializing User Dashboard...");

	    if (typeof showSection !== "function") {
	        console.error("❌ showSection is not defined. Ensure user-dashboard.js is loaded first.");
	        return;
	    }

	    $(".dashboard-container").removeClass("d-none");

	    // ✅ Load Available Pets as Default View
	    showSection("petsContainer");
	    setTimeout(() => {
	        loadAvailablePets(); // ✅ Ensure Function Exists
	    }, 500);

	    // ✅ Assign Sidebar Button Clicks
	    $('#viewPetsBtn').off().on('click', function () {
	        console.log("🐶 Viewing Available Pets");
	        showSection("petsContainer");
	        loadAvailablePets();
	    });

	    $('#myPetsBtn').off().on('click', function () {
	        console.log("🏠 Viewing My Pets");
	        showSection("myPetsContainer");
	        loadMyPets();
	    });

	    $('#pendingRequestsBtn').off().on('click', function () {
	        console.log("⏳ Viewing Pending Requests");
	        showSection("pendingRequestsContainer");
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
