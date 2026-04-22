const token = localStorage.getItem("token");

if (!token) {
    alert("Please login first");
    window.location.href = "login.html";
}

// FORM LOGIC
function handleTypeChange() {
    const type = document.getElementById("type").value;

    const locationField = document.getElementById("location");
    const calendarSection = document.getElementById("calendarSection");

    if (type === "VISITATION_REQUEST") {
        locationField.style.display = "block";
        calendarSection.style.display = "block";
    } else {
        if (selectedSlot) {
            selectedSlot.classList.remove("selected");
            selectedSlot = null;
        }
        locationField.style.display = "none";
        calendarSection.style.display = "none";

        locationField.value = "";
        document.getElementById("preferredDateTime").value = "";
    }
}

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
            alert("Location is required");
            return;
        }
        if (!preferredDateTime) {
            alert("Please select a time");
            return;
        }
    }

    const payload = {
        phone: document.getElementById("phone").value,
        message: document.getElementById("message").value,
        type,
        location: location || null,
        preferredDateTime: preferredDateTime || null
    };

    fetch("http://localhost:8080/api/messages", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(payload)
    })
        .then(async res => {
            const text = await res.text();

            if (!res.ok) {
                console.error("Backend error:", text);
                alert(text || "Request failed");
                return;
            }

            document.getElementById("successMessage").innerText = "Message sent successfully!";
            document.getElementById("contactForm").reset();

            if (selectedSlot) {
                selectedSlot.classList.remove("selected");
                selectedSlot = null;
            }

            loadCalendar();
        })
        .catch(err => {
            console.error(err);
            alert("Something went wrong");
        });
}

// CALENDAR LOGIC
async function loadCalendar() {
    const res = await fetch("http://localhost:8080/api/appointments/public");
    const appointments = await res.json();
    renderCalendar(appointments);
}

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

        for (let hour = 8; hour <= 18; hour++) {
            const slotTime = new Date(day);
            slotTime.setHours(hour, 0, 0, 0);

            const slot = document.createElement("div");
            slot.className = "time-slot";
            slot.innerText = `${hour}:00`;

            const isPast = slotTime < now;

            const isBooked = appointments.some(a => {
                const start = new Date(a.startTime);
                return (
                    start.getTime() === slotTime.getTime() &&
                    (a.status === "PENDING" || a.status === "APPROVED")
                );
            });

            if (isPast) {
                slot.classList.add("past");
            } else if (isBooked) {
                slot.classList.add("booked");
            } else {
                slot.onclick = () => selectSlot(slotTime, slot);
            }

            column.appendChild(slot);
        }

        calendar.appendChild(column);
    }
}

let selectedSlot = null;

function formatLocalDateTime(date) {
    const pad = (n) => n.toString().padStart(2, '0');

    return date.getFullYear() + '-' +
        pad(date.getMonth() + 1) + '-' +
        pad(date.getDate()) + 'T' +
        pad(date.getHours()) + ':' +
        pad(date.getMinutes()) + ':' + '00';
}

function selectSlot(dateTime, element) {
    if (selectedSlot) {
        selectedSlot.classList.remove("selected");
    }

    element.classList.add("selected");
    selectedSlot = element;

    const formatted = formatLocalDateTime(dateTime);
    document.getElementById("preferredDateTime").value = formatted;
}

document.addEventListener("DOMContentLoaded", loadCalendar);

// LOGOUT
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}