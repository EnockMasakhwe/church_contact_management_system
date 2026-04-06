document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const data = {
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    const result = await response.json();

    //Store token
    localStorage.setItem("token", result.token);
    localStorage.setItem("role", result.role);

    alert("Login successful!");

    //Redirect based on role
    if (result.role === "ROLE_ADMIN") {
        window.location.href = "admin.html";
    } else {
        window.location.href = "contact.html";
    }
});