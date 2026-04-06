document.addEventListener("DOMContentLoaded", loadMessages);
let allMessages = [];

function loadMessages() {
    fetch("/messages")
        .then(res => res.json())
        .then(data => {
            allMessages = data;
            displayMessages(data);
        });
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
    fetch(`/messages/${id}`, {
        method: "DELETE"
    })
        .then(() => {
            alert("Deleted successfully");
            loadMessages(); // refresh table
        })
        .catch(error => console.error(error));
}

function markAsRead(id) {
    fetch(`/messages/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
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