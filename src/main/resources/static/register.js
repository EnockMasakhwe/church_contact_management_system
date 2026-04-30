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

// response
async function handleResponse(response) {
    if (!response.ok) {
        let msg = "Request failed";

        try {
            const data = await response.json();
            msg = data.message || JSON.stringify(data);
        } catch {
            const text = await response.text();
            if (text) msg = text;
        }

        throw new Error(msg);
    }
    return response.json();
}

// register
document.getElementById("registerForm").addEventListener("submit", async e => {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const res = await fetch("http://localhost:8080/api/auth/register", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ username, email, password })
        });

        await handleResponse(res);

        showToast("Registration successful", "success");

        setTimeout(() => window.location.href = "login.html", 1500);

    } catch (err) {
        showToast(err.message, "error");
    }
});