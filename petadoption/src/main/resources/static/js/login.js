$(document).ready(function () {
    console.log("🚀 Login Page Loaded!");

    switchToPage('loginPage');

    $('#goToRegister').on('click', function (e) {
        e.preventDefault();
        switchToPage('registerPage');
    });

    $('#goToLogin').on('click', function (e) {
        e.preventDefault();
        switchToPage('loginPage');
    });

    $('#loginForm').on('submit', function (e) {
        e.preventDefault();

        clearLoginErrors();
        clearLoginSuccess();

        const email = $('#loginEmail').val().trim();
        const password = $('#loginPassword').val().trim();

        let errorMessages = [];

        console.log("🔍 Validating login fields...");

        if (!email && !password) {
            console.warn("⚠️ Both fields are empty!");
            showLoginError(["⚠️ All fields are required."]);
            return;
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
				
				$('#loginEmail').val('').attr('placeholder', 'Enter your email');
				$('#loginPassword').val('').attr('placeholder', 'Enter your password');

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

	function logoutUser() {
	    console.log("🚪 Logging out user...");

	    $("#logoutModal").modal("hide");

	    localStorage.removeItem("token");
	    localStorage.removeItem("userRole");

	    $(".dashboard-container").addClass("d-none");
	    $("#pendingRequestsSection, #adoptionListSection, #animalDistributionSection, #managePetsSection, #userDashboardPage, #adminDashboardPage").addClass("d-none");

	    setTimeout(() => {
	        switchToPage("loginPage");

	        showSuccessPopup("✅ You have been logged out successfully!");
	        console.log("🔄 Redirected to login page with success message.");
	    }, 300);
	}



    function switchToPage(pageId) {
        console.log(`🔄 Switching to page: ${pageId}`);
        $('.page').hide();
        $('#' + pageId).removeClass('d-none').show();
		
		if (pageId === 'loginPage' || pageId === 'registerPage') {
		        $('body').addClass('auth-background'); // Apply background
				// ✅ Add Rabbit if not already there
				  if ($('.floating-rabbit').length === 0) {
					$('body').append('<img src="/img/rabbit(1).png" class="floating-rabbit" alt="Jumping Rabbit">');
					startRabbitAnimation();

				}
		    } else {
		        $('body').removeClass('auth-background'); // Remove background for other pages
				$('.floating-rabbit').remove();

		    }

        if (pageId === 'userDashboardPage') {
            $(".dashboard-container").removeClass("d-none");
        } else {
            $(".dashboard-container").addClass("d-none");
        }
    }
	
	function startRabbitAnimation() {
	    let rabbit = $('.floating-rabbit');

	    if (!rabbit.length) return;

	    function moveRabbit() {
	        rabbit.css({ left: '-100px' }); // Start from left off-screen
	        rabbit.animate({ left: '100%' }, 5000, 'linear', function () {
	            moveRabbit(); // Restart after exiting right
	        });
	    }

	    moveRabbit(); // Start the movement loop
	}



    function initializeUserDashboard() {
        console.log("🔄 Initializing User Dashboard...");

        let token = localStorage.getItem("token");
        if (!token) {
            console.warn("🚨 No authentication token found. Aborting dashboard initialization.");
            return;
        }

        $(".dashboard-container").removeClass("d-none");

        showSection("petsContainer");
        setTimeout(() => {
            loadAvailablePets();
        }, 500);

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
		
		$(document).on("click", "#logoutBtn", function () {
		    console.log("🚪 Logout button clicked!");
		    $("#logoutModal").modal("show"); // ✅ Show confirmation modal
		});

		$(document).on("click", "#confirmLogout", function () {
		    console.log("✅ User confirmed logout.");

		    $("#logoutModal").modal("hide");

		    logoutUser();
		});

    }

    function initializeAdminDashboard() {
        console.log("🚀 Initializing Admin Dashboard...");

        if (!$("#adminDashboardPage").is(":visible")) {
            console.warn("🚨 Admin Dashboard is hidden. Skipping initialization.");
            return;
        }
		
		$("#pendingRequestsSection, #adoptionListSection, #animalDistributionSection, #managePetsSection").addClass("d-none");

        $('#managePetsSection').removeClass('d-none');
        fetchPets();
       // fetchAdoptions();

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

		$(document).on("click", "#logoutBtn", function () {
		    console.log("🚪 Logout button clicked!");
		    $("#logoutModal").modal("show"); // ✅ Show confirmation modal
		});

		$(document).on("click", "#confirmLogout", function () {
		    console.log("✅ User confirmed logout.");

		    $("#logoutModal").modal("hide");

		    logoutUser();
		});



        console.log("✅ Admin Dashboard Initialized.");
    }

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
