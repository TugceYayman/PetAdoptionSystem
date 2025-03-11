$(document).ready(function () {
	console.log("✅ Admin Dashboard Loaded");

	    let token = localStorage.getItem('token');
	    let userRole = localStorage.getItem('userRole');

		// Prevent infinite loop: only redirect if not already on login page
		if (!token || userRole !== 'ADMIN') {
		    console.warn("🚨 Unauthorized Access. Redirecting to Login...");

		    if (window.location.pathname !== "/index.html") {
		        window.location.href = "index.html";
		    }
		}
		
		showAdminSection("managePetsSection");
		fetchPets();

		
		$("#viewAdoptionListBtn").off().on("click", function () {
		        showAdminSection("adoptionListSection");
		        loadAdoptionList();
		    });

	    // ✅ Sidebar Navigation
	    $('#viewPendingRequestsBtn').on('click', function () {
	        $('#pendingRequestsSection').removeClass('d-none');
	        $('#managePetsSection').addClass('d-none');
			$('#adoptionListSection').addClass('d-none');
	        fetchPendingRequests();
	    });

	    $('#managePetsBtn').on('click', function () {
	        $('#managePetsSection').removeClass('d-none');
	        $('#pendingRequestsSection').addClass('d-none');
			$('#adoptionListSection').addClass('d-none');
	        fetchPets();
	    });
		
		
		$('#logoutBtn').on('click', function () {
		    $('#logoutModal').modal('show');
		});

		// ✅ Confirm Logout for Admin
		$('#confirmLogout').on('click', function () {
		    logoutAdmin();
		});


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

function showAdminSection(sectionId) {
    console.log(`🔄 Switching to: ${sectionId}`);

    // Hide all sections first
    $("#pendingRequestsSection, #managePetsSection, #adoptionListSection").addClass("d-none");

    // Ensure the selected section is shown
    $("#" + sectionId).removeClass("d-none").css({
        "display": "block",
        "transition": "opacity 0.3s ease-in-out",
        "opacity": "1"
    });

    console.log(`✅ Section ${sectionId} is now visible.`);
}


function loadAdoptionList() {
       console.log("📡 Fetching Adoption List...");
       let token = localStorage.getItem("token");

       $.ajax({
           url: "/api/admin/adoptions/adoption-list",
           headers: { "Authorization": "Bearer " + token },
           success: function (adoptions) {
               console.log("✅ Adoption List Fetched:", adoptions);
               renderAdoptionList(adoptions);
           },
           error: function (xhr) {
               console.error("❌ Error fetching adoption list:", xhr.responseText);
               displayErrorPopup("❌ Failed to load adoption list.");
           }
       });
   }

   function renderAdoptionList(adoptions) {
       console.log("📌 Rendering Adoption List:", adoptions);
       const container = $("#adoptionListTable tbody");
       container.empty();

       if (adoptions.length === 0) {
           container.html('<tr><td colspan="6" class="text-center">No adoptions found.</td></tr>');
           return;
       }

       adoptions.forEach(adoption => {
           const row = `
               <tr>
                   <td>${adoption.adopterName}</td>
                   <td>${adoption.adopterEmail}</td>
                   <td>${adoption.petName}</td>
                   <td>${adoption.petType}</td>
                   <td>${adoption.petBreed}</td>
                   <td><span class="badge bg-${adoption.adoptionStatus === 'APPROVED' ? 'success' : 'warning'}">
                       ${adoption.adoptionStatus}
                   </span></td>
               </tr>
           `;
           container.append(row);
       });
   }

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

    const rowDiv = $('<div class="row g-4"></div>'); // Bootstrap Grid Layout

    pets.forEach((pet) => {
        let imageUrl = pet.imageUrl ? pet.imageUrl : '/uploads/default-image.jpg'; // ✅ Fix missing images
        const petCard = `
            <div class="col-md-4 d-flex align-items-stretch">
                <div class="card pet-card shadow-sm p-3 rounded w-100">
                    <img src="${imageUrl}" class="card-img-top rounded pet-image" alt="${pet.name}" 
                        onerror="this.onerror=null; this.src='/uploads/default-image.jpg';">
                    <div class="card-body d-flex flex-column justify-content-between">
                        <div>
                            <h5 class="card-title text-danger">${pet.name}</h5>
                            <p><strong>Type:</strong> ${pet.type}</p>
                            <p><strong>Breed:</strong> ${pet.breed}</p>
                            <p><strong>Age:</strong> ${pet.age} years</p>
                            <p><strong>Status:</strong> ${pet.status}</p>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-warning w-100 update-pet" 
                                data-id="${pet.id}" data-name="${pet.name}" 
                                data-type="${pet.type}" data-breed="${pet.breed}" 
                                data-age="${pet.age}" data-status="${pet.status}">
                                ✏️ Update
                            </button>
                            <button class="btn btn-danger w-100 remove-pet mt-2" data-id="${pet.id}">
                                ❌ Remove
                            </button>
                        </div>
                    </div>
                </div>
            </div>`;

        rowDiv.append(petCard);
    });

    container.append(rowDiv); // Append full row to container

    // ✅ Attach event listeners dynamically
    $(document).off('click', '.remove-pet').on('click', '.remove-pet', function () {
        const petId = $(this).data('id');
        confirmRemovePet(petId);
    });

    $(document).off('click', '.update-pet').on('click', '.update-pet', function () {
        const petId = $(this).data('id');
        const petName = $(this).data('name');
        const petType = $(this).data('type');
        const petBreed = $(this).data('breed');
        const petAge = $(this).data('age');
        const petStatus = $(this).data('status');

        showUpdateModal(petId, petName, petType, petBreed, petAge, petStatus);
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

// ✅ Add Pet with Image Upload
// ✅ Add Pet with File Upload
window.addPet = function () {
    let token = localStorage.getItem('token');
    let formData = new FormData();

    // ✅ Collect all pet details
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
        contentType: false, // ✅ Required for form data
        processData: false, // ✅ Prevents jQuery from processing the data
        data: formData,
        success: function () {
            showPopup("✅ Pet added successfully!", "success");

            // ✅ Reset form fields
            $('#petName').val('');
            $('#petType').val('');
            $('#petBreed').val('');
            $('#petAge').val('');
            $('#petStatus').val('AVAILABLE'); // Reset dropdown to default
            $('#petImage').val(''); // Clear file input

            fetchPets(); // ✅ Refresh pet list
            $('#addPetModal').hide(); // ✅ Close modal
        },
        error: function (xhr) {
            console.error("❌ Error adding pet:", xhr.responseText);
            showErrorPopup("Failed to add pet.");
        }
    });
};


// ✅ Fetch Pending Adoption Requests
function fetchPendingRequests() {
    console.log("📡 Fetching Pending Requests...");
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/admin/adoptions/pending',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (requests) {
            let tableBody = $("#pendingRequestsTable tbody");
            tableBody.empty();

            if (!requests.length) {
                tableBody.append("<tr><td colspan='4' class='text-center'>No pending requests.</td></tr>");
                return;
            }

            requests.forEach(request => {
                let row = `
                    <tr>
                        <td>${request.pet.name}</td>
                        <td>${request.user.name}</td>
                        <td>${request.status}</td>
                        <td>
                            <button class="btn btn-success approve-request" data-id="${request.id}">✅ Approve</button>
                            <button class="btn btn-danger reject-request" data-id="${request.id}">❌ Reject</button>
                        </td>
                    </tr>
                `;
                tableBody.append(row);
            });

            // ✅ Handle Approve/Reject Buttons
            $('.approve-request').on('click', function () {
                handleAdoptionRequest($(this).data('id'), 'approve');
            });

            $('.reject-request').on('click', function () {
                handleAdoptionRequest($(this).data('id'), 'reject');
            });
        },
        error: function (xhr) {
            console.error("❌ Error fetching pending requests:", xhr.responseText);
        }
    });
}

// ✅ Handle Approve/Reject Adoption Request
function handleAdoptionRequest(requestId, action) {
    let token = localStorage.getItem('token');
    let endpoint = `/api/admin/adoptions/${action}/${requestId}`;

    $.ajax({
        url: endpoint,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            alert(`✅ Adoption request ${action}d successfully!`);
            fetchPendingRequests();
        },
        error: function (xhr) {
            console.error(`❌ Error ${action}ing request:`, xhr.responseText);
        }
    });
}

function logoutAdmin() {
    console.log("🚪 Logging out admin...");
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');

    setTimeout(() => {
        window.location.href = "index.html"; // Ensures logout is processed properly
    }, 500);
}


