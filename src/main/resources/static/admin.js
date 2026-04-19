const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

// Allow ADMIN + SUPER_ADMIN
if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    alert("Access denied.");
    window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", loadMessages);

let allMessages = [];

// ================= LOAD =================
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
        .catch(error => {
            console.error(error);
            alert("Failed to load messages");
        });
}

// ================= DISPLAY =================
function displayMessages(messages) {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    if (messages.length === 0) {
        table.innerHTML = `<tr><td colspan="9">No messages found</td></tr>`;
        return;
    }

    messages.forEach(msg => {
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
                <td>${buildActions(msg)}</td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

// ================= ACTIONS =================
function buildActions(msg) {
    let buttons = "";

    if (msg.status === "NEW") {
        buttons += `<button class="action-btn read-btn" onclick="updateStatus(${msg.id}, 'READ')">Read</button>`;
    }

    if (msg.status === "READ") {
        buttons += `<button class="action-btn progress-btn" onclick="updateStatus(${msg.id}, 'IN_PROGRESS')">Start</button>`;
    }

    if (msg.status === "IN_PROGRESS") {
        buttons += `<button class="action-btn respond-btn" onclick="updateStatus(${msg.id}, 'RESPONDED')">Responded</button>`;
    }

    if (msg.status === "RESPONDED") {
        buttons += `<button class="action-btn close-btn" onclick="updateStatus(${msg.id}, 'CLOSED')">Close</button>`;
    }

    buttons += `<button class="action-btn delete-btn" onclick="deleteMessage(${msg.id})">Delete</button>`;

    return buttons;
}

// ================= UPDATE STATUS =================
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/messages/${id}/status`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ status })
    })
        .then(res => {
            if (!res.ok) throw new Error("Failed to update status");
            return res.json();
        })
        .then(() => {
            loadMessages();
        })
        .catch(error => {
            console.error(error);
            alert("Error updating status");
        });
}

// ================= DELETE =================
function deleteMessage(id) {
    if (!confirm("Delete this message?")) return;

    fetch(`http://localhost:8080/api/admin/messages/${id}`, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("Delete failed");
            loadMessages();
        })
        .catch(error => {
            console.error(error);
            alert("Error deleting message");
        });
}

// ================= FILTER =================
function filterMessages() {
    const search = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const type = document.getElementById("typeFilter").value;

    const filtered = allMessages.filter(msg => {
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

// ================= UTIL =================
function formatDate(date) {
    if (!date) return "-";
    return new Date(date).toLocaleString();
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}