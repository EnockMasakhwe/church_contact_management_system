document.addEventListener("DOMContentLoaded", loadMessages);

function loadMessages() {
    fetch("/messages")
        .then(response => response.json())
        .then(data => {
            const table = document.getElementById("messageTable");
            table.innerHTML = "";

            data.forEach(msg => {
                const row = `
                    <tr>
                        <td>${msg.messageId}</td>
                        <td>${msg.name}</td>
                        <td>${msg.email}</td>
                        <td>${msg.message}</td>
                        <td>${msg.status}</td>
                        <td>
                            <button onclick="markAsRead(${msg.messageId})">Mark Read</button>
                            <button onclick="deleteMessage(${msg.messageId})">Delete</button>
                        </td>
                    </tr>
                `;
                table.innerHTML += row;
            });
        })
        .catch(error => console.error("Error:", error));
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