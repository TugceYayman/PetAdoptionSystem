$(document).ready(function () {
    console.log("✅ User Dashboard Loaded");

	
	setTimeout(() => {
	        if ($("#petsContainer, #myPetsContainer, #pendingRequestsContainer").length === 0) {
	            console.error("❌ Dashboard sections not found in the DOM.");
	        } else {
	            console.log("✅ Dashboard sections exist.");
	        }
	    }, 1000);
		
    let token = localStorage.getItem('token');

    if (!token) {
        console.warn("🚨 No token found. Redirecting to login...");
        window.location.href = "index.html";
        return;
    }

	if (localStorage.getItem('token')) {
	    $(".dashboard-container").removeClass("d-none");
	} else {
	    $(".dashboard-container").addClass("d-none"); // ✅ Ensure it stays hidden before login
	}

    
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

// ✅ Function to Show Sections
function showSection(sectionId) {
    console.log(`🔄 Showing ${sectionId}`);

    // Hide all sections first
    $("#petsContainer, #myPetsContainer, #pendingRequestsContainer").addClass("d-none");

    // Ensure the target section is visible
    $("#" + sectionId).removeClass("d-none").css({ "display": "block", "visibility": "visible" });

    console.log(`✅ Section ${sectionId} displayed.`);
}


// ✅ Load Available Pets (Now Using Sidebar Layout)
function loadAvailablePets() {
    console.log("📡 Fetching Available Pets...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ Pets Fetched:", pets);

            if (pets.length === 0) {
                console.warn("⚠️ No pets available! The section might be blank.");
            }

            // ✅ Ensure the section is visible before rendering
            showSection("petsContainer");
            setTimeout(() => renderPets(pets), 100);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pets:", xhr.responseText);
        }
    });
}


// ✅ Render Available Pets (Aligned with Admin Section)
function renderPets(pets) {
    console.log("📌 Rendering Pets:", pets);
    const container = $("#availablePetsList");

    container.empty(); // Clear previous pets

    if (pets.length === 0) {
        console.warn("⚠️ No pets available.");
        container.html('<p class="text-center">No pets available for adoption.</p>');
        return;
    }

    pets.forEach(pet => {
        console.log("✅ Adding pet:", pet.name);
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

    container.addClass("show"); // ✅ Ensure container becomes visible
}



// ✅ Load My Pets (for Adopted Pets)
function loadMyPets() {
    console.log("📡 Fetching My Pets...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/adoptions/my-pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ My Pets Fetched:", pets);
            renderMyPets(pets);
        },
        error: function (xhr) {
            console.error("❌ Error fetching my pets:", xhr.responseText);
        }
    });
}

// ✅ Render My Pets (Includes 'Un-adopt' Option)
function renderMyPets(pets) {
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
                        <div>
                            <h5 class="card-title text-danger">${pet.name}</h5>
                            <p><strong>Type:</strong> ${pet.type}</p>
                            <p><strong>Breed:</strong> ${pet.breed}</p>
                            <p><strong>Age:</strong> ${pet.age} years</p>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-danger unadopt-pet w-100" data-id="${pet.id}">
                                ❌ Un-Adopt
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.append(petCard);
    });

    $(document).off('click', '.unadopt-pet').on('click', '.unadopt-pet', function () {
        unadoptPet($(this).data('id'));
    });
}

// ✅ Un-adopt a Pet (Change Status)
function unadoptPet(petId) {
    let token = localStorage.getItem('token');

    $.ajax({
        url: `/api/adoptions/unadopt/${petId}`,
        type: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            showSuccessPopup("✅ Pet has been un-adopted!");
            loadMyPets();
            loadAvailablePets();
        },
        error: function (xhr) {
            console.error("❌ Error un-adopting pet:", xhr.responseText);
            showErrorPopup("Failed to un-adopt the pet.");
        }
    });
}

// ✅ Fetch Pending Requests for Logged-in User
function loadPendingRequests() {
    console.log("📡 Fetching Pending Requests...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/adoptions/pending-requests', // 🔄 Fixed endpoint
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (requests) {
            console.log("✅ Pending Requests Fetched:", requests);
            renderPendingRequests(requests);
        },
        error: function (xhr) {
            console.error("❌ Error fetching pending requests:", xhr.status, xhr.responseText);
        }
    });
}


// ✅ Render Pending Requests
function renderPendingRequests(requests) {
    const container = $("#pendingRequestsList");
    container.empty();

    if (requests.length === 0) {
        container.html('<p class="text-center">You have no pending adoption requests.</p>');
        return;
    }

    requests.forEach(request => {
        const requestCard = `
            <div class="col-md-4 d-flex align-items-stretch">
                <div class="card shadow-sm p-3 rounded w-100">
                    <div class="card-body">
                        <h5 class="card-title">${request.pet.name}</h5>
                        <p><strong>Status:</strong> ${request.status}</p>
                    </div>
                </div>
            </div>
        `;
        container.append(requestCard);
    });
}


// ✅ Success Popup
function showSuccessPopup(message) {
    alert(message);
}

// ✅ Error Popup
function showErrorPopup(message) {
    alert(message);
}
