const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

if (token) {
    if (role === "ROLE_ADMIN") {
        window.location.href = "admin.html";
    } else {
        window.location.href = "contact.html";
    }
}

//LOGIN
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

        localStorage.setItem("token", result.token);
        localStorage.setItem("role", result.role);

        alert("Login successful!");

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

//TOGGLE FORGOT PASSWORD UI
function toggleForgotPassword() {
    const box = document.getElementById("forgotPasswordBox");
    box.style.display = box.style.display === "none" ? "block" : "none";
}

//SEND RESET LINK
async function sendResetLink() {

    const email = document.getElementById("forgotEmail").value;
    const msg = document.getElementById("forgotMsg");

    if (!email) {
        msg.innerText = "Please enter your email";
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/api/auth/forgot-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email: email })
        });

        if (!response.ok) {
            throw new Error("Request failed");
        }

        const result = await response.text();

        msg.innerText = result;

    } catch (error) {
        console.error(error);
        msg.innerText = "Error sending reset email";
    }
}