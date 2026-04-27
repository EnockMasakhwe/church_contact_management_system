const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    showToast("Access denied", "error");
    setTimeout(() => window.location.href = "login.html", 1500);
}

let allMessages = [];
let filteredMessages = [];

let currentPage = 1;
const rowsPerPage = 8;

document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadMessages();
    loadAppointments();
});

// toast
function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// response handler
async function handleResponse(response) {
    if (!response.ok) {
        let msg = "Request failed";
        try {
            const data = await response.json();
            msg = data.message || data.error || msg;
        } catch {
            msg = await response.text();
        }
        throw new Error(msg);
    }
    return response.json();
}

// auth error
function handleAuthError(err) {
    if (err.message.toLowerCase().includes("unauthorized")) {
        showToast("Session expired", "error");
        setTimeout(logout, 1500);
    } else {
        showToast(err.message, "error");
    }
}

// load messages
function loadMessages() {
    fetch("http://localhost:8080/api/admin/messages", {
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleResponse)
        .then(data => {
            allMessages = data;
            filteredMessages = data;
            currentPage = 1;
            displayMessages();
        })
        .catch(handleAuthError);
}

// display messages
function displayMessages() {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    const start = (currentPage - 1) * rowsPerPage;
    const paginated = filteredMessages.slice(start, start + rowsPerPage);

    paginated.forEach(msg => {
        table.innerHTML += `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.name || "-"}</td>
                <td>${msg.email || "-"}</td>
                <td>${msg.type}</td>
                <td>${msg.message}</td>
                <td>${msg.location || "-"}</td>
                <td>${formatDate(msg.preferredDateTime)}</td>
                <td><span class="status ${msg.status}">${msg.status}</span></td>
                <td>${buildActions(msg)}</td>
            </tr>
        `;
    });

    renderPagination();
}

// filter
function filterMessages() {
    const search = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;
    const type = document.getElementById("typeFilter").value;

    filteredMessages = allMessages.filter(msg =>
        (msg.message?.toLowerCase().includes(search) ||
            msg.name?.toLowerCase().includes(search) ||
            msg.email?.toLowerCase().includes(search)) &&
        (!status || msg.status === status) &&
        (!type || msg.type === type)
    );

    currentPage = 1;
    displayMessages();
}

// pagination
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

// actions
function buildActions(msg) {
    let btn = "";

    if (msg.status === "NEW")
        btn += `<button class="action-btn read-btn" onclick="updateStatus(${msg.id},'READ')">Read</button>`;
    if (msg.status === "READ")
        btn += `<button class="action-btn progress-btn" onclick="updateStatus(${msg.id},'IN_PROGRESS')">Start</button>`;
    if (msg.status === "IN_PROGRESS")
        btn += `<button class="action-btn respond-btn" onclick="updateStatus(${msg.id},'RESPONDED')">Respond</button>`;
    if (msg.status === "RESPONDED")
        btn += `<button class="action-btn close-btn" onclick="updateStatus(${msg.id},'CLOSED')">Close</button>`;

    btn += `<button class="action-btn delete-btn" onclick="deleteMessage(${msg.id})">Delete</button>`;
    return btn;
}

function updateStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/messages/${id}/status`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + token
        },
        body: JSON.stringify({ status })
    })
        .then(handleResponse)
        .then(() => {
            showToast("Status updated", "success");
            loadMessages();
        })
        .catch(handleAuthError);
}

function deleteMessage(id) {
    fetch(`http://localhost:8080/api/admin/messages/${id}`, {
        method: "DELETE",
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleResponse)
        .then(() => {
            showToast("Message deleted", "success");
            loadMessages();
        })
        .catch(handleAuthError);
}

// appointments
function loadAppointments() {
    fetch("http://localhost:8080/api/admin/appointments", {
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleResponse)
        .then(displayAppointments)
        .catch(handleAuthError);
}

function displayAppointments(apps) {
    const table = document.getElementById("appointmentTable");
    table.innerHTML = "";

    apps.forEach(app => {
        table.innerHTML += `
            <tr>
                <td>${app.id}</td>
                <td>${app.name || "-"}</td>
                <td>${app.email || "-"}</td>
                <td>${app.message || "-"}</td>
                <td>${formatDate(app.startTime)}</td>
                <td>${formatDate(app.endTime)}</td>
                <td><span class="status ${app.status}">${app.status}</span></td>
                <td>${buildAppointmentActions(app)}</td>
            </tr>
        `;
    });
}

function buildAppointmentActions(app) {
    let btn = "";

    if (app.status === "PENDING") {
        btn += `<button class="action-btn read-btn" onclick="updateAppointmentStatus(${app.id},'APPROVED')">Approve</button>`;
        btn += `<button class="action-btn delete-btn" onclick="updateAppointmentStatus(${app.id},'REJECTED')">Reject</button>`;
    }

    if (app.status === "APPROVED") {
        btn += `<button class="action-btn progress-btn" onclick="updateAppointmentStatus(${app.id},'COMPLETED')">Complete</button>`;
    }

    return btn;
}

function updateAppointmentStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/appointments/${id}/status?status=${status}`, {
        method: "PUT",
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleResponse)
        .then(() => {
            showToast("Appointment updated", "success");
            loadAppointments();
        })
        .catch(handleAuthError);
}

// stats
function loadStats() {
    fetch("http://localhost:8080/api/admin/stats", {
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleResponse)
        .then(displayStats)
        .catch(handleAuthError);
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

// util
function formatDate(date) {
    return date ? new Date(date).toLocaleString() : "-";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}