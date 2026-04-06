const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (token) {
    if (role === "ROLE_ADMIN") {
        window.location.href = "admin.html";
    } else {
        window.location.href = "contact.html";
    }
}

document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const data = {
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    try {
        const response = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error("Invalid credentials");
        }

        const result = await response.json();

        // Store token + role
        localStorage.setItem("token", result.token);
        localStorage.setItem("role", result.role);

        alert("Login successful!");

        // Redirect
        if (result.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
        } else {
            window.location.href = "contact.html";
        }

    } catch (error) {
        console.error(error);
        alert("Login failed!");
    }
});
