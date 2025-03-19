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
            <div class="user-pet-card">
                <img src="${pet.imageUrl || 'default-image.jpg'}" alt="${pet.name}">
                <div class="card-body">
                    <h5 class="card-title text-danger">${pet.name}</h5>
                    <p><strong>Type:</strong> ${pet.type}</p>
                    <p><strong>Breed:</strong> ${pet.breed || 'Unknown'}</p>
                    <p><strong>Age:</strong> ${pet.age} years</p>
                    <button class="btn btn-danger unadopt-pet" data-id="${pet.id}">
                        ❌ Un-Adopt
                    </button>
                </div>
            </div>
        `;
        container.append(petCard);
    });

    $(".unadopt-pet").off().on("click", function () {
        let petId = $(this).data("id");
        unadoptPet(petId);
    });

    container.addClass("show");
};


window.unadoptPet = function (petId) {
    console.log(`🚨 Unadopting pet ID: ${petId}`);

    if (!confirm("Are you sure you want to un-adopt this pet?")) {
        console.log("❌ Un-adoption cancelled.");
        return;
    }

    let token = localStorage.getItem("token");

    $.ajax({
        url: `/api/adoptions/unadopt/${petId}`, // ✅ Uses the new endpoint
        type: "PUT",
        headers: { "Authorization": "Bearer " + token },
        success: function () {
            console.log(`✅ Pet ID ${petId} un-adopted successfully!`);
            displaySuccessPopup("✅ Pet has been un-adopted successfully!");

            loadMyPets(); // Remove from "My Pets"
            loadAvailablePets(); // Move back to "View Pets"
        },
        error: function (xhr) {
            console.error("❌ Error un-adopting pet:", xhr.responseText);
            displayErrorPopup("❌ Failed to un-adopt the pet.");
        }
    });
};


$(document).ready(function () {
    console.log("✅ User Dashboard Loaded");

    $(".dashboard-container").css("height", "100vh");
    $(".main-content").css("height", "100vh");

    let token = localStorage.getItem('token');

    if (!token) {
        console.warn("🚨 No token found. Redirecting to login...");
        return;
    }

    $(".dashboard-container").removeClass("d-none");

    showSection("petsContainer");
    loadAvailablePets();

    $('#viewPetsBtn').off().on('click', function () {
		if (!token) return;
        showSection("petsContainer");
        loadAvailablePets();
    });

    $('#myPetsBtn').off().on('click', function () {
		if (!token) return;
        showSection("myPetsContainer");
        loadMyPets();
    });

    $('#pendingRequestsBtn').off().on('click', function () {
		if (!token) return;
        showSection("pendingRequestsContainer");
        loadPendingRequests();
    });


});

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

function renderPendingRequests(requests) {
    const container = $("#pendingRequestsList");
    container.empty();

    if (requests.length === 0) {
        container.html('<p class="text-center">You have no pending adoption requests.</p>');
        return;
    }

    requests.forEach(request => {
        const requestCard = `
            <div class="pending-request-card">
                <div>
                    <h5>${request.pet.name}</h5>
                    <p><strong>Type:</strong> ${request.pet.type} | <strong>Breed:</strong> ${request.pet.breed}</p>
                </div>
                <div class="status">${request.status}</div>
            </div>
        `;
        container.append(requestCard);
    });
}



function displayErrorPopup(message) {
    console.log(`🛑 Showing error popup: ${message}`);
    alert(message);
}

function displaySuccessPopup(message) {
    console.log(`✅ Showing success popup: ${message}`);
    alert(message);
}
