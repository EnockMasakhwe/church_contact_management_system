// Show toast
function showToast(message, type = "error") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;

    container.appendChild(toast);

    setTimeout(() => toast.remove(), 4000);
}

// Handle response
async function handleResponse(res) {
    let data;
    const contentType = res.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        try {
            data = await res.json();
        } catch {
            throw new Error("Invalid JSON response from server");
        }
    } else {
        data = await res.text();
    }

    if (!res.ok) {
        const message =
            typeof data === "object" && data !== null
                ? data.message || data.error || "Request failed"
                : data || `Request failed (${res.status})`;

        throw new Error(message);
    }

    return data;
}

// Main fetch wrapper
async function apiFetch(url, options = {}) {
    const token = localStorage.getItem("token");
    const requestUrl = /^https?:\/\//i.test(url)
        ? url
        : `${url.startsWith("/") ? "" : "/"}${url}`;

    const config = {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: "Bearer " + token } : {}),
            ...(options.headers || {})
        }
    };

    try {
        const res = await fetch(requestUrl, config);
        if (res.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
        }
        return await handleResponse(res);
    } catch (err) {
        showToast(err.message);
        throw err;
    }
}