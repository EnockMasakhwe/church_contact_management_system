const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token || (role !== "ROLE_ADMIN" && role !== "ROLE_SUPER_ADMIN")) {
    alert("Access denied.");
    window.location.href = "login.html";
}

let allMessages = [];

document.addEventListener("DOMContentLoaded", () => {
    loadMessages();
    loadAppointments();
});

//LOAD
function loadMessages() {
    fetch("http://localhost:8080/api/admin/messages", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => res.json())
        .then(data => {
            allMessages = data;
            displayMessages(data);
        });
}

//DISPLAY
function displayMessages(messages) {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    messages.forEach(msg => {
        const row = `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.name || "-"}</td>
                <td>${msg.email || "-"}</td>
                <td>${msg.type}</td>
                <td>${msg.message}</td>
                <td>${msg.location || "-"}</td>
                <td>${formatDate(msg.preferredDateTime)}</td>
                <td>${msg.status}</td>
                <td>${buildActions(msg)}</td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

//ACTIONS
function buildActions(msg) {
    let buttons = "";

    if (msg.status === "NEW") {
        buttons += `<button onclick="updateStatus(${msg.id}, 'READ')">Read</button>`;
    }
    if (msg.status === "READ") {
        buttons += `<button onclick="updateStatus(${msg.id}, 'IN_PROGRESS')">Start</button>`;
    }
    if (msg.status === "IN_PROGRESS") {
        buttons += `<button onclick="updateStatus(${msg.id}, 'RESPONDED')">Responded</button>`;
    }
    if (msg.status === "RESPONDED") {
        buttons += `<button onclick="updateStatus(${msg.id}, 'CLOSED')">Close</button>`;
    }

    buttons += `<button onclick="deleteMessage(${msg.id})">Delete</button>`;

    return buttons;
}

//UPDATE
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

//DELETE
function deleteMessage(id) {
    fetch(`http://localhost:8080/api/admin/messages/${id}`, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + token }
    }).then(() => loadMessages());
}

//APPOINTMENTS
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
                <td>${app.status}</td>
                <td>${buildAppointmentActions(app)}</td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

function buildAppointmentActions(app) {
    let buttons = "";

    if (app.status === "PENDING") {
        buttons += `<button onclick="updateAppointmentStatus(${app.id}, 'APPROVED')">Approve</button>`;
        buttons += `<button onclick="updateAppointmentStatus(${app.id}, 'REJECTED')">Reject</button>`;
    }

    if (app.status === "APPROVED") {
        buttons += `<button onclick="updateAppointmentStatus(${app.id}, 'COMPLETED')">Complete</button>`;
    }

    return buttons;
}

function updateAppointmentStatus(id, status) {
    fetch(`http://localhost:8080/api/admin/appointments/${id}/status?status=${status}`, {
        method: "PUT",
        headers: { "Authorization": "Bearer " + token }
    }).then(() => loadAppointments());
}

//UTIL
function formatDate(date) {
    return date ? new Date(date).toLocaleString() : "-";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}