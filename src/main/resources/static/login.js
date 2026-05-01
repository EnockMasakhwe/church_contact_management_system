const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (token) {
    window.location.href = role === "ROLE_ADMIN" ? "admin.html" : "message.html";
}

// login
document.getElementById("loginForm").addEventListener("submit", async e => {
    e.preventDefault();

    try {
        const data = await apiFetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: this.email.value,
                password: this.password.value
            })
        });

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
        const msg = await apiFetch("http://localhost:8080/api/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({ email })
        });

        showToast(msg || "Reset link sent", "success");

    } catch (err) {
        showToast(err.message, "error");
    }
}