import { apiAssetUrl, apiFetch, getAuthToken } from "./api.js";

const state = {
    canciones: [],
    artistas: [],
    albumes: []
};

const qs = (selector) => document.querySelector(selector);

function setMessage(selector, text, isError = false) {
    const element = qs(selector);
    element.textContent = text;
    element.style.color = isError ? "#ffb4b4" : "#b9f6ca";
}

function artistName(id) {
    return state.artistas.find((artist) => artist.artista_id === id)?.artista_nombre ?? id ?? "";
}

function renderArtistOptions() {
    const options = [
        `<option value="">Selecciona un artista</option>`,
        ...state.artistas.map((artist) => `<option value="${artist.artista_nombre}">${artist.artista_nombre}</option>`)
    ].join("");

    qs("#song-artist").innerHTML = options;
    qs("#album-artist").innerHTML = options;
}

async function loadArtists() {
    const response = await apiFetch("/api/artistas");
    if (!response.ok) {
        throw new Error("No se pudieron cargar los artistas");
    }

    state.artistas = await response.json();
    renderArtistOptions();
    renderArtists();
}

async function loadAlbums() {
    const response = await apiFetch("/api/albumes");
    if (!response.ok) {
        throw new Error("No se pudieron cargar los albumes");
    }

    state.albumes = await response.json();
    renderAlbums();
}

async function loadSongs() {
    const response = await apiFetch("/api/canciones");
    if (!response.ok) {
        throw new Error("No se pudieron cargar las canciones");
    }

    state.canciones = await response.json();
    renderSongs();
}

async function refreshAll() {
    try {
        await loadArtists();
        await loadAlbums();
        await loadSongs();
    } catch (error) {
        setMessage("#song-message", error.message, true);
    }
}

async function loadAlbumsForSongArtist(selectedAlbum = "") {
    const artist = qs("#song-artist").value;
    const albumSelect = qs("#song-album");
    albumSelect.innerHTML = `<option value="">Sin album</option>`;
    albumSelect.disabled = true;

    if (!artist) {
        return;
    }

    const response = await apiFetch(`/api/albumes/artista?artista_nombre=${encodeURIComponent(artist)}`);
    if (!response.ok) {
        setMessage("#song-message", "No se pudieron cargar los albumes del artista.", true);
        return;
    }

    const albumes = await response.json();
    albumSelect.innerHTML = [
        `<option value="">Sin album</option>`,
        ...albumes.map((album) => `<option value="${album.album_nombre}">${album.album_nombre}</option>`)
    ].join("");
    albumSelect.disabled = false;
    albumSelect.value = selectedAlbum;
}

function renderSongs() {
    const body = qs("#songs-table-body");
    if (!state.canciones.length) {
        body.innerHTML = `<tr><td colspan="6">No hay canciones registradas.</td></tr>`;
        return;
    }

    body.innerHTML = state.canciones.map((song) => `
        <tr>
            <td><img class="entity-thumb" src="${apiAssetUrl(song.song_portada_ruta)}" alt="Portada"></td>
            <td>
                <span class="entity-title">
                    <strong>${song.song_nombre ?? ""}</strong>
                    <small>ID ${song.song_id}</small>
                </span>
            </td>
            <td>${artistName(song.song_artista)}</td>
            <td>${song.song_genero ?? ""}</td>
            <td>
                ${song.song_archivo_ruta
                    ? `<a href="${apiAssetUrl(song.song_archivo_ruta)}" target="_blank" rel="noreferrer">Ver archivo</a>`
                    : `<span class="muted">Sin archivo</span>`}
            </td>
            <td>
                <div class="row-actions">
                    <button class="btn btn-sm btn-outline-light" data-entity="song" data-action="edit" data-id="${song.song_id}">Editar</button>
                    <button class="btn btn-sm btn-outline-danger" data-entity="song" data-action="delete" data-id="${song.song_id}">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderArtists() {
    const body = qs("#artists-table-body");
    if (!state.artistas.length) {
        body.innerHTML = `<tr><td colspan="3">No hay artistas registrados.</td></tr>`;
        return;
    }

    body.innerHTML = state.artistas.map((artist) => `
        <tr>
            <td><img class="entity-thumb" src="${apiAssetUrl(artist.artista_foto_ruta)}" alt="Foto"></td>
            <td>
                <span class="entity-title">
                    <strong>${artist.artista_nombre ?? ""}</strong>
                    <small>ID ${artist.artista_id}</small>
                </span>
            </td>
            <td>
                <div class="row-actions">
                    <button class="btn btn-sm btn-outline-light" data-entity="artist" data-action="edit" data-id="${artist.artista_id}">Editar</button>
                    <button class="btn btn-sm btn-outline-danger" data-entity="artist" data-action="delete" data-id="${artist.artista_id}">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderAlbums() {
    const body = qs("#albums-table-body");
    if (!state.albumes.length) {
        body.innerHTML = `<tr><td colspan="4">No hay albumes registrados.</td></tr>`;
        return;
    }

    body.innerHTML = state.albumes.map((album) => `
        <tr>
            <td><img class="entity-thumb" src="${apiAssetUrl(album.album_portada_ruta)}" alt="Portada"></td>
            <td>
                <span class="entity-title">
                    <strong>${album.album_nombre ?? ""}</strong>
                    <small>ID ${album.album_id}</small>
                </span>
            </td>
            <td>${artistName(album.artista_album)}</td>
            <td>
                <div class="row-actions">
                    <button class="btn btn-sm btn-outline-light" data-entity="album" data-action="edit" data-id="${album.album_id}">Editar</button>
                    <button class="btn btn-sm btn-outline-danger" data-entity="album" data-action="delete" data-id="${album.album_id}">Eliminar</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function resetSongForm() {
    qs("#song-form").reset();
    qs("#song-id").value = "";
    qs("#song-save-btn").textContent = "Guardar";
    qs("#song-album").innerHTML = `<option value="">Sin album</option>`;
    qs("#song-album").disabled = true;
    setMessage("#song-message", "");
}

function resetArtistForm() {
    qs("#artist-form").reset();
    qs("#artist-id").value = "";
    qs("#artist-save-btn").textContent = "Guardar";
    setMessage("#artist-message", "");
}

function resetAlbumForm() {
    qs("#album-form").reset();
    qs("#album-id").value = "";
    qs("#album-save-btn").textContent = "Guardar";
    setMessage("#album-message", "");
}

function songFormData() {
    const data = new FormData();
    data.append("song_nombre", qs("#song-name").value.trim());
    data.append("artista_nombre", qs("#song-artist").value);
    data.append("album_nombre", qs("#song-album").value);
    data.append("song_compositor", qs("#song-composer").value.trim());
    data.append("song_genero", qs("#song-genre").value);

    if (qs("#song-file").files.length) {
        data.append("archivoCancion", qs("#song-file").files[0]);
    }

    if (qs("#song-cover").files.length) {
        data.append("portadaCancion", qs("#song-cover").files[0]);
    }

    return data;
}

function artistFormData() {
    const data = new FormData();
    data.append("artista_nombre", qs("#artist-name").value.trim());

    if (qs("#artist-photo").files.length) {
        data.append("fotoArtista", qs("#artist-photo").files[0]);
    }

    return data;
}

function albumFormData() {
    const data = new FormData();
    data.append("album_nombre", qs("#album-name").value.trim());
    data.append("artista_nombre", qs("#album-artist").value);

    if (qs("#album-cover").files.length) {
        data.append("portadaAlbum", qs("#album-cover").files[0]);
    }

    return data;
}

async function saveSong(event) {
    event.preventDefault();
    const id = qs("#song-id").value;
    const path = id ? `/api/actualizarCancion/${id}` : "/api/crearCancion";

    try {
        const response = await apiFetch(path, {
            method: id ? "PUT" : "POST",
            body: songFormData()
        });

        if (!response.ok) {
            throw new Error(await response.text() || "No se pudo guardar la cancion");
        }

        resetSongForm();
        setMessage("#song-message", "Cancion guardada correctamente.");
        await loadSongs();
    } catch (error) {
        setMessage("#song-message", error.message, true);
    }
}

async function saveArtist(event) {
    event.preventDefault();
    const id = qs("#artist-id").value;
    const path = id ? `/api/actualizarArtista/${id}` : "/api/crearArtista";

    try {
        const response = await apiFetch(path, {
            method: id ? "PUT" : "POST",
            body: artistFormData()
        });

        if (!response.ok) {
            throw new Error(await response.text() || "No se pudo guardar el artista");
        }

        resetArtistForm();
        setMessage("#artist-message", "Artista guardado correctamente.");
        await loadArtists();
        await loadAlbums();
        renderSongs();
    } catch (error) {
        setMessage("#artist-message", error.message, true);
    }
}

async function saveAlbum(event) {
    event.preventDefault();
    const id = qs("#album-id").value;
    const path = id ? `/api/actualizarAlbum/${id}` : "/api/crearAlbum";

    try {
        const response = await apiFetch(path, {
            method: id ? "PUT" : "POST",
            body: albumFormData()
        });

        if (!response.ok) {
            throw new Error(await response.text() || "No se pudo guardar el album");
        }

        resetAlbumForm();
        setMessage("#album-message", "Album guardado correctamente.");
        await loadAlbums();
    } catch (error) {
        setMessage("#album-message", error.message, true);
    }
}

async function editSong(id) {
    const song = state.canciones.find((item) => item.song_id === id);
    if (!song) {
        return;
    }

    qs("#song-id").value = song.song_id;
    qs("#song-name").value = song.song_nombre ?? "";
    qs("#song-artist").value = artistName(song.song_artista);
    qs("#song-composer").value = song.song_compositor ?? "";
    qs("#song-genre").value = song.song_genero ?? "";
    qs("#song-save-btn").textContent = "Actualizar";

    let selectedAlbum = "";
    const albumResponse = await apiFetch(`/api/canciones/${song.song_id}/album`);
    if (albumResponse.ok) {
        selectedAlbum = (await albumResponse.json()).album_nombre ?? "";
    }

    await loadAlbumsForSongArtist(selectedAlbum);
    setMessage("#song-message", "Editando cancion ID " + song.song_id);
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function editArtist(id) {
    const artist = state.artistas.find((item) => item.artista_id === id);
    if (!artist) {
        return;
    }

    qs("#artist-id").value = artist.artista_id;
    qs("#artist-name").value = artist.artista_nombre ?? "";
    qs("#artist-save-btn").textContent = "Actualizar";
    setMessage("#artist-message", "Editando artista ID " + artist.artista_id);
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function editAlbum(id) {
    const album = state.albumes.find((item) => item.album_id === id);
    if (!album) {
        return;
    }

    qs("#album-id").value = album.album_id;
    qs("#album-name").value = album.album_nombre ?? "";
    qs("#album-artist").value = artistName(album.artista_album);
    qs("#album-save-btn").textContent = "Actualizar";
    setMessage("#album-message", "Editando album ID " + album.album_id);
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function deleteEntity(entity, id) {
    const endpoint = {
        song: `/api/eliminarCancion/${id}`,
        artist: `/api/eliminarArtista/${id}`,
        album: `/api/eliminarAlbum/${id}`
    }[entity];

    if (!window.confirm("Eliminar este registro?")) {
        return;
    }

    const response = await apiFetch(endpoint, { method: "DELETE" });
    if (!response.ok) {
        const messageSelector = entity === "song" ? "#song-message" : entity === "artist" ? "#artist-message" : "#album-message";
        setMessage(messageSelector, await response.text() || "No se pudo eliminar el registro", true);
        return;
    }

    if (entity === "song") {
        await loadSongs();
    } else if (entity === "artist") {
        await loadArtists();
        await loadAlbums();
        renderSongs();
    } else {
        await loadAlbums();
    }
}

async function canAccessAdmin() {
    if (!getAuthToken()) {
        window.location.href = "login.html";
        return false;
    }

    const response = await apiFetch("/api/auth/me");
    if (!response.ok) {
        window.location.href = "login.html";
        return false;
    }

    const user = await response.json();
    if (user.usuario_rol !== 1) {
        window.location.href = "home.html";
        return false;
    }

    return true;
}

async function initAdmin() {
    if (!(await canAccessAdmin())) {
        return;
    }

    document.addEventListener("click", (event) => {
        const tabButton = event.target.closest(".tab-button");
        if (tabButton) {
            document.querySelectorAll(".tab-button").forEach((button) => button.classList.remove("active"));
            document.querySelectorAll(".crud-view").forEach((view) => view.classList.remove("active"));
            tabButton.classList.add("active");
            qs(`#${tabButton.dataset.tab}-view`).classList.add("active");
            return;
        }

        const actionButton = event.target.closest("button[data-entity]");
        if (!actionButton) {
            return;
        }

        const id = Number(actionButton.dataset.id);
        const { entity, action } = actionButton.dataset;

        if (action === "delete") {
            deleteEntity(entity, id);
        } else if (entity === "song") {
            editSong(id);
        } else if (entity === "artist") {
            editArtist(id);
        } else if (entity === "album") {
            editAlbum(id);
        }
    });

    qs("#song-artist").addEventListener("change", () => loadAlbumsForSongArtist());
    qs("#song-form").addEventListener("submit", saveSong);
    qs("#artist-form").addEventListener("submit", saveArtist);
    qs("#album-form").addEventListener("submit", saveAlbum);
    qs("#new-song-btn").addEventListener("click", resetSongForm);
    qs("#song-cancel-btn").addEventListener("click", resetSongForm);
    qs("#new-artist-btn").addEventListener("click", resetArtistForm);
    qs("#artist-cancel-btn").addEventListener("click", resetArtistForm);
    qs("#new-album-btn").addEventListener("click", resetAlbumForm);
    qs("#album-cancel-btn").addEventListener("click", resetAlbumForm);

    refreshAll();
}

initAdmin();
