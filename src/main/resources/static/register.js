function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

async function handleResponse(response) {
    if (!response.ok) {
        let msg = "Registration failed";
        try {
            const data = await response.json();
            msg = data.message || msg;
        } catch {
            msg = await response.text();
        }
        throw new Error(msg);
    }
}

document.getElementById("registerForm").addEventListener("submit", async e => {
    e.preventDefault();

    try {
        const res = await fetch("http://localhost:8080/api/auth/register", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                username: username.value,
                email: email.value,
                password: password.value
            })
        });

        await handleResponse(res);

        showToast("Registration successful", "success");

        setTimeout(() => window.location.href = "login.html", 1500);

    } catch (err) {
        showToast(err.message, "error");
    }
});