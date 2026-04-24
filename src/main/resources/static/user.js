const token = localStorage.getItem("token");

if (!token) {
    alert("Please login first");
    window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", () => {
    loadMyMessages();
    loadMyAppointments();
});

//MESSAGES
function loadMyMessages() {
    fetch("http://localhost:8080/api/user/messages", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => handleAuth(res))
        .then(res => res.json())
        .then(data => displayMyMessages(data));
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

//APPOINTMENTS
function loadMyAppointments() {
    fetch("http://localhost:8080/api/user/appointments", {
        headers: { "Authorization": "Bearer " + token }
    })
        .then(res => handleAuth(res))
        .then(res => res.json())
        .then(data => displayMyAppointments(data));
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

//UTIL
function formatDate(date) {
    return date ? new Date(date).toLocaleString() : "-";
}

function handleAuth(res) {
    if (res.status === 401 || res.status === 403) {
        alert("Session expired. Login again.");
        logout();
        throw new Error("Unauthorized");
    }
    return res;
}

function goToMessages() {
    window.location.href = "message.html";
}

function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}