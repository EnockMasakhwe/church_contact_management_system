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
    document.getElementById("resendVerificationBox").style.display = "none";
    box.style.display = box.style.display === "none" ? "block" : "none";
}

function toggleResendVerification() {
    const box = document.getElementById("resendVerificationBox");
    document.getElementById("forgotPasswordBox").style.display = "none";
    box.style.display = box.style.display === "none" ? "block" : "none";
}

// send reset link
async function sendResetLink() {
    const email = document.getElementById("forgotEmail").value;

    if (!email.trim()) {
        showToast("Enter your email", "warning");
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

async function resendVerificationEmail() {
    const email = document.getElementById("verificationEmail").value;
    if (!email.trim()) {
        showToast("Enter your email", "warning");
        return;
    }

    try {
        const message = await apiFetch("/api/auth/resend-verification", {
            method: "POST",
            body: JSON.stringify({ email })
        });
        showToast(message, "success");
    } catch (err) {
        showToast(err.message, "error");
    }
}