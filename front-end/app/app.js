import { apiAssetUrl, apiFetch } from "./api.js";

const trendingArtistsList = document.getElementById("trending-artists-list");
const featuredAlbumsList = document.getElementById("featured-albums-list");
const recentlyPlayedList = document.getElementById("recently-played-list");

if (trendingArtistsList) {
    loadTrendingArtists();
}

if (featuredAlbumsList) {
    loadFeaturedAlbums();
}

if (recentlyPlayedList) {
    loadRecentlyPlayed();
}

async function loadTrendingArtists() {
    try {
        const response = await apiFetch("/api/tendencias/artistas");

        if (!response.ok) {
            throw new Error("No se pudieron cargar los artistas en tendencia.");
        }

        const artists = await response.json();
        renderCardItems({
            container: trendingArtistsList,
            items: artists,
            emptyMessage: "Todavia no hay artistas en tendencia.",
            getImage: (artist) => artist.artista_foto_ruta,
            getTitle: (artist) => artist.artista_nombre || "Artista sin nombre",
            alt: "Artista"
        });
    } catch (error) {
        renderCardError(trendingArtistsList, error.message);
    }
}

async function loadFeaturedAlbums() {
    try {
        const response = await apiFetch("/api/destacados/albumes");

        if (!response.ok) {
            throw new Error("No se pudieron cargar los albumes destacados.");
        }

        const albums = await response.json();
        renderCardItems({
            container: featuredAlbumsList,
            items: albums,
            emptyMessage: "Todavia no hay albumes destacados.",
            getImage: (album) => album.album_portada_ruta,
            getTitle: (album) => album.album_nombre || "Album sin nombre",
            alt: "Album"
        });
    } catch (error) {
        renderCardError(featuredAlbumsList, error.message);
    }
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

function renderCardItems({ container, items, emptyMessage, getImage, getTitle, alt }) {
    if (!items.length) {
        container.innerHTML = `
            <div class="col-12">
                <div class="playlist-item p-3">${emptyMessage}</div>
            </div>
        `;
        return;
    }

    container.innerHTML = items.map((item) => `
        <div class="col-12 col-sm-6 col-lg-2">
            <div class="card-item h-100">
                <img src="${apiAssetUrl(getImage(item))}" alt="${alt}">
                <span class="d-block mt-2">${escapeHtml(getTitle(item))}</span>
            </div>
        </div>
    `).join("");
}

function renderCardError(container, message) {
    container.innerHTML = `
        <div class="col-12">
            <div class="playlist-item p-3">${escapeHtml(message)}</div>
        </div>
    `;
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
