const token = localStorage.getItem("token");

if (!token) {
    showToast("Please login first", "warning");
    setTimeout(() => window.location.href = "login.html", 1500);
}

// load
document.addEventListener("DOMContentLoaded", () => {
    loadMyMessages();
    loadMyAppointments();
});

async function loadMyMessages() {
    try {
        const messages = await apiFetch("http://localhost:8080/api/user/messages");
        displayMyMessages(messages);
    } catch (err) {
        // Check if it's an auth error
        if (err.message === "Unauthorized" || err.message.includes("401") || err.message.includes("403")) {
            showToast("Session expired", "error");
            setTimeout(logout, 1500);
        }
        // Toast is already shown by apiFetch, but you can add custom handling here
        console.error(err);
    }
}

function displayMyMessages(messages) {
    const table = document.getElementById("myMessagesTable");
    if (!table) return;

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

async function loadMyAppointments() {
    try {
        const appointments = await apiFetch("http://localhost:8080/api/user/appointments");
        displayMyAppointments(appointments);
    } catch (err) {
        // Check if it's an auth error
        if (err.message === "Unauthorized" || err.message.includes("401") || err.message.includes("403")) {
            showToast("Session expired", "error");
            setTimeout(logout, 1500);
        }
        console.error(err);
    }
}

function displayMyAppointments(apps) {
    const table = document.getElementById("myAppointmentsTable");
    if (!table) return;

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