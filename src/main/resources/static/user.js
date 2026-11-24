const token = localStorage.getItem("token");

if (!token) {
    showToast("Please login first", "warning");
    setTimeout(() => window.location.href = "login.html", 1500);
}

let messagesPage = 0;
let appointmentsPage = 0;
const dashboardPageSize = 8;

// load
document.addEventListener("DOMContentLoaded", () => {
    if (!token) return;

    loadMyMessages();
    loadMyAppointments();
});

async function loadMyMessages(page = messagesPage) {
    messagesPage = page;
    const table = document.getElementById("myMessagesTable");
    table.innerHTML = `<tr><td class="loading-row" colspan="5">Loading messages...</td></tr>`;
    try {
        const messages = await apiFetch(`/api/user/messages?page=${messagesPage}&size=${dashboardPageSize}`);
        displayMyMessages(messages);
    } catch (err) {
        if (err.status === 401) {
            showToast("Session expired", "error");
            setTimeout(logout, 1500);
        } else {
            showToast(err.message, "error");
        }
    }
}

function displayMyMessages(pageData) {
    const table = document.getElementById("myMessagesTable");
    if (!table) return;

    table.innerHTML = "";
    const messages = pageData.content || [];

    if (messages.length === 0) {
        table.innerHTML = `<tr><td class="empty-row" colspan="5">You have not sent any messages yet.</td></tr>`;
    } else messages.forEach(msg => {
        table.innerHTML += `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.type}</td>
                <td>${escapeHtml(msg.message)}</td>
                <td><span class="status ${msg.status}">${msg.status}</span></td>
                <td>${formatDate(msg.createdAt)}</td>
            </tr>
        `;
    });
    renderDashboardPagination("myMessagesPagination", pageData, loadMyMessages);
}

async function loadMyAppointments(page = appointmentsPage) {
    appointmentsPage = page;
    const table = document.getElementById("myAppointmentsTable");
    table.innerHTML = `<tr><td class="loading-row" colspan="6">Loading appointments...</td></tr>`;
    try {
        const appointments = await apiFetch(`/api/user/appointments?page=${appointmentsPage}&size=${dashboardPageSize}`);
        displayMyAppointments(appointments);
    } catch (err) {
        if (err.status === 401) {
            showToast("Session expired", "error");
            setTimeout(logout, 1500);
        } else {
            showToast(err.message, "error");
        }
    }
}

function displayMyAppointments(pageData) {
    const table = document.getElementById("myAppointmentsTable");
    if (!table) return;

    table.innerHTML = "";
    const apps = pageData.content || [];

    if (apps.length === 0) {
        table.innerHTML = `<tr><td class="empty-row" colspan="6">You have no appointment requests yet.</td></tr>`;
    } else apps.forEach(app => {
        table.innerHTML += `
            <tr>
                <td>${app.id}</td>
                <td>${escapeHtml(app.message)}</td>
                <td>${formatDate(app.startTime)}</td>
                <td>${formatDate(app.endTime)}</td>
                <td><span class="status ${app.status}">${app.status}</span></td>
                <td>${canCancelAppointment(app) ? `<button class="action-btn delete-btn" onclick="cancelAppointment(${app.id})">Cancel</button>` : "-"}</td>
            </tr>
        `;
    });
    renderDashboardPagination("myAppointmentsPagination", pageData, loadMyAppointments);
}

function escapeHtml(value) {
    return String(value ?? "-").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function renderDashboardPagination(containerId, pageData, loadPage) {
    const container = document.getElementById(containerId);
    if (!container) return;
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

function canCancelAppointment(appointment) {
    return (appointment.status === "PENDING" || appointment.status === "APPROVED")
        && new Date(appointment.startTime) > new Date();
}

async function cancelAppointment(appointmentId) {
    if (!confirm("Cancel this appointment request? The time slot will become available to other members.")) {
        return;
    }

    try {
        await apiFetch(`/api/user/appointments/${appointmentId}/cancel`, { method: "PUT" });
        showToast("Appointment cancelled", "success");
        await loadMyAppointments();
    } catch (err) {
        if (err.status === 401) {
            showToast("Session expired", "error");
            setTimeout(logout, 1500);
        } else {
            showToast(err.message, "error");
        }
    }
}

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