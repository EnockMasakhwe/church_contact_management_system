const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (token) {
    window.location.href = role === "ROLE_ADMIN" ? "admin.html" : "message.html";
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

// response
async function handleResponse(response) {
    if (!response.ok) {
        let msg = "Login failed";
        try {
            const data = await response.json();
            msg = data.message || msg;
        } catch {
            msg = await response.text();
        }
        throw new Error(msg);
    }
    return response.json();
}

// login
document.getElementById("loginForm").addEventListener("submit", async e => {
    e.preventDefault();

    try {
        const res = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                email: email.value,
                password: password.value
            })
        });

        const data = await handleResponse(res);

        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);

        showToast("Login successful", "success");

        setTimeout(() => {
            window.location.href =
                data.role === "ROLE_ADMIN" ? "admin.html" : "message.html";
        }, 1000);

    } catch (err) {
        showToast(err.message, "error");
    }
});