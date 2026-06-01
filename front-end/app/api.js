const LOCAL_API_BASE_URL = "http://localhost:8080";
const REMOTE_API_BASE_URL = "https://rq6pq57v-8080.use2.devtunnels.ms";

export const API_BASE_URL = ["localhost", "127.0.0.1"].includes(window.location.hostname)
    ? LOCAL_API_BASE_URL
    : REMOTE_API_BASE_URL;
export const GOOGLE_OAUTH_URL = `${API_BASE_URL}/oauth2/authorization/google`;

export function getAuthToken() {
    return localStorage.getItem("jwtToken");
}

export function isGuestSession() {
    return localStorage.getItem("guestMode") === "true";
}

export function startGuestSession() {
    localStorage.removeItem("jwtToken");
    localStorage.setItem("guestMode", "true");
    window.location.href = "home.html";
}

export function clearSession() {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("guestMode");
}

export function getAuthHeaders(extraHeaders = {}) {
    const token = getAuthToken();
    const headers = { ...extraHeaders };

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

export function apiFetch(path, options = {}) {
    const headers = getAuthHeaders(options.headers || {});

    return fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers
    });
}

export function apiAssetUrl(path) {
    if (!path) {
        return "../assets/logo.svg";
    }

    if (path.startsWith("http")) {
        return path;
    }

    return `${API_BASE_URL}${path}`;
}

export function handleOAuthRedirect() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (!token) {
        return false;
    }

    localStorage.setItem("jwtToken", token);
    localStorage.removeItem("guestMode");
    window.history.replaceState({}, document.title, window.location.pathname);
    window.location.href = "home.html";
    return true;
}

export function getOAuthError() {
    const params = new URLSearchParams(window.location.search);
    const error = params.get("oauthError");

    if (!error) {
        return "";
    }

    window.history.replaceState({}, document.title, window.location.pathname);
    return error;
}

export function startGoogleOAuth() {
    window.location.href = GOOGLE_OAUTH_URL;
}
