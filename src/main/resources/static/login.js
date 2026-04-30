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

// toggle forgot password
function toggleForgotPassword() {
    const box = document.getElementById("forgotPasswordBox");
    box.style.display = box.style.display === "none" ? "block" : "none";
}

// send reset link
async function sendResetLink() {
    const email = document.getElementById("forgotEmail").value;

    if (!email) {
        showToast("Enter your email", "error");
        return;
    }

    try {
        const res = await fetch("http://localhost:8080/api/auth/forgot-password", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ email })
        });

        const msg = await res.text();

        if (!res.ok) throw new Error(msg);

        showToast(msg || "Reset link sent", "success");

    } catch (err) {
        showToast(err.message, "error");
    }
}