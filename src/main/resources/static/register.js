// register
document.getElementById("registerForm").addEventListener("submit", async e => {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const data = await apiFetch("${BASE_URL}/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ username, email, password })
        });

        showToast("Registration successful", "success");

        setTimeout(() => window.location.href = "login.html", 1500);

    } catch (err) {
        console.error(err);
    }
});