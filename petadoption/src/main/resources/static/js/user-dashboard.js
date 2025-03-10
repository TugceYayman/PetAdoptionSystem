$(document).ready(function () {
    console.log("✅ User Dashboard Loaded");

    // ✅ Load pets when dashboard loads
    loadAvailablePets();

    // ✅ Handle sidebar navigation
    $('#viewPetsBtn').on('click', function () {
        loadAvailablePets();
    });

    $('#myPetsBtn').on('click', function () {
        loadMyPets();
    });

    $('#pendingRequestsBtn').on('click', function () {
        loadPendingRequests();
    });
});

// ✅ Fetch Available Pets
function loadAvailablePets() {
    console.log("📡 Fetching Available Pets...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ Pets Fetched:", pets);
            renderPets(pets);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pets:", xhr.responseText);
            showErrorPopup("Failed to load pets.");
        }
    });
}

// ✅ Render Available Pets
function renderPets(pets) {
    const container = $('#petsContainer');
    container.empty();

    pets.forEach(pet => {
        if (pet.status !== "ADOPTED") {  // ✅ Hide adopted pets
            const petCard = `
                <div class="card pet-card" style="width: 18rem;">
                    <img src="${pet.imageUrl || 'default-image.jpg'}" class="card-img-top" alt="${pet.name}">
                    <div class="card-body">
                        <h5 class="card-title text-danger">${pet.name}</h5>
                        <p><strong>Type:</strong> ${pet.type}</p>
                        <p><strong>Breed:</strong> ${pet.breed}</p>
                        <p><strong>Age:</strong> ${pet.age} years</p>
                        <button class="btn btn-warning request-adoption" data-id="${pet.id}">📩 Request Adoption</button>
                    </div>
                </div>
            `;
            container.append(petCard);
        }
    });

    // ✅ Handle Adoption Requests
    $('.request-adoption').on('click', function () {
        const petId = $(this).data('id');
        requestAdoption(petId);
    });
}

// ✅ Request Adoption
function requestAdoption(petId) {
    let token = localStorage.getItem('token');

    $.ajax({
        url: `/api/adoptions/request/${petId}`,
        type: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            showSuccessPopup("✅ Adoption request sent!");
            loadAvailablePets();
        },
        error: function (xhr) {
            console.error("❌ Error requesting adoption:", xhr.responseText);
            showErrorPopup("Failed to send request.");
        }
    });
}

// ✅ Fetch My Adopted Pets
function loadMyPets() {
    console.log("📡 Fetching My Pets...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/adoptions/my-pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ My Pets Fetched:", pets);
            renderPets(pets);
        },
        error: function (xhr) {
            console.error("❌ Error fetching my pets:", xhr.responseText);
            showErrorPopup("Failed to load adopted pets.");
        }
    });
}

// ✅ Fetch Pending Requests
function loadPendingRequests() {
    console.log("📡 Fetching Pending Requests...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/adoptions/pending-requests',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (requests) {
            console.log("✅ Pending Requests Fetched:", requests);
            renderPendingRequests(requests);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pending requests:", xhr.responseText);
            showErrorPopup("Failed to load pending requests.");
        }
    });
}

// ✅ Render Pending Requests
function renderPendingRequests(requests) {
    const container = $('#petsContainer');
    container.empty();

    if (requests.length === 0) {
        container.html('<p class="text-center">No pending requests.</p>');
        return;
    }

    requests.forEach(request => {
        const requestCard = `
            <div class="card pet-card" style="width: 18rem;">
                <div class="card-body">
                    <h5 class="card-title">${request.pet.name}</h5>
                    <p><strong>Status:</strong> ${request.status}</p>
                </div>
            </div>
        `;
        container.append(requestCard);
    });
}
