const BASE_URL = "https://contact_form.up.railway.app";

// show toast
function showToast(message, type = "error") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;

    container.appendChild(toast);

    setTimeout(() => toast.remove(), 4000);
}

// handle response
async function handleResponse(res) {
    let data;

    try {
        data = await res.json();
    } catch {
        throw new Error("Unexpected server response");
    }

    if (!res.ok) {
        throw new Error(data.message || "Request failed");
    }

    return data;
}

// main fetch wrapper
async function apiFetch(url, options = {}) {
    const token = localStorage.getItem("token");

    const config = {
        headers: {
            "Content-Type": "application/json",
            ...(token && { Authorization: "Bearer " + token })
        },
        ...options
    };

    try {
        const res = await fetch(url, config);
        const data = await handleResponse(res);
        return data;
    } catch (err) {
        showToast(err.message);
        throw err;
    }
}