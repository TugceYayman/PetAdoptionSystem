$(document).ready(function () {

    $('#registerForm').on('submit', async function (e) {
        e.preventDefault();

        clearRegisterError();

        const name = $('#registerName').val().trim();
        const email = $('#registerEmail').val().trim();
        const password = $('#registerPassword').val().trim();

        if (!name || !email || !password) {
            showRegisterError('⚠️ All fields are required.');
            return;
        }

        if (!validateEmail(email)) {
            showRegisterError('⚠️ Please enter a valid email address.');
            return;
        }

        try {
            const response = await fetch('/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password })
            });

            const data = await response.json();

            if (response.ok) {
                showSuccessPopup(`🎉 ${data.message}`);
                showPage('loginPage');
            } else {
                showRegisterError(`❌ ${data.message}`);
            }

        } catch (error) {
            showRegisterError('❌ Unable to connect to server. Please try again later.');
        }
    });

    function showRegisterError(message) {
        let errorBox = $('#registerErrorMessage');
        if (errorBox.length === 0) {
            errorBox = $('<div id="registerErrorMessage" class="alert alert-danger"></div>');
            errorBox.insertBefore('#registerForm');
        }
        errorBox.removeClass('d-none').html(`<i class="bi bi-exclamation-triangle-fill"></i> ${message}`);
    }

    function clearRegisterError() {
        $('#registerErrorMessage').addClass('d-none').html('');
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function showSuccessPopup(message) {
        const popup = $('<div class="popup-message alert alert-success"></div>').text(message);
        $('body').append(popup);
        setTimeout(() => popup.fadeOut(() => popup.remove()), 3000);
    }


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
	
	// Function to switch pages (hide all, show selected one)
	function switchToPage(pageId) {
	    $('.page').hide();
	    $('#' + pageId).removeClass('d-none').show();
	}


});
