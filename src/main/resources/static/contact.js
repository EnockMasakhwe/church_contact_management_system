const token = localStorage.getItem("token");

if (!token) {
    alert("Please login first");
    window.location.href = "login.html";
}

// Track selected slot
let selectedSlotElement = null;

// Handle UI changes
function handleTypeChange() {
    const type = document.getElementById("type").value;

    const locationField = document.getElementById("location");
    const calendarContainer = document.getElementById("calendarContainer");

    if (type === "VISITATION_REQUEST") {
        locationField.style.display = "block";
        calendarContainer.style.display = "block";

        loadCalendar(); // load slots only when needed
    } else {
        locationField.style.display = "none";
        calendarContainer.style.display = "none";

        locationField.value = "";
        document.getElementById("preferredDateTime").value = "";

        if (selectedSlotElement) {
            selectedSlotElement.classList.remove("selected");
            selectedSlotElement = null;
        }
    }
}

// Submit form
function submitForm(event) {
    event.preventDefault();

    const type = document.getElementById("type").value;
    const location = document.getElementById("location").value;
    const preferredDateTime = document.getElementById("preferredDateTime").value;

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
            alert("Please select a time from the calendar");
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
                throw new Error("Error occurred");
            }
            return response.json();
        })
        .then(() => {
            document.getElementById("successMessage").innerText = "Message sent successfully!";
            document.getElementById("contactForm").reset();

            handleTypeChange(); // reset UI
        })
        .catch(error => {
            console.error(error);
            alert("Something went wrong!");
        });
}

// Load calendar data
async function loadCalendar() {
    const res = await fetch("http://localhost:8080/api/appointments");
    const appointments = await res.json();

    renderCalendar(appointments);
}

// Render calendar
function renderCalendar(appointments) {
    const calendar = document.getElementById("calendar");
    calendar.innerHTML = "";

    const now = new Date();

    for (let i = 0; i < 7; i++) {
        const day = new Date();
        day.setDate(now.getDate() + i);

        const column = document.createElement("div");
        column.className = "day-column";

        const header = document.createElement("div");
        header.className = "day-header";
        header.innerText = day.toDateString();

        column.appendChild(header);

        for (let hour = 8; hour <= 17; hour++) {
            const slotTime = new Date(day);
            slotTime.setHours(hour, 0, 0, 0);

            const slot = document.createElement("div");
            slot.className = "time-slot";
            slot.innerText = `${hour}:00`;

            const isBooked = appointments.some(a => {
                const start = new Date(a.startTime);
                return start.getTime() === slotTime.getTime();
            });

            if (isBooked) {
                slot.classList.add("booked");
            } else {
                slot.onclick = () => selectSlot(slotTime, slot);
            }

            column.appendChild(slot);
        }

        calendar.appendChild(column);
    }
}

// Select slot
function selectSlot(dateTime, element) {
    if (selectedSlotElement) {
        selectedSlotElement.classList.remove("selected");
    }

    element.classList.add("selected");
    selectedSlotElement = element;

    document.getElementById("preferredDateTime").value = dateTime.toISOString();
}

// Logout
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}