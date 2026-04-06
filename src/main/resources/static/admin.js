const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (!token || role !== "ROLE_ADMIN") {
    alert("Access denied. Admins only.");
    window.location.href = "login.html";
}

document.addEventListener("DOMContentLoaded", loadMessages);

let allMessages = [];

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
        .catch(error => console.error(error));
}

function displayMessages(messages) {
    const table = document.getElementById("messageTable");
    table.innerHTML = "";

    messages.forEach(msg => {
        const row = `
            <tr>
                <td>${msg.messageId}</td>
                <td>${msg.name}</td>
                <td>${msg.email}</td>
                <td>${msg.message}</td>
                <td>${msg.status}</td>
                <td>
                    <button onclick="markAsRead(${msg.messageId})">Read</button>
                    <button onclick="deleteMessage(${msg.messageId})">Delete</button>
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
    fetch(`http://localhost:8080/api/admin/messages/delete/{id}`, {
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
    fetch(`http://localhost:8080/api/admin/messages/status/{id}`, {
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

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}