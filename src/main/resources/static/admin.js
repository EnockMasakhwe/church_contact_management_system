const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    alert("Access denied.");
    window.location.href = "login.html";
}

let allMessages = [];
let filteredMessages = [];

// PAGINATION
let currentPage = 1;
const rowsPerPage = 8;

document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadMessages();
    loadAppointments();
});

// LOAD MESSAGES
function loadMessages() {
    fetch("http://localhost:8080/api/admin/messages", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => res.json())
        .then(data => {
            allMessages = data;
            filteredMessages = data;
            currentPage = 1;
            displayMessages();
        });
}

// DISPLAY MESSAGES (WITH PAGINATION)
function displayMessages() {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    const start = (currentPage - 1) * rowsPerPage;
    const paginated = filteredMessages.slice(start, start + rowsPerPage);

    paginated.forEach(msg => {
        const row = `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.name || "-"}</td>
                <td>${msg.email || "-"}</td>
                <td>${msg.type}</td>
                <td>${msg.message}</td>
                <td>${msg.location || "-"}</td>
                <td>${formatDate(msg.preferredDateTime)}</td>
                <td>
                    <span class="status ${msg.status}">
                        ${msg.status}
                    </span>
                </td>
                <td>${buildActions(msg)}</td>
            </tr>
        `;
        table.innerHTML += row;
    });

    renderPagination();
}

// FILTER MESSAGES
function filterMessages() {
    const search = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const type = document.getElementById("typeFilter").value;

    filteredMessages = allMessages.filter(msg => {
        const matchesSearch =
            msg.message?.toLowerCase().includes(search) ||
            msg.name?.toLowerCase().includes(search) ||
            msg.email?.toLowerCase().includes(search);

        const matchesStatus = status ? msg.status === status : true;
        const matchesType = type ? msg.type === type : true;

        return matchesSearch && matchesStatus && matchesType;
    });

    currentPage = 1; // reset page
    displayMessages();
}

// PAGINATION UI
function renderPagination() {
    const totalPages = Math.ceil(filteredMessages.length / rowsPerPage);
    const container = document.getElementById("pagination");
    if (!container) return;

    container.innerHTML = "";

    for (let i = 1; i <= totalPages; i++) {
        container.innerHTML += `
            <button class="page-btn ${i === currentPage ? "active-page" : ""}"
                onclick="goToPage(${i})">${i}</button>
        `;
    }
}

function goToPage(page) {
    currentPage = page;
    displayMessages();
}

// ACTION BUTTONS
function buildActions(msg) {
    let buttons = "";

    if (msg.status === "NEW") {
        buttons += `<button class="action-btn read-btn" onclick="updateStatus(${msg.id}, 'READ')">Read</button>`;
    }
    if (msg.status === "READ") {
        buttons += `<button class="action-btn progress-btn" onclick="updateStatus(${msg.id}, 'IN_PROGRESS')">Start</button>`;
    }
    if (msg.status === "IN_PROGRESS") {
        buttons += `<button class="action-btn respond-btn" onclick="updateStatus(${msg.id}, 'RESPONDED')">Respond</button>`;
    }
    if (msg.status === "RESPONDED") {
        buttons += `<button class="action-btn close-btn" onclick="updateStatus(${msg.id}, 'CLOSED')">Close</button>`;
    }

    buttons += `<button class="action-btn delete-btn" onclick="deleteMessage(${msg.id})">Delete</button>`;

    return buttons;
}

// UPDATE STATUS
function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/messages/${id}/status`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ status })
    }).then(() => loadMessages());
}

// DELETE
function deleteMessage(id) {
    fetch(`http://localhost:8080/api/admin/messages/${id}`, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + token }
    }).then(() => loadMessages());
}

// APPOINTMENTS
function loadAppointments() {
    fetch("http://localhost:8080/api/admin/appointments", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => res.json())
        .then(data => displayAppointments(data));
}

function displayAppointments(appointments) {
    const table = document.getElementById("appointmentTable");
    table.innerHTML = "";

    appointments.forEach(app => {
        const row = `
            <tr>
                <td>${app.id}</td>
                <td>${app.name || "-"}</td>
                <td>${app.email || "-"}</td>
                <td>${app.message || "-"}</td>
                <td>${formatDate(app.startTime)}</td>
                <td>${formatDate(app.endTime)}</td>
                <td>
                    <span class="status ${app.status}">
                        ${app.status}
                    </span>
                </td>
                <td>${buildAppointmentActions(app)}</td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

// APPOINTMENT ACTIONS
function buildAppointmentActions(app) {
    let buttons = "";

    if (app.status === "PENDING") {
        buttons += `<button class="action-btn read-btn" onclick="updateAppointmentStatus(${app.id}, 'APPROVED')">Approve</button>`;
        buttons += `<button class="action-btn delete-btn" onclick="updateAppointmentStatus(${app.id}, 'REJECTED')">Reject</button>`;
    }

    if (app.status === "APPROVED") {
        buttons += `<button class="action-btn progress-btn" onclick="updateAppointmentStatus(${app.id}, 'COMPLETED')">Complete</button>`;
    }

    return buttons;
}

function updateAppointmentStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/appointments/${id}/status?status=${status}`, {
        method: "PUT",
        headers: { "Authorization": "Bearer " + token }
    }).then(() => loadAppointments());
}

// UTIL
function formatDate(date) {
    return date ? new Date(date).toLocaleString() : "-";
}

function goToDashboard() {
    window.location.href = "user.html";
}

function goToMessages() {
    window.location.href = "message.html";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}

// STATS
function loadStats() {
    fetch("http://localhost:8080/api/admin/stats", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => res.json())
        .then(data => displayStats(data));
}

function displayStats(stats) {
    const container = document.getElementById("statsContainer");

    container.innerHTML = `
        <div class="stat-card"><h3>${stats.totalMessages}</h3><p>Total</p></div>
        <div class="stat-card"><h3>${stats.newMessages}</h3><p>New</p></div>
        <div class="stat-card"><h3>${stats.inProgressMessages}</h3><p>In Progress</p></div>
        <div class="stat-card"><h3>${stats.closedMessages}</h3><p>Closed</p></div>
        <div class="stat-card"><h3>${stats.totalAppointments}</h3><p>Appointments</p></div>
        <div class="stat-card"><h3>${stats.pendingAppointments}</h3><p>Pending</p></div>
        <div class="stat-card"><h3>${stats.approvedAppointments}</h3><p>Approved</p></div>
        <div class="stat-card"><h3>${stats.completedAppointments}</h3><p>Completed</p></div>
    `;
}