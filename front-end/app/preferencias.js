import { apiAssetUrl, apiFetch, clearSession, getAuthToken, isGuestSession } from "./api.js";

const artistsGrid = document.getElementById("artists-grid");
const form = document.getElementById("preferences-form");
const saveButton = document.getElementById("save-preferences");
const errorElement = document.getElementById("preferences-error");
const logoutButton = document.getElementById("logout-btn");
let selectedArtistId = null;

if (!getAuthToken() || isGuestSession()) {
    window.location.href = "login.html";
}

logoutButton?.addEventListener("click", () => {
    clearSession();
    window.location.href = "login.html";
});

loadArtists();

form?.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!selectedArtistId) {
        showError("Selecciona un artista para continuar.");
        return;
    }

    saveButton.disabled = true;
    showError("");

    try {
        const response = await apiFetch("/api/preferencias/artista-inicial", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                artista_id: selectedArtistId
            })
        });

        if (!response.ok) {
            const message = await readError(response);
            throw new Error(message || "No se pudo guardar tu preferencia.");
        }

        window.location.href = "home.html";
    } catch (error) {
        showError(error.message);
        saveButton.disabled = false;
    }
});

async function loadArtists() {
    try {
        const [userResponse, artistsResponse] = await Promise.all([
            apiFetch("/api/auth/me"),
            apiFetch("/api/artistas")
        ]);

        if (userResponse.status === 401 || userResponse.status === 403) {
            clearSession();
            window.location.href = "login.html";
            return;
        }

        if (userResponse.ok) {
            const user = await userResponse.json();
            if (user.usuario_preferencias_configuradas) {
                window.location.href = "home.html";
                return;
            }
        }

        if (!artistsResponse.ok) {
            throw new Error("No se pudieron cargar los artistas.");
        }

        const artists = await artistsResponse.json();
        renderArtists(artists);
    } catch (error) {
        artistsGrid.innerHTML = `<div class="loading-state">${escapeHtml(error.message)}</div>`;
    }
}

function renderArtists(artists) {
    if (!artists.length) {
        artistsGrid.innerHTML = `<div class="loading-state">Todavia no hay artistas registrados.</div>`;
        return;
    }

    artistsGrid.innerHTML = artists.map((artist) => `
        <button class="artist-option" type="button" data-artist-id="${artist.artista_id}">
            <img src="${apiAssetUrl(artist.artista_foto_ruta)}" alt="${escapeHtml(artist.artista_nombre || "Artista")}">
            <span>${escapeHtml(artist.artista_nombre || "Artista sin nombre")}</span>
        </button>
    `).join("");

    artistsGrid.querySelectorAll("[data-artist-id]").forEach((button) => {
        button.addEventListener("click", () => {
            selectedArtistId = Number(button.dataset.artistId);
            artistsGrid.querySelectorAll(".artist-option").forEach((item) => item.classList.remove("selected"));
            button.classList.add("selected");
            saveButton.disabled = false;
            showError("");
        });
    });
}

async function readError(response) {
    const text = await response.text();
    try {
        const json = JSON.parse(text);
        return json.error || text;
    } catch {
        return text;
    }
}

function showError(message) {
    if (errorElement) {
        errorElement.textContent = message;
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
