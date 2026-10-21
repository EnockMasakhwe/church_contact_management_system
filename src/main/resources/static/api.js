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
    const contentType = res.headers.get("content-type") || "";
    const isJson = contentType.toLowerCase().includes("json");
    let body;

    try {
        body = await res.text();
    } catch {
        throw new ApiError("Could not read the server response. Please try again.", res.status);
    }

    let data = null;
    if (body) {
        if (isJson) {
            try {
                data = JSON.parse(body);
            } catch {
                if (res.ok) {
                    throw new ApiError("Server returned an invalid response.", res.status);
                }
            }
        } else if (contentType.toLowerCase().includes("text/plain")) {
            data = body;
        }
    }

    if (!res.ok) {
        const responseMessage = typeof data === "object" && data !== null
            ? data.message || data.error
            : typeof data === "string" ? data.trim() : "";
        const message = typeof responseMessage === "string" && responseMessage.trim()
            ? responseMessage.trim()
            : `Request failed (${res.status}).`;

        throw new ApiError(message, res.status);
    }

    return data;
}

class ApiError extends Error {
    constructor(message, status = null) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
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

    let res;
    try {
        res = await fetch(requestUrl, config);
    } catch (err) {
        if (err && err.name === "AbortError") {
            throw new ApiError("Request was cancelled.");
        }
        throw new ApiError("Unable to reach the server. Check your connection and try again.");
    }

    if (res.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
    }

    return handleResponse(res);
}