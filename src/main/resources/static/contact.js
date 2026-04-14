const token = localStorage.getItem("token");

if (!token) {
    alert("Please login first");
    window.location.href = "login.html";
}

// Handle showing/hiding fields
function handleTypeChange() {
    const type = document.getElementById("type").value;

    const locationField = document.getElementById("location");
    const dateField = document.getElementById("preferredDateTime");

    if (type === "VISITATION_REQUEST") {
        locationField.style.display = "block";
        dateField.style.display = "block";
    } else {
        locationField.style.display = "none";
        dateField.style.display = "none";

        locationField.value = "";
        dateField.value = "";
    }
}

function submitForm(event) {
    event.preventDefault();

    const type = document.getElementById("type").value;
    const location = document.getElementById("location").value;
    const preferredDateTime = document.getElementById("preferredDateTime").value;

    //Frontend validation
    if (!type) {
        alert("Please select a request type");
        return;
    }

    if (type === "VISITATION_REQUEST") {
        if (!location) {
            alert("Location is required for visitation");
            return;
        }
        if (!preferredDateTime) {
            alert("Date and time is required for visitation");
            return;
        }
    }

    const user = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        message: document.getElementById("message").value,
        type: type,
        location: location || null,
        preferredDateTime: preferredDateTime || null
    };

    fetch("http://localhost:8080/api/messages", {
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

            // Reset hidden fields
            handleTypeChange();
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Something went wrong!");
        });
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}