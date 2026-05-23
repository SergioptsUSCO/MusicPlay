export const API_BASE_URL = "https://rq6pq57v-8080.use2.devtunnels.ms/";

export function getAuthToken() {
    return localStorage.getItem("jwtToken");
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
