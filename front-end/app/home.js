import { apiAssetUrl, apiFetch, clearSession, getAuthToken, handleOAuthRedirect, isGuestSession }
from "./api.js";

import { loadHomeView, playSongQueue, playSongShuffleQueue }
from "./views/homeView.js";

import { loadPlaylistView }
from "./views/playlistView.js";

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
let searchSongs = [];
let searchAlbums = [];
let searchTerm = "";
let searchRequestId = 0;
let savedAlbums = [];
let likedSongIds = [];
let userPlaylists = [];
let currentCollectionStarted = false;

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

window.addEventListener("musicplay:likes-updated", () => {
    if (!isGuestSession()) {
        renderUserLibrary();
    }
});

window.addEventListener("musicplay:add-song-to-playlist", (event) => {
    openAddToPlaylistModal(event.detail.songId);
});

window.addEventListener("musicplay:playback-song-changed", (event) => {
    markActiveCollectionSongById(event.detail.songId);
});

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

        await renderUserLibrary();
    } catch (error) {
        console.error("Error al cargar el usuario actual:", error);
    }
}

async function renderUserLibrary() {
    if (!sidebar) {
        return;
    }

    try {
        const [albumsResponse, artistsResponse, likesResponse, playlistsResponse] = await Promise.all([
            apiFetch("/api/biblioteca/albumes"),
            apiFetch("/api/artistas"),
            apiFetch("/api/likes"),
            apiFetch("/api/playlists")
        ]);
        savedAlbums = albumsResponse.ok ? await albumsResponse.json() : [];
        likedSongIds = likesResponse.ok ? await likesResponse.json() : [];
        userPlaylists = playlistsResponse.ok ? await playlistsResponse.json() : [];
        if (artistsResponse.ok) {
            searchArtists = await artistsResponse.json();
        }

        sidebar.innerHTML = `
            <div class="library-header">
                <h5>Tu biblioteca</h5>
                <button class="btn create-btn" id="create-playlist-btn" type="button">+ Crear</button>
            </div>

            <button class="playlist-item liked-songs-item" type="button" id="liked-songs-library">
                <div class="liked-cover">&hearts;</div>
                <div>
                    <h6>Canciones que te gustan</h6>
                    <p>Playlist • ${likedSongIds.length} canciones</p>
                </div>
            </button>

            ${userPlaylists.map(renderLibraryPlaylist).join("")}

            ${savedAlbums.length ? savedAlbums.map(renderLibraryAlbum).join("") : `
                <div class="guest-library-card">
                    <h6>Guarda tus albumes favoritos</h6>
                    <p>Usa el boton + en un album para tenerlo aqui.</p>
                </div>
            `}
        `;

        document.getElementById("create-playlist-btn")?.addEventListener("click", loadCreatePlaylistView);
        document.getElementById("liked-songs-library")?.addEventListener("click", loadLikedSongsView);
        sidebar.querySelectorAll("[data-library-playlist]").forEach((button) => {
            button.addEventListener("click", () => loadPlaylistDetailView(Number(button.dataset.libraryPlaylist)));
        });
        sidebar.querySelectorAll("[data-playlist-menu-toggle]").forEach((button) => {
            button.addEventListener("click", (event) => {
                event.stopPropagation();
                const row = button.closest(".library-playlist-row");
                sidebar.querySelectorAll(".library-playlist-row.menu-open").forEach((openRow) => {
                    if (openRow !== row) {
                        openRow.classList.remove("menu-open");
                    }
                });
                row.classList.toggle("menu-open");
            });
        });
        sidebar.querySelectorAll("[data-edit-playlist]").forEach((button) => {
            button.addEventListener("click", (event) => {
                event.stopPropagation();
                const playlistId = Number(button.dataset.editPlaylist);
                const playlist = userPlaylists.find((item) => item.playlist_id === playlistId);
                loadCreatePlaylistView(playlist);
            });
        });
        sidebar.querySelectorAll("[data-delete-playlist]").forEach((button) => {
            button.addEventListener("click", async (event) => {
                event.stopPropagation();
                await deletePlaylist(Number(button.dataset.deletePlaylist));
            });
        });
        sidebar.querySelectorAll("[data-library-album]").forEach((button) => {
            button.addEventListener("click", () => loadAlbumDetailView(Number(button.dataset.libraryAlbum)));
        });
    } catch (error) {
        console.error("No se pudo cargar la biblioteca:", error);
    }
}

function renderLibraryPlaylist(playlist) {
    return `
        <div class="library-playlist-row">
            <button class="playlist-item library-playlist-item" type="button" data-library-playlist="${playlist.playlist_id}">
                <img src="${apiAssetUrl(playlist.playlist_portada_ruta)}" alt="Portada">
                <div>
                    <h6>${escapeHtml(playlist.playlist_nombre || "Playlist sin nombre")}</h6>
                    <p>Playlist</p>
                </div>
            </button>
            <button class="playlist-more-btn" type="button" data-playlist-menu-toggle="${playlist.playlist_id}" aria-label="Opciones de playlist">...</button>
            <div class="playlist-options-menu">
                <button type="button" data-edit-playlist="${playlist.playlist_id}">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20h4.6L19.1 9.5a2.1 2.1 0 0 0 0-3l-1.6-1.6a2.1 2.1 0 0 0-3 0L4 15.4V20Zm2-2v-1.8L16 6.2l1.8 1.8L7.8 18H6Z"/></svg>
                    Editar playlist
                </button>
                <button class="danger" type="button" data-delete-playlist="${playlist.playlist_id}">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 21c-1.1 0-2-.9-2-2V7h14v12c0 1.1-.9 2-2 2H7ZM9 4h6l1 1h4v2H4V5h4l1-1Zm0 6v8h2v-8H9Zm4 0v8h2v-8h-2Z"/></svg>
                    Borrar playlist
                </button>
            </div>
        </div>
    `;
}

function loadCreatePlaylistView(playlist = null) {
    const isEditing = Boolean(playlist?.playlist_id);
    const content = document.getElementById("content-view");
    const title = isEditing ? "Editar playlist" : "Crear playlist";
    const description = isEditing
        ? "Actualiza el nombre o cambia la portada de tu playlist."
        : "Ponle nombre y portada a una nueva playlist.";
    const coverLabel = isEditing ? "Cambiar foto" : "Agregar foto";
    const submitLabel = isEditing ? "Guardar cambios" : "Crear playlist";
    const coverSrc = apiAssetUrl(playlist?.playlist_portada_ruta);
    const nameValue = escapeAttribute(playlist?.playlist_nombre || "");

    content.innerHTML = `
        <section class="create-playlist-view ${isEditing ? "edit-playlist-view" : "new-playlist-view"}">
            <div class="section-header">
                <div>
                    <p>${isEditing ? "Playlist existente" : "Nueva playlist"}</p>
                    <h2>${title}</h2>
                    <span>${description}</span>
                </div>
            </div>

            <form id="create-playlist-form" class="create-playlist-form">
                <label class="playlist-cover-picker" for="playlist-cover-input">
                    <img id="playlist-cover-preview" src="${coverSrc}" alt="Portada">
                    <span>${coverLabel}</span>
                </label>
                <input id="playlist-cover-input" type="file" accept="image/*">

                <div class="input-group">
                    <label for="playlist-name-input">Nombre</label>
                    <input id="playlist-name-input" type="text" placeholder="Nombre de la playlist" value="${nameValue}" required>
                </div>

                <div class="playlist-form-actions">
                    <button class="btn premium-btn" type="submit">${submitLabel}</button>
                    ${isEditing ? `<button class="btn secondary-btn" id="cancel-playlist-edit" type="button">Cancelar</button>` : ""}
                </div>
            </form>
        </section>
    `;

    const input = document.getElementById("playlist-cover-input");
    input.addEventListener("change", () => {
        const file = input.files?.[0];
        if (file) {
            document.getElementById("playlist-cover-preview").src = URL.createObjectURL(file);
        }
    });

    document.getElementById("cancel-playlist-edit")?.addEventListener("click", () => {
        loadPlaylistDetailView(playlist.playlist_id);
    });

    document.getElementById("create-playlist-form").addEventListener("submit", (event) => savePlaylist(event, playlist?.playlist_id));
}

async function savePlaylist(event, playlistId = null) {
    event.preventDefault();
    const formData = new FormData();
    const name = document.getElementById("playlist-name-input").value.trim();
    const cover = document.getElementById("playlist-cover-input").files?.[0];

    formData.append("playlist_nombre", name);
    if (cover) {
        formData.append("playlist_portada", cover);
    }

    try {
        const response = await apiFetch(playlistId ? `/api/playlists/${playlistId}` : "/api/playlists", {
            method: playlistId ? "PUT" : "POST",
            body: formData
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo guardar la playlist.");
        }

        const playlist = await response.json();
        await renderUserLibrary();
        loadPlaylistDetailView(playlist.playlist_id);
    } catch (error) {
        alert(error.message);
    }
}

async function deletePlaylist(playlistId) {
    const confirmed = await openConfirmDialog({
        title: "Borrar playlist",
        message: "Seguro que quieres borrar esta playlist? Esta accion no se puede deshacer.",
        confirmText: "Borrar playlist",
        danger: true
    });

    if (!confirmed) {
        return;
    }

    try {
        const response = await apiFetch(`/api/playlists/${playlistId}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo borrar la playlist.");
        }

        await renderUserLibrary();
        loadHomeView();
    } catch (error) {
        alert(error.message);
    }
}

async function loadLikedSongsView() {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando canciones que te gustan...</div>`;

    try {
        const [songsResponse, artistsResponse] = await Promise.all([
            apiFetch("/api/likes/canciones"),
            apiFetch("/api/artistas")
        ]);

        if (!songsResponse.ok) {
            throw new Error("No se pudieron cargar tus canciones favoritas.");
        }

        const songs = await songsResponse.json();
        const artists = artistsResponse.ok ? await artistsResponse.json() : searchArtists;
        searchArtists = artists;

        renderCollectionView({
            typeLabel: "Playlist",
            title: "Canciones que te gustan",
            subtitle: `${songs.length} canciones`,
            likedCover: true,
            songs,
            artists,
            emptyMessage: "Todavia no has dado like a ninguna cancion."
        });
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
}

async function loadPlaylistDetailView(playlistId) {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando playlist...</div>`;

    try {
        const [playlistResponse, songsResponse, artistsResponse] = await Promise.all([
            apiFetch(`/api/playlists/${playlistId}`),
            apiFetch(`/api/playlists/${playlistId}/canciones`),
            apiFetch("/api/artistas")
        ]);

        if (!playlistResponse.ok || !songsResponse.ok) {
            throw new Error("No se pudo cargar la playlist.");
        }

        const playlist = await playlistResponse.json();
        const songs = await songsResponse.json();
        const artists = artistsResponse.ok ? await artistsResponse.json() : searchArtists;
        searchArtists = artists;

        renderCollectionView({
            typeLabel: "Playlist",
            title: playlist.playlist_nombre || "Playlist sin nombre",
            subtitle: `${songs.length} canciones`,
            image: playlist.playlist_portada_ruta,
            songs,
            artists,
            playlistId,
            emptyMessage: "Esta playlist todavia no tiene canciones."
        });
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
}

function renderLibraryAlbum(album) {
    return `
        <button class="playlist-item library-album-item" type="button" data-library-album="${album.album_id}">
            <img src="${apiAssetUrl(album.album_portada_ruta)}" alt="Portada">
            <div>
                <h6>${escapeHtml(album.album_nombre || "Album sin nombre")}</h6>
                <p>Album • ${escapeHtml(getSearchArtistName(album.artista_album))}</p>
            </div>
        </button>
    `;
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

    loadFirstAlbumView();
}

async function loadFirstAlbumView() {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando album...</div>`;

    try {
        const response = await apiFetch("/api/albumes");

        if (!response.ok) {
            throw new Error("No se pudieron cargar los albumes.");
        }

        const albums = await response.json();

        if (!albums.length) {
            content.innerHTML = `<div class="song-list-state">Todavia no hay albumes cargados.</div>`;
            return;
        }

        loadAlbumDetailView(albums[0].album_id);
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
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
                <div class="song-list-state">Escribe una cancion, album o artista para buscar.</div>
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
}

async function searchCatalog(query) {
    const results = document.getElementById("search-results");
    const summary = document.getElementById("search-summary");
    const term = query.trim();
    const requestId = ++searchRequestId;

    if (!term) {
        summary.textContent = "Canciones, albumes y artistas";
        results.innerHTML = `<div class="song-list-state">Escribe una cancion, album o artista para buscar.</div>`;
        searchSongs = [];
        searchAlbums = [];
        searchTerm = "";
        return;
    }

    results.innerHTML = `<div class="song-list-state">Buscando...</div>`;

    try {
        const data = await fetchSearchResults(term);

        if (requestId !== searchRequestId) {
            return;
        }

        const canciones = data.canciones || [];
        const albumes = data.albumes || [];
        const artistas = data.artistas || [];
        const total = canciones.length + albumes.length + artistas.length;

        summary.textContent = term ? `${total} resultados` : `${total} elementos`;

        if (!total) {
            results.innerHTML = `<div class="song-list-state">No encontramos resultados para "${escapeHtml(term)}".</div>`;
            return;
        }

        searchSongs = canciones;
        searchAlbums = albumes;
        searchTerm = term;

        results.innerHTML = `
            ${renderSearchGroup("Canciones", canciones.slice(0, 10), renderSongResult, canciones.length, "canciones")}
            ${renderSearchGroup("Albumes", albumes.slice(0, 4), renderAlbumResult, albumes.length, "albumes")}
            ${renderSearchGroup("Artistas", artistas.slice(0, 4), renderArtistResult, artistas.length, "artistas")}
        `;

        results.querySelectorAll("[data-search-song]").forEach((button) => {
            button.addEventListener("click", () => playSearchSong(JSON.parse(button.dataset.searchSong)));
        });

        results.querySelectorAll("[data-search-album]").forEach((button) => {
            button.addEventListener("click", () => loadAlbumDetailView(Number(button.dataset.searchAlbum)));
        });

        results.querySelectorAll("[data-search-artist]").forEach((button) => {
            button.addEventListener("click", () => loadArtistDetailView(Number(button.dataset.searchArtist)));
        });

        results.querySelectorAll("[data-show-all]").forEach((button) => {
            button.addEventListener("click", () => loadSearchAllView(button.dataset.showAll));
        });
    } catch (error) {
        if (requestId !== searchRequestId) {
            return;
        }

        results.innerHTML = `<div class="song-list-state">${error.message}</div>`;
        summary.textContent = "Error";
    }
}

async function fetchSearchResults(term) {
    const response = await apiFetch(`/api/busqueda?q=${encodeURIComponent(term)}`);

    if (response.ok) {
        const data = await response.json();
        searchArtists = mergeArtists(data.artistasRelacionados || data.artistas || []);
        return data;
    }

    return searchWithExistingCollections(term);
}

function mergeArtists(artists) {
    const artistMap = new Map(searchArtists.map((artist) => [artist.artista_id, artist]));

    artists.forEach((artist) => {
        artistMap.set(artist.artista_id, artist);
    });

    return Array.from(artistMap.values());
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

function renderSearchGroup(title, items, renderItem, total, type) {
    if (!items.length) {
        return "";
    }

    return `
        <section class="search-result-group">
            <h3>${title}</h3>
            <div class="search-result-grid">
                ${items.map(renderItem).join("")}
            </div>
            ${total > items.length ? `<button class="show-all-btn" type="button" data-show-all="${type}">Mostrar todo</button>` : ""}
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
        <button class="search-result-item" type="button" data-search-album="${album.album_id}">
            <img src="${apiAssetUrl(album.album_portada_ruta)}" alt="Portada">
            <span>
                <strong>${escapeHtml(album.album_nombre || "Sin nombre")}</strong>
                <small>${escapeHtml(getSearchArtistName(album.artista_album))}</small>
            </span>
        </button>
    `;
}

function renderArtistResult(artist) {
    return `
        <button class="search-result-item" type="button" data-search-artist="${artist.artista_id}">
            <img src="${apiAssetUrl(artist.artista_foto_ruta)}" alt="Artista">
            <span>
                <strong>${escapeHtml(artist.artista_nombre || "Sin nombre")}</strong>
                <small>Artista</small>
            </span>
        </button>
    `;
}

function loadSearchAllView(type) {
    const config = {
        canciones: {
            title: "Todas las canciones",
            items: searchSongs,
            render: renderSongResult
        },
        albumes: {
            title: "Todos los albumes",
            items: searchAlbums,
            render: renderAlbumResult
        },
        artistas: {
            title: "Todos los artistas",
            items: searchArtists.filter((artist) => {
                if (!searchTerm) {
                    return true;
                }

                return normalizeSearchText(artist.artista_nombre).includes(normalizeSearchText(searchTerm));
            }),
            render: renderArtistResult
        }
    }[type];

    if (!config) {
        return;
    }

    const content = document.getElementById("content-view");
    content.innerHTML = `
        <section class="search-view">
            <div class="section-header">
                <h2>${config.title}</h2>
                <button class="btn premium-btn" type="button" id="back-to-search">Volver</button>
            </div>

            <div class="search-result-grid search-all-grid">
                ${config.items.map(config.render).join("") || `<div class="song-list-state">No hay resultados.</div>`}
            </div>
        </section>
    `;

    document.getElementById("back-to-search")?.addEventListener("click", loadSearchView);
    content.querySelectorAll("[data-search-song]").forEach((button) => {
        button.addEventListener("click", () => playSearchSong(JSON.parse(button.dataset.searchSong)));
    });
    content.querySelectorAll("[data-search-album]").forEach((button) => {
        button.addEventListener("click", () => loadAlbumDetailView(Number(button.dataset.searchAlbum)));
    });
    content.querySelectorAll("[data-search-artist]").forEach((button) => {
        button.addEventListener("click", () => loadArtistDetailView(Number(button.dataset.searchArtist)));
    });
}

async function loadAlbumDetailView(albumId) {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando album...</div>`;

    try {
        const [albumResponse, artistsResponse, songsResponse] = await Promise.all([
            apiFetch(`/api/buscarAlbum/${albumId}`),
            apiFetch("/api/artistas"),
            apiFetch(`/api/albumes/${albumId}/canciones`)
        ]);

        if (!albumResponse.ok) {
            throw new Error("No se pudo cargar el album.");
        }

        const album = await albumResponse.json();
        const artists = artistsResponse.ok ? await artistsResponse.json() : searchArtists;
        if (!songsResponse.ok) {
            throw new Error("No se pudieron cargar las canciones del album.");
        }

        const songs = await songsResponse.json();
        const artistName = artists.find((artist) => artist.artista_id === album.artista_album)?.artista_nombre || "Artista desconocido";

        searchArtists = artists;
        renderCollectionView({
            typeLabel: "Album",
            title: album.album_nombre || "Album sin nombre",
            subtitle: `${artistName} • ${songs.length} canciones`,
            image: album.album_portada_ruta,
            songs,
            artists,
            album,
            emptyMessage: "Este album todavia no tiene canciones."
        });
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
}

async function loadArtistDetailView(artistId) {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando artista...</div>`;

    try {
        const [artistsResponse, albumsResponse, songsResponse] = await Promise.all([
            apiFetch("/api/artistas"),
            apiFetch("/api/albumes"),
            apiFetch("/api/canciones")
        ]);

        if (!artistsResponse.ok || !albumsResponse.ok || !songsResponse.ok) {
            throw new Error("No se pudo cargar el artista.");
        }

        const artists = await artistsResponse.json();
        const albums = await albumsResponse.json();
        const songs = await songsResponse.json();
        const artist = artists.find((item) => item.artista_id === artistId);

        if (!artist) {
            throw new Error("Artista no encontrado.");
        }

        const artistAlbums = albums.filter((album) => album.artista_album === artistId);
        const artistSongs = songs.filter((song) => song.song_artista === artistId);

        searchArtists = artists;
        renderCollectionView({
            typeLabel: "Artista",
            title: artist.artista_nombre || "Artista sin nombre",
            subtitle: `${artistAlbums.length} albumes • ${artistSongs.length} canciones`,
            image: artist.artista_foto_ruta,
            songs: artistSongs,
            artists,
            albums: artistAlbums,
            emptyMessage: "Este artista todavia no tiene canciones."
        });
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
}

function renderCollectionView({ typeLabel, title, subtitle, image, likedCover = false, songs, artists, albums = [], album = null, playlistId = null, emptyMessage }) {
    const content = document.getElementById("content-view");
    const isAlbumSaved = album ? savedAlbums.some((savedAlbum) => savedAlbum.album_id === album.album_id) : false;
    const albumId = album?.album_id || null;
    currentCollectionStarted = false;
    const coverMarkup = likedCover
        ? `<div class="liked-cover collection-liked-cover">&hearts;</div>`
        : `<img src="${apiAssetUrl(image)}" alt="Portada">`;

    content.innerHTML = `
        <div class="playlist-header collection-header">
            ${coverMarkup}
            <div>
                <p>${escapeHtml(typeLabel)}</p>
                <h1>${escapeHtml(title)}</h1>
                <span>${escapeHtml(subtitle)}</span>
            </div>
        </div>

        <div class="playlist-actions">
            <button class="play-main-btn" id="collection-play-btn" type="button" aria-label="Reproducir">▶</button>
            <button class="shuffle-play-btn" id="collection-shuffle-btn" type="button" aria-label="Reproducir aleatoriamente" title="Reproducir aleatoriamente">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M16 3h5v5h-2V6.41l-4.64 4.64-1.41-1.41L17.59 5H16V3Z"></path>
                    <path d="M4 7h3.17c1.06 0 2.08.42 2.83 1.17l7.59 7.59V14H21v5h-5v-2h1.59L8.59 9H4V7Z"></path>
                    <path d="M4 17h3.17c1.06 0 2.08-.42 2.83-1.17l1.88-1.88 1.41 1.41-1.88 1.88A6 6 0 0 1 7.17 19H4v-2Z"></path>
                </svg>
            </button>
            ${album && !isGuestSession() ? `
                <button class="save-album-btn ${isAlbumSaved ? "saved" : ""}" id="save-album-btn" type="button" aria-label="Guardar album">
                    ${isAlbumSaved ? "Guardado" : "+"}
                </button>
            ` : ""}
        </div>

        ${albums.length ? `
            <section class="search-result-group collection-section">
                <h3>Albumes</h3>
                <div class="search-result-grid">
                    ${albums.map(renderAlbumResult).join("")}
                </div>
            </section>
        ` : ""}

        <div class="songs-table collection-songs ${albumId ? "album-songs" : ""}">
            ${renderCollectionSongs(songs, artists, emptyMessage, playlistId, albumId)}
        </div>
    `;

    document.getElementById("collection-play-btn")?.addEventListener("click", () => {
        const audio = document.getElementById("audio-player");
        const playButton = document.getElementById("collection-play-btn");
        if (currentCollectionStarted && audio?.src) {
            if (audio.paused) {
                audio.play();
                playButton.textContent = "⏸";
            } else {
                audio.pause();
                playButton.textContent = "▶";
            }
            return;
        }

        const firstPlayable = songs.findIndex((song) => song.song_archivo_ruta);
        if (firstPlayable >= 0) {
            playSongQueue(songs, artists, firstPlayable);
            currentCollectionStarted = true;
            playButton.textContent = "⏸";
            document.getElementById("collection-shuffle-btn")?.classList.remove("active");
            markActiveCollectionSong(firstPlayable);
        }
    });

    document.getElementById("collection-shuffle-btn")?.addEventListener("click", () => {
        const shuffleButton = document.getElementById("collection-shuffle-btn");
        const started = playSongShuffleQueue(songs, artists);
        if (started) {
            currentCollectionStarted = true;
            document.getElementById("collection-play-btn").textContent = "⏸";
            shuffleButton.classList.add("active");
        }
    });

    document.getElementById("save-album-btn")?.addEventListener("click", () => toggleAlbumInLibrary(album.album_id));

    content.querySelectorAll("[data-collection-song]").forEach((row) => {
        row.addEventListener("click", () => {
            const index = Number(row.dataset.collectionSong);
            playSongQueue(songs, artists, index);
            currentCollectionStarted = true;
            document.getElementById("collection-play-btn").textContent = "⏸";
            document.getElementById("collection-shuffle-btn")?.classList.remove("active");
            markActiveCollectionSong(index);
        });
    });

    content.querySelectorAll("[data-like-song]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            toggleSongLike(Number(button.dataset.likeSong), button);
        });
    });

    content.querySelectorAll("[data-add-song]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            openAddToPlaylistModal(Number(button.dataset.addSong));
        });
    });

    content.querySelectorAll("[data-song-options-menu]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            const actions = button.closest(".song-options-actions");
            content.querySelectorAll(".song-options-actions.menu-open").forEach((openActions) => {
                if (openActions !== actions) {
                    openActions.classList.remove("menu-open");
                }
            });
            actions.classList.toggle("menu-open");
        });
    });

    content.querySelectorAll("[data-menu-add-song]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            button.closest(".song-options-actions")?.classList.remove("menu-open");
            openAddToPlaylistModal(Number(button.dataset.menuAddSong));
        });
    });

    content.querySelectorAll("[data-remove-playlist-song]").forEach((button) => {
        button.addEventListener("click", async (event) => {
            event.stopPropagation();
            button.closest(".song-options-actions")?.classList.remove("menu-open");
            await removeSongFromPlaylist(playlistId, Number(button.dataset.removePlaylistSong));
        });
    });

    content.querySelectorAll("[data-search-album]").forEach((button) => {
        button.addEventListener("click", () => loadAlbumDetailView(Number(button.dataset.searchAlbum)));
    });

    preloadCollectionDurations(songs);
}

async function toggleAlbumInLibrary(albumId) {
    const button = document.getElementById("save-album-btn");
    const isSaved = savedAlbums.some((savedAlbum) => savedAlbum.album_id === albumId);

    if (isSaved) {
        const confirmed = await openConfirmDialog({
            title: "Quitar album",
            message: "Seguro que quieres quitar este album de tu biblioteca?",
            confirmText: "Quitar",
            danger: true
        });

        if (!confirmed) {
            return;
        }
    }

    if (button) {
        button.disabled = true;
    }

    try {
        const response = await apiFetch(`/api/biblioteca/albumes/${albumId}`, {
            method: isSaved ? "DELETE" : "POST"
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo guardar el album.");
        }

        await renderUserLibrary();

        if (button) {
            button.textContent = isSaved ? "+" : "Guardado";
            button.classList.toggle("saved", !isSaved);
        }
    } catch (error) {
        alert(error.message);
    } finally {
        if (button) {
            button.disabled = false;
        }
    }
}

async function toggleSongLike(songId, button) {
    const liked = likedSongIds.includes(songId);
    button.disabled = true;

    try {
        const response = await apiFetch(`/api/likes/${songId}`, {
            method: liked ? "DELETE" : "POST"
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo actualizar el like.");
        }

        likedSongIds = liked
            ? likedSongIds.filter((id) => id !== songId)
            : [...likedSongIds, songId];

        button.classList.toggle("liked", !liked);
        button.textContent = !liked ? "♥" : "♡";
        await renderUserLibrary();
    } catch (error) {
        alert(error.message);
    } finally {
        button.disabled = false;
    }
}

async function openAddToPlaylistModal(songId) {
    if (!userPlaylists.length) {
        await renderUserLibrary();
    }

    const existingModal = document.getElementById("playlist-modal-backdrop");
    if (existingModal) {
        existingModal.remove();
    }

    document.body.insertAdjacentHTML("beforeend", `
        <div id="playlist-modal-backdrop" class="playlist-modal-backdrop">
            <div class="playlist-modal" role="dialog" aria-modal="true" aria-labelledby="playlist-modal-title">
                <div class="playlist-modal-header">
                    <h3 id="playlist-modal-title">Agregar a playlist</h3>
                    <button type="button" id="playlist-modal-close" aria-label="Cerrar">×</button>
                </div>
                <div class="playlist-modal-list">
                    ${userPlaylists.length ? userPlaylists.map((playlist) => `
                        <button type="button" class="playlist-modal-option" data-modal-playlist="${playlist.playlist_id}">
                            <img src="${apiAssetUrl(playlist.playlist_portada_ruta)}" alt="Portada">
                            <span>${escapeHtml(playlist.playlist_nombre || "Playlist sin nombre")}</span>
                        </button>
                    `).join("") : `
                        <div class="song-list-state">Primero crea una playlist.</div>
                    `}
                </div>
            </div>
        </div>
    `);

    const backdrop = document.getElementById("playlist-modal-backdrop");
    document.getElementById("playlist-modal-close").addEventListener("click", () => backdrop.remove());
    backdrop.addEventListener("click", (event) => {
        if (event.target === backdrop) {
            backdrop.remove();
        }
    });

    backdrop.querySelectorAll("[data-modal-playlist]").forEach((button) => {
        button.addEventListener("click", async () => {
            await addSongToPlaylist(songId, Number(button.dataset.modalPlaylist));
            backdrop.remove();
        });
    });
}

async function addSongToPlaylist(songId, playlistId) {
    try {
        const response = await apiFetch(`/api/playlists/${playlistId}/canciones/${songId}`, {
            method: "POST"
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo agregar la cancion.");
        }
    } catch (error) {
        alert(error.message);
    }
}

async function removeSongFromPlaylist(playlistId, songId) {
    const confirmed = await openConfirmDialog({
        title: "Quitar cancion",
        message: "Seguro que quieres quitar esta cancion de la playlist?",
        confirmText: "Quitar cancion",
        danger: true
    });

    if (!confirmed) {
        return;
    }

    try {
        const response = await apiFetch(`/api/playlists/${playlistId}/canciones/${songId}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "No se pudo quitar la cancion.");
        }

        loadPlaylistDetailView(playlistId);
    } catch (error) {
        alert(error.message);
    }
}

function renderCollectionSongs(songs, artists, emptyMessage, playlistId = null, albumId = null) {
    if (!songs.length) {
        return `<div class="song-list-state">${emptyMessage}</div>`;
    }

    return `
        <div class="song-row header-row">
            <span>#</span>
            <span>Titulo</span>
            <span>Artista</span>
            <span>Duracion</span>
        </div>
        ${songs.map((song, index) => `
            <button class="song-row collection-song-row" type="button" data-collection-song="${index}" data-collection-song-id="${song.song_id}" ${song.song_archivo_ruta ? "" : "disabled"}>
                <span>${index + 1}</span>
                <span>${escapeHtml(song.song_nombre || "Sin nombre")}</span>
                <span>${escapeHtml(getArtistNameFromList(song.song_artista, artists))}</span>
                <span class="song-actions">
                    <span id="collection-duration-${index}">${song.song_archivo_ruta ? "--:--" : "-"}</span>
                    ${albumId && !isGuestSession() ? renderSongOptionsActions(song) : ""}
                    ${playlistId && !isGuestSession() ? renderSongOptionsActions(song, true) : ""}
                    ${!albumId && !playlistId && !isGuestSession() ? `<span class="add-playlist-btn" role="button" tabindex="0" data-add-song="${song.song_id}">+</span>` : ""}
                    ${!isGuestSession() ? `<span class="like-song-btn ${likedSongIds.includes(song.song_id) ? "liked" : ""}" role="button" tabindex="0" data-like-song="${song.song_id}">${likedSongIds.includes(song.song_id) ? "♥" : "♡"}</span>` : ""}
                </span>
            </button>
        `).join("")}
    `;
}

function renderSongOptionsActions(song, canRemoveFromPlaylist = false) {
    return `
        <span class="song-options-actions">
            <span class="song-options-menu-btn" role="button" tabindex="0" data-song-options-menu="${song.song_id}" aria-label="Opciones de cancion">...</span>
            <span class="song-options-menu">
                <span role="button" tabindex="0" data-menu-add-song="${song.song_id}">Agregar a playlist</span>
                ${canRemoveFromPlaylist ? `<span class="danger" role="button" tabindex="0" data-remove-playlist-song="${song.song_id}">Quitar de playlist</span>` : ""}
            </span>
        </span>
    `;
}

function preloadCollectionDurations(songs) {
    songs.forEach((song, index) => {
        if (!song.song_archivo_ruta) {
            return;
        }

        const audio = new Audio(apiAssetUrl(song.song_archivo_ruta));
        audio.preload = "metadata";
        audio.addEventListener("loadedmetadata", () => {
            const duration = document.getElementById(`collection-duration-${index}`);
            if (duration) {
                duration.textContent = formatDuration(audio.duration);
            }
        });
    });
}

function formatDuration(seconds) {
    if (!Number.isFinite(seconds)) {
        return "0:00";
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60).toString().padStart(2, "0");
    return `${minutes}:${remainingSeconds}`;
}

function markActiveCollectionSong(index) {
    document.querySelectorAll(".collection-song-row").forEach((row) => row.classList.remove("active"));
    document.querySelector(`[data-collection-song="${index}"]`)?.classList.add("active");
}

function markActiveCollectionSongById(songId) {
    if (!songId) {
        return;
    }

    const activeRow = document.querySelector(`[data-collection-song-id="${songId}"]`);
    if (!activeRow) {
        return;
    }

    document.querySelectorAll(".collection-song-row").forEach((row) => row.classList.remove("active"));
    activeRow.classList.add("active");
}

function playSearchSong(song) {
    if (!song.song_archivo_ruta) {
        return;
    }

    const songIndex = searchSongs.findIndex((item) => item.song_id === song.song_id);
    playSongQueue(searchSongs.length ? searchSongs : [song], searchArtists, songIndex >= 0 ? songIndex : 0);
}

function getSearchArtistName(artistId) {
    return searchArtists.find((artist) => artist.artista_id === artistId)?.artista_nombre || "Artista desconocido";
}

function getArtistNameFromList(artistId, artists) {
    return artists.find((artist) => artist.artista_id === artistId)?.artista_nombre || "Artista desconocido";
}

function setPlayerSongInfoVisible(isVisible) {
    const songInfo = document.querySelector(".song-info");

    if (songInfo) {
        songInfo.style.visibility = isVisible ? "visible" : "hidden";
    }
}

function openConfirmDialog({ title, message, confirmText = "Aceptar", cancelText = "Cancelar", danger = false }) {
    return new Promise((resolve) => {
        document.getElementById("confirm-modal-backdrop")?.remove();

        document.body.insertAdjacentHTML("beforeend", `
            <div id="confirm-modal-backdrop" class="playlist-modal-backdrop">
                <div class="confirm-modal" role="dialog" aria-modal="true" aria-labelledby="confirm-modal-title">
                    <div class="confirm-modal-body">
                        <h3 id="confirm-modal-title">${escapeHtml(title)}</h3>
                        <p>${escapeHtml(message)}</p>
                    </div>
                    <div class="confirm-modal-actions">
                        <button class="confirm-cancel-btn" type="button" id="confirm-modal-cancel">${escapeHtml(cancelText)}</button>
                        <button class="confirm-accept-btn ${danger ? "danger" : ""}" type="button" id="confirm-modal-accept">${escapeHtml(confirmText)}</button>
                    </div>
                </div>
            </div>
        `);

        const backdrop = document.getElementById("confirm-modal-backdrop");
        const close = (value) => {
            backdrop.remove();
            resolve(value);
        };

        document.getElementById("confirm-modal-cancel").addEventListener("click", () => close(false));
        document.getElementById("confirm-modal-accept").addEventListener("click", () => close(true));
        backdrop.addEventListener("click", (event) => {
            if (event.target === backdrop) {
                close(false);
            }
        });
    });
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

async function recordPlayback(songId, duration = 0) {
    if (!songId || !getAuthToken() || isGuestSession()) {
        return;
    }

    try {
        await apiFetch("/api/reproducciones", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                cancion_id: songId,
                duracion_reproduccion: duration
            })
        });
    } catch (error) {
        console.error("No se pudo registrar la reproduccion:", error);
    }
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
