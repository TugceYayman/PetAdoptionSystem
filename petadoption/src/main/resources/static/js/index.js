/*async function fetchPets() {
    const response = await fetch('/api/pets');
    const pets = await response.json();

    const petList = document.getElementById('petList');
    petList.innerHTML = '';

    pets.forEach(pet => {
        const card = `
            <div class="pet-card">
                <img src="${pet.imageUrl}" alt="${pet.name}">
                <h3>${pet.name}</h3>
                <p>${pet.breed}, ${pet.age} years old</p>
                <button onclick="adoptPet(${pet.id})">Adopt Me! 🐶</button>
            </div>
        `;
        petList.innerHTML += card;
    });
}

async function adoptPet(petId) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please log in first!');
        window.location.href = 'login.html';
        return;
    }

    const response = await fetch(`/api/adoptions/${petId}`, {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    });

    if (response.ok) {
        alert('Adoption request sent!');
    } else {
        alert('Adoption request failed.');
    }
/}

fetchPets();*/
