const token = localStorage.getItem("token");

if (!token) {
    showToast("Please login first", "warning");
    setTimeout(() => window.location.href = "login.html", 1500);
}

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

// auth
function handleAuth(res) {
    if (res.status === 401 || res.status === 403) {
        showToast("Session expired", "error");
        setTimeout(logout, 1500);
        throw new Error("Unauthorized");
    }
    return res;
}

// load
document.addEventListener("DOMContentLoaded", () => {
    loadMyMessages();
    loadMyAppointments();
});

function loadMyMessages() {
    fetch("http://localhost:8080/api/user/messages", {
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleAuth)
        .then(res => res.json())
        .then(displayMyMessages)
        .catch(err => showToast(err.message, "error"));
}

function displayMyMessages(messages) {
    const table = document.getElementById("myMessagesTable");
    table.innerHTML = "";

    messages.forEach(msg => {
        table.innerHTML += `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.type}</td>
                <td>${msg.message}</td>
                <td><span class="status ${msg.status}">${msg.status}</span></td>
                <td>${formatDate(msg.createdAt)}</td>
            </tr>
        `;
    });
}

function loadMyAppointments() {
    fetch("http://localhost:8080/api/user/appointments", {
        headers: { Authorization: "Bearer " + token }
    })
        .then(handleAuth)
        .then(res => res.json())
        .then(displayMyAppointments)
        .catch(err => showToast(err.message, "error"));
}

function displayMyAppointments(apps) {
    const table = document.getElementById("myAppointmentsTable");
    table.innerHTML = "";

    apps.forEach(app => {
        table.innerHTML += `
            <tr>
                <td>${app.id}</td>
                <td>${app.message || "-"}</td>
                <td>${formatDate(app.startTime)}</td>
                <td>${formatDate(app.endTime)}</td>
                <td><span class="status ${app.status}">${app.status}</span></td>
            </tr>
        `;
    });
}

function formatDate(date) {
    return date ? new Date(date).toLocaleString() : "-";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}