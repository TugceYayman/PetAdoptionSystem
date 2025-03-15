$(document).ready(function () {
    $(document).on('click', '#addPetButton', function () {
        console.log("✅ Add Pet Button Clicked!");
        $('#addPetModal').removeClass('d-none').show();
    });
	
	$(document).on('click', '#closeAddPetModal', function () {
	       $('#addPetModal').hide();
	   });
	   
	   // ✅ Add Pet with Image Upload
	   $(document).on('click', '#confirmAddPet', function () {
	       addPet();
	   });

});




$(document).ready(function () {
	
	
    // ✅ Event Listener for Animal Distribution Button
    $('#viewAnimalDistributionBtn').on('click', function () {
        showAdminSection("animalDistributionSection");
        fetchAnimalDistribution();
    });
});

// ✅ Function to Fetch Animal Distribution Data
function fetchAnimalDistribution() {
    let token = localStorage.getItem('token');

    $.ajax({
        url: '/api/pets',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function (pets) {
            console.log("✅ Animal Distribution Data Fetched:", pets);
            renderAnimalDistributionChart(pets);
        },
        error: function (xhr) {
            console.error("❌ Error fetching animal distribution:", xhr.responseText);
        }
    });
}

function renderAnimalDistributionChart(pets) {
    let animalTypeCounts = {};

    pets.forEach(pet => {
        animalTypeCounts[pet.type] = (animalTypeCounts[pet.type] || 0) + 1;
    });

    let labels = Object.keys(animalTypeCounts);
    let dataValues = Object.values(animalTypeCounts);

    let canvas = document.getElementById('animalChart');

    // ✅ Fix Chart Size
    canvas.style.width = "100%";
    canvas.style.height = "auto";

    if (window.animalChartInstance) {
        window.animalChartInstance.destroy();
    }

    // 🎨 Define Custom Colors for Animals
    let colorPalette = [
        'rgba(255, 99, 132, 0.6)', // Red
        'rgba(54, 162, 235, 0.6)', // Blue
        'rgba(255, 206, 86, 0.6)', // Yellow
        'rgba(75, 192, 192, 0.6)', // Green
        'rgba(153, 102, 255, 0.6)', // Purple
        'rgba(255, 159, 64, 0.6)'  // Orange
    ];

    let borderColorPalette = [
        'rgba(255, 99, 132, 1)',
        'rgba(54, 162, 235, 1)',
        'rgba(255, 206, 86, 1)',
        'rgba(75, 192, 192, 1)',
        'rgba(153, 102, 255, 1)',
        'rgba(255, 159, 64, 1)'
    ];

    // ✅ Assign a unique color to each animal type
    let backgroundColors = labels.map((_, i) => colorPalette[i % colorPalette.length]);
    let borderColors = labels.map((_, i) => borderColorPalette[i % borderColorPalette.length]);

    let ctx = canvas.getContext('2d');
    window.animalChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Number of Animals',
                data: dataValues,
                backgroundColor: backgroundColors, // ✅ Assign multiple colors
                borderColor: borderColors, // ✅ Assign border colors
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    stepSize: 1,  // ✅ Force whole number increments
                    ticks: {
                        callback: function(value) {
                            return Number.isInteger(value) ? value : ''; // ✅ Hide decimals
                        }
                    }
                }
            }
        }
    });
}



$(document).ready(function () {
	console.log("✅ Admin Dashboard Loaded");
	
	if (!$("#adminDashboardPage").is(":visible")) {
	    console.warn("🚨 Admin Dashboard is hidden. Skipping admin scripts.");
	    return;  // ✅ Prevents execution if it's hidden
	}


	    let token = localStorage.getItem('token');
	    let userRole = localStorage.getItem('userRole');

		// ✅ Prevent infinite loop by checking if already on index.html (Login Page)
		 if (!token || userRole !== 'ADMIN') {
		     console.warn("🚨 Unauthorized Access. Redirecting to Login...");

		     // 🚀 FIX: Only redirect if not already on the login page
		     if (window.location.pathname !== "/index.html") { 
		         window.location.href = "index.html";
		     }
		     return;  // 🚀 Prevent further execution
		 }


		// ✅ Only fetch data if a valid token exists
		if (token) {
		    $('#managePetsSection').removeClass('d-none');
		    fetchPets();
		  //  fetchAdoptions();
		}
		

		
		$('#viewPendingRequestsBtn').on('click', function () {
		        if (!token) return; // ✅ Don't fetch if no token
		        $('#pendingRequestsSection').removeClass('d-none');
		        $('#managePetsSection').addClass('d-none');
		        $('#adoptionListSection').addClass('d-none');
				$('#viewAnimalDistributionBtn').addClass('d-none');

		        fetchPendingRequests();
		    });

		    $('#managePetsBtn').on('click', function () {
		        if (!token) return; // ✅ Don't fetch if no token
		        $('#managePetsSection').removeClass('d-none');
		        $('#pendingRequestsSection').addClass('d-none');
		        $('#adoptionListSection').addClass('d-none');
				$('#viewAnimalDistributionBtn').addClass('d-none');
		        fetchPets();
		    });

		    $('#viewAdoptionListBtn').off().on("click", function () {
		        if (!token) return; // ✅ Don't fetch if no token
				$('#viewAnimalDistributionBtn').addClass('d-none');

		        showAdminSection("adoptionListSection");
		        loadAdoptionList();
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


    // ✅ Update Pet with Image Upload
    $(document).on('click', '#confirmUpdatePet', function () {
        updatePet();
    });

    fetchPets();
  //  fetchAdoptions();
});

function showAdminSection(sectionId) {
    console.log(`🔄 Switching to: ${sectionId}`);

    // Hide all sections including the chart section
    $("#pendingRequestsSection, #managePetsSection, #adoptionListSection, #animalDistributionSection").addClass("d-none");

    // Ensure the selected section is shown
    $("#" + sectionId).removeClass("d-none").css({
        "transition": "opacity 0.3s ease-in-out",
        "opacity": "1"
    });

    // ✅ Destroy the chart instance when switching sections
    if (sectionId !== "animalDistributionSection" && window.animalChartInstance) {
        window.animalChartInstance.destroy();
        window.animalChartInstance = null; // Clear the chart instance
        console.log("🗑️ Chart destroyed");
    }
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
		console.warn("🚨 Authentication token missing. Not fetching adoptions.");
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
/*window.fetchAdoptions = function () {
    console.log("📡 Fetching Adoptions...");
    let token = localStorage.getItem('token'); 
	if (!token) {
	       console.warn("🚨 Authentication token missing. Not fetching adoptions.");
	       return;
	   } 
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
};*/

// ✅ Render Pets with 3-Column Layout
window.renderPetsForAdmin = function (pets) {
    console.log("📌 Rendering Pets...");
    const container = $('#petsCardContainer');
    container.empty();

    const rowDiv = $('<div class="row g-4"></div>'); // Bootstrap Grid Layout

    pets.forEach((pet) => {
        let imageUrl = pet.imageUrl ? pet.imageUrl : '/uploads/not-found.png'; // ✅ Fix missing images
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

window.removePet = function (petId) {
    let token = localStorage.getItem('token');

    if (!token) {
        console.error("🚨 No authentication token found. Redirecting to login...");
        alert("Session expired. Please log in again.");
        window.location.href = "/index.html";
        return;
    }

    console.log("📝 Sending DELETE request to:", `/api/pets/${petId}`);
    console.log("🔑 Authorization Header:", `Bearer ${token}`);

    $.ajax({
        url: `/api/pets/${petId}`,
        type: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
        success: function () {
            alert("✅ Pet removed successfully!");
            fetchPets();
        },
        error: function (xhr) {
            console.error(`❌ Error removing pet: ${xhr.status} - ${xhr.responseText}`);
            showErrorPopup(`Failed to remove pet. Status: ${xhr.status}`);
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




