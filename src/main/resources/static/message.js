const token = localStorage.getItem("token");

if (!token) {
    showToast("Please login first", "warning");
    setTimeout(() => window.location.href = "login.html", 1500);
}

// toast
function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// response handler
async function handleResponse(res) {
    if (!res.ok) {
        let msg = "Request failed";
        try {
            const data = await res.json();
            msg = data.message || data.error || msg;
        } catch {
            msg = await res.text();
        }
        throw new Error(msg);
    }
    return res;
}

// form logic
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

// submit form
function submitForm(event) {
    event.preventDefault();

    const type = document.getElementById("type").value;
    const location = document.getElementById("location").value;
    const preferredDateTime = document.getElementById("preferredDateTime").value;

    if (!type) {
        showToast("Select a request type", "warning");
        return;
    }

    if (type === "VISITATION_REQUEST") {
        if (!location) {
            showToast("Location is required", "warning");
            return;
        }
        if (!preferredDateTime) {
            showToast("Select a time", "warning");
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
        .then(handleResponse)
        .then(() => {
            showToast("Message sent successfully", "success");
            document.getElementById("contactForm").reset();

            if (selectedSlot) {
                selectedSlot.classList.remove("selected");
                selectedSlot = null;
            }

            loadCalendar();
        })
        .catch(err => {
            if (err.message.toLowerCase().includes("unauthorized")) {
                showToast("Session expired", "error");
                setTimeout(logout, 1500);
            } else {
                showToast(err.message, "error");
            }
        });
}

// calendar
async function loadCalendar() {
    try {
        const res = await fetch("http://localhost:8080/api/appointments/public");
        const data = await res.json();
        renderCalendar(data);
    } catch (err) {
        showToast("Failed to load calendar", "error");
    }
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

// slot selection
let selectedSlot = null;

function formatLocalDateTime(date) {
    const pad = n => n.toString().padStart(2, '0');

    return date.getFullYear() + '-' +
        pad(date.getMonth() + 1) + '-' +
        pad(date.getDate()) + 'T' +
        pad(date.getHours()) + ':' +
        pad(date.getMinutes()) + ':00';
}

function selectSlot(dateTime, element) {
    if (selectedSlot) {
        selectedSlot.classList.remove("selected");
    }

    element.classList.add("selected");
    selectedSlot = element;

    document.getElementById("preferredDateTime").value =
        formatLocalDateTime(dateTime);
}

// init
document.addEventListener("DOMContentLoaded", loadCalendar);

// nav
function goToDashboard() {
    window.location.href = "user.html";
}

// logout
function logout() {
    localStorage.clear();
    window.location.href = "login.html";
}