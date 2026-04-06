//Auth check
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token || role !== "ROLE_ADMIN") {
    alert("Access denied. Admins only.");
    window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", loadMessages);

let allMessages = [];

function loadMessages() {
    fetch("/api/admin/messages", {
        headers: {
            "Authorization": "Bearer " + token
        }
    })
        .then(res => res.json())
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
        const row = `
            <tr>
                <td>${msg.id}</td>
                <td>${msg.name}</td>
                <td>${msg.email}</td>
                <td>${msg.message}</td>
                <td>${msg.status}</td>
                <td>
                    <button onclick="markAsRead(${msg.id})">Read</button>
                    <button onclick="deleteMessage(${msg.id})">Delete</button>
                </td>
            </tr>
        `;
        table.innerHTML += row;
    });
}

function filterMessages() {
    const search = document.getElementById("searchInput").value.toLowerCase();
    const status = document.getElementById("statusFilter").value;

    let filtered = allMessages.filter(msg => {
        const matchesSearch =
            msg.name.toLowerCase().includes(search) ||
            msg.email.toLowerCase().includes(search);

        const matchesStatus =
            status === "" || msg.status === status;

        return matchesSearch && matchesStatus;
    });

    displayMessages(filtered);
}

function deleteMessage(id) {
    fetch(`/api/admin/messages/${id}`, {
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

function markAsRead(id) {
    fetch(`/api/admin/messages/${id}/status`, {   
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
            status: "READ"
        })
    })
        .then(() => {
            alert("Marked as READ");
            loadMessages();
        })
        .catch(error => console.error(error));
}