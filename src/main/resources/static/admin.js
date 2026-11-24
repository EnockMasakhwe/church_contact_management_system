const token = localStorage.getItem("token");
const role = localStorage.getItem("role");
const isSuperAdmin = role === "ROLE_SUPER_ADMIN";

if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    showToast("Access denied", "error");
    window.location.href = "login.html";
}

let filteredMessages = [];
let usersPage = 0;
let appointmentsPage = 0;
const tablePageSize = 8;

// pagination
let currentPage = 1;

document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadMessages();
    loadAppointments();
    loadUsers();
});

function loadUsers(page = usersPage) {
    usersPage = page;
    const table = document.getElementById("usersTable");
    table.innerHTML = `<tr><td class="loading-row" colspan="7">Loading users...</td></tr>`;
    apiFetch(`/api/admin/users?page=${usersPage}&size=${tablePageSize}`)
        .then(usersPageData => displayUsers(usersPageData))
        .catch(err => showToast(err.message, "error"));
}

function displayUsers(usersPageData) {
    const table = document.getElementById("usersTable");
    table.innerHTML = "";
    const users = usersPageData.content || [];

    if (users.length === 0) {
        table.innerHTML = `<tr><td class="empty-row" colspan="7">No registered users found.</td></tr>`;
    } else users.forEach(user => {
        const isProtectedSuperAdmin = user.role === "ROLE_SUPER_ADMIN";
        let actions = "";

        if (isSuperAdmin && !isProtectedSuperAdmin) {
            const nextRole = user.role === "ROLE_ADMIN" ? "ROLE_USER" : "ROLE_ADMIN";
            const actionLabel = nextRole === "ROLE_ADMIN" ? "Make admin" : "Remove admin";
            actions += `<button class="action-btn progress-btn" onclick="updateUserRole(${user.id}, '${nextRole}')">${actionLabel}</button>`;
        }

        if (isSuperAdmin && !isProtectedSuperAdmin) {
            actions += `<button class="action-btn delete-btn" onclick="deleteUser(${user.id})">Delete</button>`;
        }

        table.innerHTML += `
            <tr>
                <td>${user.id}</td>
                <td>${escapeHtml(user.username)}</td>
                <td>${escapeHtml(user.email)}</td>
                <td>${escapeHtml(user.role)}</td>
                <td>${user.enabled ? "Yes" : "No"}</td>
                <td>${formatDate(user.createdAt)}</td>
                <td>${actions || "-"}</td>
            </tr>
        `;
    });
    renderTablePagination("usersPagination", usersPageData, loadUsers);
}

function updateUserRole(userId, nextRole) {
    apiFetch(`/api/admin/users/${userId}/role`, {
        method: "PUT",
        body: JSON.stringify({ role: nextRole })
    })
        .then(() => {
            showToast("User role updated", "success");
            loadUsers();
        })
        .catch(err => showToast(err.message, "error"));
}

function deleteUser(userId) {
    if (!confirm("Delete this account? Its messages and appointments will also be deleted.")) {
        return;
    }

    apiFetch(`/api/admin/users/${userId}`, { method: "DELETE" })
        .then(() => {
            showToast("User deleted", "success");
            loadUsers();
        })
        .catch(err => showToast(err.message, "error"));
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

// LOAD MESSAGES
function loadMessages(page = currentPage - 1) {
    const search = document.getElementById("searchInput")?.value.trim();
    const status = document.getElementById("statusFilter")?.value;
    const type = document.getElementById("typeFilter")?.value;
    const params = new URLSearchParams({
        page: String(page),
        size: String(tablePageSize)
    });
    if (search) params.set("search", search);
    if (status) params.set("status", status);
    if (type) params.set("type", type);

    const table = document.getElementById("messageTable");
    table.innerHTML = `<tr><td class="loading-row" colspan="9">Loading messages...</td></tr>`;
    apiFetch(`/api/admin/messages?${params}`)
        .then(data => {
            filteredMessages = data.content || [];
            currentPage = data.number + 1;
            displayMessages(data);
        })
        .catch(err => showToast(err.message, "error"));
}

// DISPLAY MESSAGES
function displayMessages(pageData) {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    if (filteredMessages.length === 0) {
        table.innerHTML = `<tr><td class="empty-row" colspan="9">No messages match your filters.</td></tr>`;
    } else {

    filteredMessages.forEach(msg => {
        table.innerHTML += `
            <tr>
                <td>${msg.id}</td>
                <td>${escapeHtml(msg.name)}</td>
                <td>${escapeHtml(msg.email)}</td>
                <td>${escapeHtml(msg.type)}</td>
                <td>${escapeHtml(msg.message)}</td>
                <td>${escapeHtml(msg.location)}</td>
                <td>${formatDate(msg.preferredDateTime)}</td>
                <td><span class="status ${msg.status}">${msg.status}</span></td>
                <td>${buildActions(msg)}</td>
            </tr>
        `;
    });
    }
    renderTablePagination("pagination", pageData, page => loadMessages(page));
}

// FILTER
function filterMessages() {
    currentPage = 1;
    loadMessages(0);
}

// pagination UI
function renderTablePagination(containerId, pageData, loadPage) {
    const container = document.getElementById(containerId);
    if (!container || !pageData) return;
    container.innerHTML = "";
    if (pageData.totalPages <= 1) return;

    const previous = document.createElement("button");
    previous.textContent = "Previous";
    previous.disabled = pageData.first;
    previous.onclick = () => loadPage(pageData.number - 1);
    container.appendChild(previous);

    for (let i = 0; i < pageData.totalPages; i++) {
        const button = document.createElement("button");
        button.textContent = i + 1;
        button.className = i === pageData.number ? "active-page" : "";
        button.onclick = () => loadPage(i);
        container.appendChild(button);
    }

    const next = document.createElement("button");
    next.textContent = "Next";
    next.disabled = pageData.last;
    next.onclick = () => loadPage(pageData.number + 1);
    container.appendChild(next);
}

// ACTIONS
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
    apiFetch(`/api/admin/messages/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status })
    })
        .then(() => {
            showToast("Status updated", "success");
            loadMessages();
        })
        .catch(err => showToast(err.message, "error"));
}

// DELETE
function deleteMessage(id) {
    apiFetch(`/api/admin/messages/${id}`, {
        method: "DELETE"
    })
        .then(() => {
            showToast("Message deleted", "success");
            loadMessages();
        })
        .catch(err => showToast(err.message, "error"));
}

// APPOINTMENTS
function loadAppointments(page = appointmentsPage) {
    appointmentsPage = page;
    const table = document.getElementById("appointmentTable");
    table.innerHTML = `<tr><td class="loading-row" colspan="8">Loading appointments...</td></tr>`;
    apiFetch(`/api/admin/appointments?page=${appointmentsPage}&size=${tablePageSize}`)
        .then(data => displayAppointments(data))
        .catch(err => showToast(err.message, "error"));
}

function displayAppointments(pageData) {
    const table = document.getElementById("appointmentTable");
    table.innerHTML = "";
    const apps = pageData.content || [];

    if (apps.length === 0) {
        table.innerHTML = `<tr><td class="empty-row" colspan="8">No appointments found.</td></tr>`;
    } else apps.forEach(app => {
        table.innerHTML += `
            <tr>
                <td>${app.id}</td>
                <td>${escapeHtml(app.name)}</td>
                <td>${escapeHtml(app.email)}</td>
                <td>${escapeHtml(app.message)}</td>
                <td>${formatDate(app.startTime)}</td>
                <td>${formatDate(app.endTime)}</td>
                <td><span class="status ${app.status}">${app.status}</span></td>
                <td>${buildAppointmentActions(app)}</td>
            </tr>
        `;
    });
    renderTablePagination("appointmentPagination", pageData, loadAppointments);
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
    apiFetch(`/api/admin/appointments/${id}/status?status=${status}`, {
        method: "PUT"
    })
        .then(() => {
            showToast("Appointment updated", "success");
            loadAppointments();
        })
        .catch(err => showToast(err.message, "error"));
}

// STATS
function loadStats() {
    apiFetch("/api/admin/stats")
        .then(data => displayStats(data))
        .catch(err => showToast(err.message, "error"));
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