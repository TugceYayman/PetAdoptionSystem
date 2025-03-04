document.getElementById('loginForm').onsubmit = async function (e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!email || !password) {
        alert('Please enter both email and password.');
        return;
    }

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, password})
        });

        if (response.ok) {
            const result = await response.json();

            // Store token and role
            localStorage.setItem('token', result.token);
            localStorage.setItem('userRole', result.role);

            alert('Login successful!');

            // Redirect based on role
            if (result.role === 'ADMIN') {
                window.location.href = 'admin-dashboard.html';
            } else {
                window.location.href = 'my-adoptions.html';
            }
        } else {
            const errorText = await response.text();
            alert(`Login failed: ${errorText}`);
        }
    } catch (error) {
        alert('An error occurred while trying to log in. Please try again later.');
        console.error('Login error:', error);
    }
};
