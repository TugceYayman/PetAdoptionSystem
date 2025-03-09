$(document).ready(function () {
    console.log("🔄 Checking if Admin Dashboard is visible...");

    let token = localStorage.getItem('token');
    let userRole = localStorage.getItem('userRole');

    if (!$('#adminDashboardPage').length) {
        return;
    }

    if (!token || userRole !== 'ADMIN') {
        console.warn("🚨 User is not authenticated or not an admin. Stopping dashboard loading.");
        return;
    }

    console.log("✅ Admin Dashboard Loaded");

    // ✅ Event Listeners for Modals
    $(document).on('click', '#addPetButton', function () {
        $('#addPetModal').removeClass('d-none').show();
    });

    $(document).on('click', '#closeAddPetModal', function () {
        $('#addPetModal').hide();
    });

    $(document).on('click', '#closeUpdatePetModal', function () {
        $('#updatePetModal').hide();
    });

    // ✅ Add Pet with Image Upload
    $(document).on('click', '#confirmAddPet', function () {
        addPet();
    });

    // ✅ Update Pet with Image Upload
    $(document).on('click', '#confirmUpdatePet', function () {
        updatePet();
    });

    fetchPets();
    fetchAdoptions();
});

// ✅ Add Pet with File Upload
function addPet() {
    let token = localStorage.getItem('token');
    let formData = new FormData();

    formData.append("name", $('#petName').val().trim());
    formData.append("type", $('#petType').val().trim());
    formData.append("breed", $('#petBreed').val().trim());
    formData.append("age", $('#petAge').val().trim());
    formData.append("status", $('#petStatus').val());

    let petImageFile = $('#petImage')[0].files[0];
    if (petImageFile) {
        formData.append("image", petImageFile);
    }

    $.ajax({
        url: '/api/pets',
        type: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        contentType: false,
        processData: false,
        data: formData,
        success: function () {
            showPopup("✅ Pet added successfully!", "success");
            $('#addPetModal').hide();
            fetchPets();
        },
        error: function () {
            showPopup("❌ Failed to add pet.", "error");
        }
    });
}

// ✅ Open Update Pet Modal
function openUpdateModal(petId) {
    console.log(`📝 Fetching Pet ${petId} for Update...`);

    $.ajax({
        url: `/api/pets/${petId}`,
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
        success: function (pet) {
            $('#updatePetId').val(pet.id);
            $('#updatePetName').val(pet.name);
            $('#updatePetType').val(pet.type);
            $('#updatePetBreed').val(pet.breed);
            $('#updatePetAge').val(pet.age);
            $('#updatePetStatus').val(pet.status);
            $('#updatePetModal').removeClass('d-none').show();
        },
        error: function () {
            showPopup("❌ Failed to fetch pet details.", "error");
        }
    });
}

// ✅ Update Pet with File Upload
function updatePet() {
    let token = localStorage.getItem('token');
    let petId = $('#updatePetId').val();
    let formData = new FormData();

    formData.append("name", $('#updatePetName').val().trim());
    formData.append("type", $('#updatePetType').val().trim());
    formData.append("breed", $('#updatePetBreed').val().trim());
    formData.append("age", $('#updatePetAge').val().trim());
    formData.append("status", $('#updatePetStatus').val());

    let petImageFile = $('#updatePetImage')[0].files[0];
    if (petImageFile) {
        formData.append("image", petImageFile);
    }

    $.ajax({
        url: `/api/pets/${petId}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token },
        contentType: false,
        processData: false,
        data: formData,
        success: function () {
            showPopup("✅ Pet updated successfully!", "success");
            $('#updatePetModal').hide();
            fetchPets();
        },
        error: function () {
            showPopup("❌ Failed to update pet.", "error");
        }
    });
}

// ✅ Show Error Popup Instead of Alert
function showPopup(message, type) {
    const popup = $('<div class="popup-message"></div>').text(message);
    popup.addClass(type === "error" ? "alert-danger" : "alert-success");

    $('body').append(popup);
    setTimeout(function () {
        popup.fadeOut(function () {
            popup.remove();
        });
    }, 3000);
}


// ✅ Fetch Pets
window.fetchPets = function () {
    console.log("📡 Fetching Pets...");
    let token = localStorage.getItem('token');  
    $.ajax({
        url: '/api/pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ Pets Fetched:", pets);
            if (!pets || pets.length === 0) {
                $('#petsCardContainer').html('<p class="text-center">No pets available.</p>');
            } else {
                renderPetsForAdmin(pets);
            }
        },
        error: function (xhr) {
            console.error("❌ Error fetching pets:", xhr.responseText);
            showErrorPopup("Failed to load pets.");
        }
    });
};

// ✅ Fetch Adoptions
window.fetchAdoptions = function () {
    console.log("📡 Fetching Adoptions...");
    let token = localStorage.getItem('token');  
    $.ajax({
        url: '/api/admin/adoptions',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (adoptions) {
            console.log("✅ Adoptions Fetched:", adoptions);
            if (!adoptions || adoptions.length === 0) {
                $('#adoptionsTableContainer').html('<p class="text-center">No pending adoptions.</p>');
            } else {
                renderAdoptions(adoptions);
            }
        },
        error: function (xhr) {
            console.error("❌ Error fetching adoptions:", xhr.responseText);
            showErrorPopup("Failed to load adoptions.");
        }
    });
};

// ✅ Render Pets with 3-Column Layout
window.renderPetsForAdmin = function (pets) {
    console.log("📌 Rendering Pets...");
    const container = $('#petsCardContainer');
    container.empty();

    let rowDiv = $('<div class="row"></div>'); // Create a row container

    pets.forEach((pet, index) => {
        const petCard = `
            <div class="col-md-4 mb-4">
                <div class="card pet-card shadow-sm p-3 rounded">
                    <img src="${pet.imageUrl || 'default-image.jpg'}" class="card-img-top rounded" alt="${pet.name}">
                    <div class="card-body text-center">
                        <h5 class="card-title text-danger">${pet.name}</h5>
                        <p><strong>Type:</strong> ${pet.type}</p>
                        <p><strong>Breed:</strong> ${pet.breed}</p>
                        <p><strong>Age:</strong> ${pet.age} years</p>
                        <p><strong>Status:</strong> ${pet.status}</p>
                        <button class="btn btn-warning update-pet" data-id="${pet.id}" data-name="${pet.name}" data-type="${pet.type}">✏️ Update</button>
                        <button class="btn btn-danger remove-pet" data-id="${pet.id}">❌ Remove</button>
                    </div>
                </div>
            </div>`;

        rowDiv.append(petCard);

        if ((index + 1) % 3 === 0) {
            container.append(rowDiv);
            rowDiv = $('<div class="row"></div>'); // Start a new row
        }
    });

    if (rowDiv.children().length > 0) {
        container.append(rowDiv);
    }

    $(document).off('click', '.remove-pet').on('click', '.remove-pet', function () {
        const petId = $(this).data('id');
        confirmRemovePet(petId);
    });

    $(document).off('click', '.update-pet').on('click', '.update-pet', function () {
        const petId = $(this).data('id');
        const petName = $(this).data('name');
        const petType = $(this).data('type');

        $('#updatePetId').val(petId);
        $('#updatePetName').val(petName);
        $('#updatePetType').val(petType);

        $('#updatePetModal').removeClass('d-none').show();
    });
};

// ✅ Confirmation Before Removing Pet
function confirmRemovePet(petId) {
    if (confirm("⚠️ Are you sure you want to remove this pet? This action cannot be undone!")) {
        removePet(petId);
    }
}

// ✅ Remove Pet
window.removePet = function (petId) {
    let token = localStorage.getItem('token');
    $.ajax({
        url: `/api/pets/${petId}`,
        type: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            alert("✅ Pet removed successfully!");
            fetchPets();
        },
        error: function (xhr) {
            console.error("❌ Error removing pet:", xhr.responseText);
            showErrorPopup("Failed to remove pet.");
        }
    });
};

// ✅ Add Pet
window.addPet = function () {
    const petName = $('#petName').val().trim();
    const petType = $('#petType').val().trim();
    let token = localStorage.getItem('token');

    if (!petName || !petType) {
        showErrorPopup("⚠️ Please enter all pet details.");
        return;
    }

    $.ajax({
        url: '/api/pets',
        type: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
        contentType: 'application/json',
        data: JSON.stringify({ name: petName, type: petType }),
        success: function () {
            alert("✅ Pet added successfully!");
            fetchPets();
            $('#addPetModal').hide();
        },
        error: function (xhr) {
            console.error("❌ Error adding pet:", xhr.responseText);
            showErrorPopup("Failed to add pet.");
        }
    });
};
