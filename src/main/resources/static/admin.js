const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

//Allow ADMIN + SUPER_ADMIN
if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    alert("Access denied.");
    window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", loadMessages);

let allMessages = [];

function loadMessages() {
    fetch("http://localhost:8080/api/admin/messages", {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("Failed to load messages");
            return res.json();
        })
        .then(data => {
            allMessages = data;
            displayMessages(data);
        })
        .catch(error => console.error(error));
}

function displayMessages(messages) {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    messages.forEach(msg => {

        const actions = buildActions(msg);

        const row = `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.name}</td>
                <td>${msg.email}</td>
                <td>${msg.type}</td>
                <td>${msg.message}</td>
                <td>${msg.location || "-"}</td>
                <td>${formatDate(msg.preferredDateTime)}</td>
                <td><span class="status ${msg.status}">${msg.status}</span></td>
                <td>${actions}</td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

//Smart actions based on status
function buildActions(msg) {
    let buttons = "";

    if (msg.status === "NEW") {
        buttons += `<button class="action-btn read-btn" onclick="updateStatus(${msg.id}, 'READ')">Mark Read</button>`;
    }

    if (msg.status === "READ") {
        buttons += `<button class="action-btn respond-btn" onclick="updateStatus(${msg.id}, 'RESPONDED')">Mark Responded</button>`;
    }

    buttons += `<button class="action-btn delete-btn" onclick="deleteMessage(${msg.id})">Delete</button>`;

    return buttons;
}

// Update status (READ / RESPONDED)
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/messages/{id}/status`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ status })
    })
        .then(() => {
            alert(`Marked as ${status}`);
            loadMessages();
        })
        .catch(error => console.error(error));
}

function deleteMessage(id) {
    if (!confirm("Are you sure you want to delete this message?")) return;

    fetch(`http://localhost:8080/api/admin/messages/{id}`, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(() => {
            alert("Deleted successfully");
            loadMessages();
        })
        .catch(error => console.error(error));
}

// Format date nicely
function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString();
}

function filterMessages() {
    const search = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const type = document.getElementById("typeFilter").value;

    let filtered = allMessages.filter(msg => {
        const matchesSearch =
            msg.name.toLowerCase().includes(search) ||
            msg.email.toLowerCase().includes(search);

        const matchesStatus =
            status === "" || msg.status === status;

        const matchesType =
            type === "" || msg.type === type;

        return matchesSearch && matchesStatus && matchesType;
    });

    displayMessages(filtered);
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}