document.getElementById('registerForm').onsubmit = async function(e) {
    e.preventDefault();

    const response = await fetch('/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            name: document.getElementById('name').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        })
    });

    if (response.ok) {
        alert('Registration successful! Please log in.');
        window.location.href = 'login.html';
    } else {
        alert('Registration failed. Try again.');
    }
};
