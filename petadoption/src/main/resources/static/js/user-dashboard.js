// ✅ Declare Function at Global Scope
window.showSection = function (sectionId) {
    console.log(`🔄 Showing ${sectionId}`);

    $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");

    $("#" + sectionId).removeClass("d-none").css({
        "display": "flex",
        "flex-direction": "column",
        "justify-content": "flex-start",
        "align-items": "center",
    });

    console.log(`✅ Section ${sectionId} displayed.`);
};


// ✅ Load My Adopted Pets
window.loadMyPets = function () {
    console.log("📡 Fetching My Pets...");
    let token = localStorage.getItem("token");

    $.ajax({
        url: "/api/adoptions/my-pets",
        headers: { "Authorization": "Bearer " + token },
        success: function (pets) {
            console.log("✅ My Pets Fetched:", pets);
            renderMyPets(pets);
        },
        error: function (xhr) {
            console.error("❌ Error fetching my pets:", xhr.responseText);
            displayErrorPopup("❌ Failed to load adopted pets.");
        }
    });
};

// ✅ Render My Adopted Pets
window.renderMyPets = function (pets) {
    console.log("📌 Rendering My Pets:", pets);
    const container = $("#myPetsList");
    container.empty();

    if (pets.length === 0) {
        container.html('<p class="text-center">You have no adopted pets yet.</p>');
        return;
    }

    pets.forEach(pet => {
        const petCard = `
            <div class="col-md-4 d-flex align-items-stretch">
                <div class="card user-pet-card shadow-sm p-3 rounded w-100">
                    <img src="${pet.imageUrl || 'default-image.jpg'}" class="card-img-top rounded" alt="${pet.name}">
                    <div class="card-body d-flex flex-column justify-content-between">
                        <h5 class="card-title text-danger">${pet.name}</h5>
                        <p><strong>Type:</strong> ${pet.type}</p>
                        <p><strong>Breed:</strong> ${pet.breed}</p>
                        <p><strong>Age:</strong> ${pet.age} years</p>
                        <button class="btn btn-danger unadopt-pet w-100" data-id="${pet.id}">
                            ❌ Un-Adopt
                        </button>
                    </div>
                </div>
            </div>
        `;
        container.append(petCard);
    });

    // ✅ Handle unadoption action
    $(".unadopt-pet").off().on("click", function () {
        let petId = $(this).data("id");
        unadoptPet(petId);
    });

    container.addClass("show");
};


$(document).ready(function () {
    console.log("✅ User Dashboard Loaded");

    $(".dashboard-container").css("height", "100vh");
    $(".main-content").css("height", "100vh");

    let token = localStorage.getItem('token');

    if (!token) {
        console.warn("🚨 No token found. Redirecting to login...");
        window.location.href = "index.html";
        return;
    }

    $(".dashboard-container").removeClass("d-none");

    // ✅ Load Available Pets Initially
    showSection("petsContainer");
    loadAvailablePets();

    // ✅ Sidebar Navigation Click Events
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

    $('#logoutBtn').on('click', function () {
        $('#logoutModal').modal('show');
    });

    $('#confirmLogout').on('click', function () {
        logoutUser();
    });
});

// ✅ Load Available Pets
function loadAvailablePets() {
    console.log("📡 Fetching Available Pets...");
    let token = localStorage.getItem("token");

    $.ajax({
        url: "/api/pets",
        headers: { "Authorization": "Bearer " + token },
        success: function (pets) {
            console.log("✅ Pets Fetched:", pets);

            let availablePets = pets.filter(pet => pet.status === "AVAILABLE");

            if (availablePets.length === 0) {
                console.warn("⚠️ No pets available for adoption.");
            }

            showSection("petsContainer");
            setTimeout(() => renderPets(availablePets), 100);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pets:", xhr.responseText);
        }
    });
}

// ✅ Render Available Pets
function renderPets(pets) {
    console.log("📌 Rendering Pets:", pets);
    const container = $("#availablePetsList");
    container.empty();

    if (pets.length === 0) {
        container.html('<p class="text-center">No pets available for adoption.</p>');
        return;
    }

    pets.forEach(pet => {
        const petCard = `
            <div class="col-md-4">
                <div class="card pet-card">
                    <img src="${pet.imageUrl || 'default-image.jpg'}" class="card-img-top rounded" alt="${pet.name}">
                    <div class="card-body">
                        <h5 class="card-title">${pet.name}</h5>
                        <p><strong>Type:</strong> ${pet.type}</p>
                        <p><strong>Breed:</strong> ${pet.breed}</p>
                        <p><strong>Age:</strong> ${pet.age} years</p>
                        <button class="btn btn-warning request-adoption w-100" data-id="${pet.id}">
                            📩 Request Adoption
                        </button>
                    </div>
                </div>
            </div>
        `;
        container.append(petCard);
    });

    $(".request-adoption").off().on("click", function () {
        let petId = $(this).data("id");
        requestAdoption(petId);
    });

    container.addClass("show");
}

// ✅ Send Adoption Request with Duplicate Check
function requestAdoption(petId) {
    console.log(`📡 Sending adoption request for pet ID: ${petId}`);
    let token = localStorage.getItem("token");

    $.ajax({
        url: `/api/adoptions/request/${petId}`,
        type: "POST",
        headers: { "Authorization": "Bearer " + token },
        success: function (response) {
            console.log("✅ Adoption request successful:", response);
            displaySuccessPopup("✅ Adoption request sent successfully!");

            loadAvailablePets();
            loadPendingRequests();
        },
        error: function (xhr) {
            console.error("❌ Error requesting adoption:", xhr.responseText);

            let errorMessage = "❌ Failed to request adoption. Please try again.";

            if (xhr.status === 400) {
                if (xhr.getResponseHeader("content-type")?.includes("application/json")) {
                    try {
                        let responseJson = JSON.parse(xhr.responseText);
                        if (responseJson.message.includes("already requested adoption")) {
                            errorMessage = "❌ You have already requested adoption for this pet.";
                        }
                    } catch (e) {
                        console.error("⚠️ Failed to parse JSON response:", e);
                    }
                } else {
                    errorMessage = xhr.responseText;
                }
            }

            displayErrorPopup(errorMessage);
        }
    });
}

// ✅ Load Pending Requests
function loadPendingRequests() {
    console.log("📡 Fetching Pending Requests...");
    let token = localStorage.getItem("token");

    $.ajax({
        url: "/api/adoptions/pending-requests",
        headers: { "Authorization": "Bearer " + token },
        success: function (requests) {
            renderPendingRequests(requests);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pending requests:", xhr.responseText);
            displayErrorPopup("❌ Failed to load pending requests.");
        }
    });
}

// ✅ Render Pending Adoption Requests
function renderPendingRequests(requests) {
    const container = $("#pendingRequestsList");
    container.empty();

    if (requests.length === 0) {
        container.html('<p class="text-center">You have no pending adoption requests.</p>');
        return;
    }

    let requestCards = requests.map(request => `
        <div class="col-md-4">
            <div class="card shadow-sm p-3 rounded text-center">
                <h5 class="card-title">${request.pet.name}</h5>
                <p><strong>Status:</strong> <span class="badge bg-warning">${request.status}</span></p>
            </div>
        </div>
    `).join("");

    container.html(requestCards);
}

// ✅ Popups
function displayErrorPopup(message) {
    console.log(`🛑 Showing error popup: ${message}`);
    alert(message);
}

function displaySuccessPopup(message) {
    console.log(`✅ Showing success popup: ${message}`);
    alert(message);
}
