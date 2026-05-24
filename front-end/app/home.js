import { apiAssetUrl, apiFetch, clearSession, getAuthToken, handleOAuthRedirect, isGuestSession }
from "./api.js";

import { loadHomeView }
from "./views/homeView.js";

import { loadPlaylistView }
from "./views/playlistView.js";

import { loadAlbumView }
from "./views/albumView.js";

handleOAuthRedirect();

if (!getAuthToken() && !isGuestSession()) {
    window.location.href = "login.html";
}

const logoutButton = document.getElementById("logout-btn");
const profileInitial = document.getElementById("profile-initial");
const adminLink = document.getElementById("admin-link");
const sidebar = document.querySelector(".sidebar");
const searchViewButton = document.getElementById("search-view-btn");
let searchArtists = [];

setPlayerSongInfoVisible(false);

if (logoutButton) {
    logoutButton.addEventListener("click", () => {
        clearSession();
        window.location.href = "login.html";
    });
}

if (searchViewButton) {
    searchViewButton.addEventListener("click", loadSearchView);
}

async function loadCurrentUser() {
    if (isGuestSession()) {
        if (profileInitial) {
            profileInitial.textContent = "I";
            profileInitial.title = "Invitado";
        }

        if (logoutButton) {
            logoutButton.textContent = "Iniciar Sesion";
        }

        if (adminLink) {
            adminLink.style.display = "none";
        }

        renderGuestLibrary();
        return;
    }

    try {
        const response = await apiFetch("/api/auth/me");

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem("jwtToken");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            return;
        }

        const user = await response.json();
        const name = user.usuario_nombre || user.usuario_correo || "";
        const initial = name.trim().charAt(0).toUpperCase();

        if (profileInitial && initial) {
            profileInitial.textContent = initial;
            profileInitial.title = name;
        }

        if (adminLink && user.usuario_rol !== 1) {
            adminLink.style.display = "none";
        }
    } catch (error) {
        console.error("Error al cargar el usuario actual:", error);
    }
}

function renderGuestLibrary() {
    if (!sidebar) {
        return;
    }

    sidebar.innerHTML = `
        <div class="library-header">
            <h5>Tu biblioteca</h5>
        </div>

        <div class="guest-library-card">
            <h6>Inicia sesion para guardar tu musica</h6>
            <p>Crea playlists, guarda favoritos y conserva tu biblioteca en MusicPlay.</p>
            <button type="button" id="guest-library-login" class="btn premium-btn">Iniciar Sesion</button>
        </div>
    `;

    document.getElementById("guest-library-login")?.addEventListener("click", () => {
        clearSession();
        window.location.href = "login.html";
    });
}

window.loadHomeView = function(){

    loadHomeView();
}

window.loadPlaylistView = function(id){

    loadPlaylistView(id);
}

window.loadAlbumView = function(){

    loadAlbumView();
}

async function loadSearchView() {
    const content = document.getElementById("content-view");

    content.innerHTML = `
        <section class="search-view">
            <div class="section-header">
                <h2>Busqueda</h2>
                <span id="search-summary">Canciones, albumes y artistas</span>
            </div>

            <div class="search-panel">
                <label for="catalog-search">Buscar en MusicPlay</label>
                <div class="search-field">
                    <input id="catalog-search" class="form-control" type="search" placeholder="Cancion, album o artista..." autocomplete="off">
                    <button id="catalog-search-btn" class="btn premium-btn" type="button">Buscar</button>
                </div>
            </div>

            <div id="search-results" class="search-results">
                <div class="song-list-state">Cargando catalogo...</div>
            </div>
        </section>
    `;

    const input = document.getElementById("catalog-search");
    const button = document.getElementById("catalog-search-btn");
    let timeoutId;

    const runSearch = () => {
        window.clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => searchCatalog(input.value), 250);
    };

    input.addEventListener("input", runSearch);
    button.addEventListener("click", () => searchCatalog(input.value));
    input.focus();
    searchCatalog("");
}

async function searchCatalog(query) {
    const results = document.getElementById("search-results");
    const summary = document.getElementById("search-summary");
    const term = query.trim();

    if (!term) {
        summary.textContent = "Todo el catalogo";
    }

    results.innerHTML = `<div class="song-list-state">${term ? "Buscando..." : "Cargando catalogo..."}</div>`;

    try {
        const data = await fetchSearchResults(term);
        const canciones = data.canciones || [];
        const albumes = data.albumes || [];
        const artistas = data.artistas || [];
        const total = canciones.length + albumes.length + artistas.length;

        summary.textContent = term ? `${total} resultados` : `${total} elementos`;

        if (!total) {
            results.innerHTML = `<div class="song-list-state">No encontramos resultados para "${escapeHtml(term)}".</div>`;
            return;
        }

        results.innerHTML = `
            ${renderSearchGroup("Canciones", canciones, renderSongResult)}
            ${renderSearchGroup("Albumes", albumes, renderAlbumResult)}
            ${renderSearchGroup("Artistas", artistas, renderArtistResult)}
        `;

        results.querySelectorAll("[data-search-song]").forEach((button) => {
            button.addEventListener("click", () => playSearchSong(JSON.parse(button.dataset.searchSong)));
        });
    } catch (error) {
        results.innerHTML = `<div class="song-list-state">${error.message}</div>`;
        summary.textContent = "Error";
    }
}

async function fetchSearchResults(term) {
    const response = await apiFetch(`/api/busqueda?q=${encodeURIComponent(term)}`);

    if (response.ok) {
        const [data, artistsResponse] = await Promise.all([
            response.json(),
            apiFetch("/api/artistas")
        ]);

        searchArtists = artistsResponse.ok ? await artistsResponse.json() : data.artistas || [];
        return data;
    }

    return searchWithExistingCollections(term);
}

async function searchWithExistingCollections(term) {
    const [songsResponse, albumsResponse, artistsResponse] = await Promise.all([
        apiFetch("/api/canciones"),
        apiFetch("/api/albumes"),
        apiFetch("/api/artistas")
    ]);

    if (!songsResponse.ok || !albumsResponse.ok || !artistsResponse.ok) {
        throw new Error("No se pudieron cargar las colecciones.");
    }

    const [allSongs, allAlbums, allArtists] = await Promise.all([
        songsResponse.json(),
        albumsResponse.json(),
        artistsResponse.json()
    ]);

    searchArtists = allArtists;

    return {
        canciones: filterAndRank(allSongs, term, (song) => song.song_nombre),
        albumes: filterAndRank(allAlbums, term, (album) => album.album_nombre),
        artistas: filterAndRank(allArtists, term, (artist) => artist.artista_nombre)
    };
}

function filterAndRank(items, term, getName) {
    const query = normalizeSearchText(term);

    if (!query) {
        return items;
    }

    return items
        .filter((item) => normalizeSearchText(getName(item)).includes(query))
        .sort((left, right) => {
            const leftName = normalizeSearchText(getName(left));
            const rightName = normalizeSearchText(getName(right));
            return scoreSearchResult(leftName, query) - scoreSearchResult(rightName, query)
                || leftName.localeCompare(rightName);
        });
}

function scoreSearchResult(value, query) {
    if (value === query) {
        return 0;
    }

    if (value.startsWith(query)) {
        return 1;
    }

    return 2;
}

function normalizeSearchText(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

function renderSearchGroup(title, items, renderItem) {
    if (!items.length) {
        return "";
    }

    return `
        <section class="search-result-group">
            <h3>${title}</h3>
            <div class="search-result-grid">
                ${items.map(renderItem).join("")}
            </div>
        </section>
    `;
}

function renderSongResult(song) {
    return `
        <button class="search-result-item" type="button" data-search-song='${escapeAttribute(JSON.stringify(song))}' ${song.song_archivo_ruta ? "" : "disabled"}>
            <img src="${apiAssetUrl(song.song_portada_ruta)}" alt="Portada">
            <span>
                <strong>${escapeHtml(song.song_nombre || "Sin nombre")}</strong>
                <small>${escapeHtml(getSearchArtistName(song.song_artista))}</small>
            </span>
        </button>
    `;
}

function renderAlbumResult(album) {
    return `
        <div class="search-result-item">
            <img src="${apiAssetUrl(album.album_portada_ruta)}" alt="Portada">
            <span>
                <strong>${escapeHtml(album.album_nombre || "Sin nombre")}</strong>
                <small>Album</small>
            </span>
        </div>
    `;
}

function renderArtistResult(artist) {
    return `
        <div class="search-result-item">
            <img src="${apiAssetUrl(artist.artista_foto_ruta)}" alt="Artista">
            <span>
                <strong>${escapeHtml(artist.artista_nombre || "Sin nombre")}</strong>
                <small>Artista</small>
            </span>
        </div>
    `;
}

function playSearchSong(song) {
    if (!song.song_archivo_ruta) {
        return;
    }

    const audio = document.getElementById("audio-player");
    audio.src = apiAssetUrl(song.song_archivo_ruta);
    audio.play();

    document.getElementById("player-cover").src = apiAssetUrl(song.song_portada_ruta);
    document.getElementById("player-title").textContent = song.song_nombre || "Sin nombre";
    document.getElementById("player-artist").textContent = getSearchArtistName(song.song_artista);
    setPlayerSongInfoVisible(true);
}

function getSearchArtistName(artistId) {
    return searchArtists.find((artist) => artist.artista_id === artistId)?.artista_nombre || "Artista desconocido";
}

function setPlayerSongInfoVisible(isVisible) {
    const songInfo = document.querySelector(".song-info");

    if (songInfo) {
        songInfo.style.visibility = isVisible ? "visible" : "hidden";
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

function escapeAttribute(value) {
    return escapeHtml(value);
}

window.toggleSidebar = function(){
    const sidebar = document.querySelector('.sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    const body = document.body;
    if (sidebar && backdrop) {
        const isOpen = !sidebar.classList.contains('mobile-open');
        sidebar.classList.toggle('mobile-open');
        backdrop.classList.toggle('visible', isOpen);
        body.classList.toggle('sidebar-open', isOpen);
    }
}

window.closeSidebar = function(){
    const sidebar = document.querySelector('.sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    const body = document.body;
    if (sidebar && backdrop) {
        sidebar.classList.remove('mobile-open');
        backdrop.classList.remove('visible');
        body.classList.remove('sidebar-open');
    }
}

loadCurrentUser();
loadHomeView();
