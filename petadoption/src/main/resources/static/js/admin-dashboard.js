$(document).ready(function () {
    fetchPets();
    fetchAdoptions();
});

async function fetchPets() {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/pets', {
        headers: {'Authorization': 'Bearer ' + token}
    });

    if (!response.ok) {
        alert('Failed to fetch pets.');
        return;
    }

    const pets = await response.json();
    const container = $('#petsCardContainer');
    container.empty();

    pets.forEach(pet => {
        const petCard = `
            <div class="pet-card">
                <img src="${pet.imageUrl}" alt="${pet.name}">
                <div class="card-body">
                    <h5>${pet.name}</h5>
                    <p><strong>Type:</strong> ${pet.type}</p>
                    <p><strong>Breed:</strong> ${pet.breed}</p>
                    <p><strong>Age:</strong> ${pet.age} years</p>
                    <p><strong>Status:</strong> ${pet.status}</p>
                    <button class="btn-remove" onclick="removePet(${pet.id})">❌ Remove</button>
                </div>
            </div>
        `;
        container.append(petCard);
    });
}

async function fetchAdoptions() {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/admin/adoptions', {
        headers: {'Authorization': 'Bearer ' + token}
    });

    const adoptions = await response.json();
    const adoptionsTable = $('#adoptionsTableContainer');

    adoptionsTable.html(`
        <table>
            <tr>
                <th>Pet</th>
                <th>User</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </table>
    `);

    adoptions.forEach(adoption => {
        adoptionsTable.find('table').append(`
            <tr>
                <td>${adoption.pet.name}</td>
                <td>${adoption.user.email}</td>
                <td>${adoption.status}</td>
                <td>
                    <button class="btn-approve" onclick="updateStatus(${adoption.id}, 'APPROVED')">✅ Approve</button>
                    <button class="btn-reject" onclick="updateStatus(${adoption.id}, 'REJECTED')">❌ Reject</button>
                </td>
            </tr>
        `);
    });
}

async function updateStatus(adoptionId, status) {
    const token = localStorage.getItem('token');
    await fetch(`/api/admin/adoptions/${adoptionId}/${status.toLowerCase()}`, {
        method: 'PUT',
        headers: {'Authorization': 'Bearer ' + token}
    });
    fetchAdoptions();
}

async function removePet(petId) {
    const token = localStorage.getItem('token');
    await fetch(`/api/admin/pets/${petId}`, {
        method: 'DELETE',
        headers: {'Authorization': 'Bearer ' + token}
    });
    fetchPets();
}

function showAddPetModal() {
    $('#addPetModal').fadeIn();
}

function closeAddPetModal() {
    $('#addPetModal').fadeOut();
}

async function addPet() {
    const token = localStorage.getItem('token');
    const name = $('#petName').val().trim();
    const type = $('#petType').val().trim();

    if (!name || !type) {
        alert('Please fill out all fields');
        return;
    }

    await fetch('/api/admin/pets', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, type })
    });

    closeAddPetModal();
    fetchPets();
}
