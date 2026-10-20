// login
document.getElementById("loginForm").addEventListener("submit", async e => {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const data = await apiFetch("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });

        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);

        showToast("Login successful", "success");

        setTimeout(() => {
            window.location.href =
                data.role === "ROLE_ADMIN" || data.role === "ROLE_SUPER_ADMIN"
                    ? "admin.html"
                    : "message.html";
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
        const msg = await apiFetch("/api/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({ email })
        });

        showToast(msg || "Reset link sent", "success");

    } catch (err) {
        showToast(err.message, "error");
    }
}