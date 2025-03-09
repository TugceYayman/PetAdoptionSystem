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


// ✅ Define selectedPetId globally
let selectedPetId = null;

// ✅ Function to Open Update Modal & Set Selected Pet
function showUpdateModal(id, name, type, breed, age, status) {
    selectedPetId = id; // Set the selected pet ID globally

    // Fill update modal fields with existing pet details
    $('#updatePetName').val(name);
    $('#updatePetType').val(type);
    $('#updatePetBreed').val(breed);
    $('#updatePetAge').val(age);
    $('#updatePetStatus').val(status);

    // Show the modal
    $('#updatePetModal').removeClass('d-none').show();
}

// ✅ Function to Update Pet Details
function updatePet() {
    const petId = selectedPetId;
    if (!petId) {
        showErrorPopup("No pet selected for update.");
        return;
    }

    const formData = new FormData();
    formData.append("name", $('#updatePetName').val().trim());
    formData.append("type", $('#updatePetType').val().trim());
    formData.append("breed", $('#updatePetBreed').val().trim());
    formData.append("age", $('#updatePetAge').val());
    formData.append("status", $('#updatePetStatus').val());

    const fileInput = $('#updatePetImage')[0].files[0];
    if (fileInput) {
        formData.append("image", fileInput);
    }

    $.ajax({
        url: `/api/pets/${petId}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') },
        processData: false,
        contentType: false,
        data: formData,
        success: function (response) {
            showPopup("✅ Pet updated successfully!", "success");
            $('#updatePetModal').hide();
            fetchPets();
        },
        error: function (xhr) {
            console.error("❌ Error updating pet:", xhr.responseText);
            showErrorPopup("Failed to update pet.");
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

function showErrorPopup(message) {
    const popup = $('<div class="popup-message alert-danger"></div>').text(message);
    $('body').append(popup);

    setTimeout(function () {
        popup.fadeOut(function () {
            popup.remove();
        });
    }, 3000);
}



// ✅ Fetch Pets with Detailed Logging
window.fetchPets = function () {
    console.log("📡 Fetching Pets...");
    let token = localStorage.getItem('token');  

    if (!token) {
        showErrorPopup("Authentication token missing. Please log in again.");
        return;
    }

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
            console.error("❌ Error fetching pets:", xhr.status, xhr.responseText);
            showErrorPopup(`Failed to load pets. Status: ${xhr.status} - ${xhr.responseText}`);
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
						<button class="btn btn-warning" 
						    onclick="showUpdateModal('${pet.id}', '${pet.name}', '${pet.type}', '${pet.breed}', ${pet.age}, '${pet.status}')">
						    ✏️ Update
						</button>
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
