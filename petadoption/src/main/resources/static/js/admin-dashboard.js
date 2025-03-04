async function fetchAdoptions() {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/admin/adoptions', {
        headers: {'Authorization': 'Bearer ' + token}
    });
    const adoptions = await response.json();

    const table = document.getElementById('adoptionsTable');
    table.innerHTML = '<table><tr><th>Pet</th><th>User</th><th>Status</th><th>Actions</th></tr>';

    adoptions.forEach(adoption => {
        table.innerHTML += `
            <tr>
                <td>${adoption.pet.name}</td>
                <td>${adoption.user.email}</td>
                <td>${adoption.status}</td>
                <td>
                    <button onclick="updateStatus(${adoption.id}, 'APPROVED')">✅ Approve</button>
                    <button onclick="updateStatus(${adoption.id}, 'REJECTED')">❌ Reject</button>
                </td>
            </tr>
        `;
    });

    table.innerHTML += '</table>';
}

async function updateStatus(adoptionId, status) {
    const token = localStorage.getItem('token');
    await fetch(`/api/admin/adoptions/${adoptionId}/${status.toLowerCase()}`, {
        method: 'PUT',
        headers: {'Authorization': 'Bearer ' + token}
    });
    fetchAdoptions();
}

fetchAdoptions();
