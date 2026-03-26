function submitForm(event) {
    event.preventDefault(); // stop normal form submission

    const user = {
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        phoneNumber: document.getElementById("phoneNumber").value,
        message: document.getElementById("message").value
    };

    fetch("/saveUser", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    })
        .then(response => response.json())
        .then(data => {
            alert("Message sent successfully!");
            console.log(data);
        })
        .catch(error => {
            console.error("Error:", error);
        });
}