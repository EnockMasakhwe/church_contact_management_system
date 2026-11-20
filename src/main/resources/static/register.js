// register
document.getElementById("registerForm").addEventListener("submit", async e => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const submitButton = e.currentTarget.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    try {
        await apiFetch("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ username, email, password })
        });

        document.getElementById("registeredEmail").textContent = email;
        document.getElementById("registerForm").hidden = true;
        document.querySelector(".auth-subtitle").hidden = true;
        document.getElementById("registrationConfirmation").hidden = false;
        showToast("Registration received", "success");

    } catch (err) {
        showToast(err.message, "error");
        submitButton.disabled = false;
    }
});