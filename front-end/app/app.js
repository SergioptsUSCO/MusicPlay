import { apiAssetUrl, apiFetch } from "./api.js";

const recentlyPlayedList = document.getElementById("recently-played-list");

if (recentlyPlayedList) {
    loadRecentlyPlayed();
}

async function loadRecentlyPlayed() {
    try {
        const response = await apiFetch("/api/recientementeReproducidas");

        if (!response.ok) {
            throw new Error("No se pudo cargar lo reproducido recientemente.");
        }

        const songs = deduplicateSongs(await response.json());

        if (!songs.length) {
            recentlyPlayedList.innerHTML = `
                <div class="col-12">
                    <div class="playlist-item p-3">Todavia no hay reproducciones recientes.</div>
                </div>
            `;
            return;
        }

        recentlyPlayedList.innerHTML = songs.map((song) => `
            <div class="col-12">
                <div class="playlist-item p-3">
                    <div class="row g-3 align-items-center">
                        <div class="col-auto">
                            <img src="${apiAssetUrl(song.song_portada_ruta)}" alt="Cancion" class="img-thumbnail" style="width: 80px; height: 80px; object-fit: cover;">
                        </div>
                        <div class="col">
                            <span class="nombre">${escapeHtml(song.song_nombre || "Sin nombre")}</span>
                            <span class="artista">${escapeHtml(song.song_artista || "Artista desconocido")}</span>
                        </div>
                    </div>
                </div>
            </div>
        `).join("");
    } catch (error) {
        recentlyPlayedList.innerHTML = `
            <div class="col-12">
                <div class="playlist-item p-3">${escapeHtml(error.message)}</div>
            </div>
        `;
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

function deduplicateSongs(songs) {
    const seen = new Set();

    return songs.filter((song) => {
        const key = normalizeSongKey(song);

        if (seen.has(key)) {
            return false;
        }

        seen.add(key);
        return true;
    });
}

function normalizeSongKey(song) {
    return `${song.song_nombre || ""}::${song.song_artista || ""}`.toLowerCase().trim();
}
