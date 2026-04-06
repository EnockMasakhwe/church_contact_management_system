//Check if user is logged in
const token = localStorage.getItem("token");

if (!token) {
    alert("Please login first");
    window.location.href = "login.html";
}

function submitForm(event) {
    event.preventDefault();

    const user = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        phoneNumber: document.getElementById("phoneNumber").value,
        message: document.getElementById("message").value
    };

    fetch("/api/messages", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(user)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Unauthorized or error occurred");
            }
            return response.json();
        })
        .then(data => {
            document.getElementById("successMessage").innerText = "Message sent successfully!";
            document.getElementById("contactForm").reset();
            console.log(data);
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Something went wrong!");
        });
}