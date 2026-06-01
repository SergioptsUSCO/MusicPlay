import { apiAssetUrl, apiFetch, getAuthToken, isGuestSession } from "../api.js";

let songs = [];
let artists = [];
let playbackSongs = [];
let playbackArtists = [];
let currentIndex = -1;
let shouldWrapPlaybackQueue = true;
let likedSongIds = [];
let recommendedSongs = [];
let currentPlaybackSong = null;

export async function loadHomeView() {

    const content = document.getElementById("content-view");

    content.innerHTML = `
        <section class="music-section suggestions-section">
            <div class="section-header">
                <h2>Sugerencias</h2>
                <span id="suggestions-count">Cargando...</span>
            </div>

            <div class="suggestions-grid" id="suggestions-list">
                <div class="song-list-state">Cargando sugerencias...</div>
            </div>
        </section>

        <section class="music-section songs-library">
            <div class="section-header">
                <h2>Canciones disponibles</h2>
                <span id="songs-count">Cargando...</span>
            </div>

            <div class="songs-list" id="songs-list">
                <div class="song-list-state">Cargando canciones...</div>
            </div>
        </section>

        <footer class="home-site-footer">
            <div class="home-footer-grid">
                <div>
                    <p>© 2025 MusicPlay</p>
                </div>
                <div class="home-footer-logo">
                    <img src="../assets/logo.svg" alt="MusicPlay Logo" width="40" height="40">
                </div>
                <nav class="home-footer-nav" aria-label="Footer">
                    <button type="button" data-home-action="inicio">Inicio</button>
                    <button type="button" data-home-action="artistas">Artistas</button>
                    <button type="button" data-home-action="albumes">Albumes</button>
                    <button type="button" data-home-action="reciente">Reciente</button>
                </nav>
            </div>
        </footer>
    `;

    setupPlayerControls();
    setupHomeFooterActions();
    await loadSongs();
}

async function loadSongs() {
    const list = document.getElementById("songs-list");
    const count = document.getElementById("songs-count");

    try {
        const [songsResponse, artistsResponse] = await Promise.all([
            apiFetch("/api/canciones"),
            apiFetch("/api/artistas")
        ]);

        if (!songsResponse.ok) {
            throw new Error("No se pudieron cargar las canciones");
        }

        songs = sortSongsAlphabetically(await songsResponse.json());
        artists = artistsResponse.ok ? await artistsResponse.json() : [];
        likedSongIds = await loadLikedSongIds();

        count.textContent = `${songs.length} canciones`;
        await loadSuggestions();
        renderSongs(list);
        preloadDurations();
    } catch (error) {
        list.innerHTML = `<div class="song-list-state">${error.message}</div>`;
        count.textContent = "Sin datos";
        renderSuggestionsState("No se pudieron cargar las sugerencias.");
    }
}

function setupHomeFooterActions() {
    document.querySelectorAll("[data-home-action]").forEach((button) => {
        button.addEventListener("click", () => {
            const action = button.dataset.homeAction;

            if (action === "inicio") {
                loadHomeView();
                return;
            }

            if (action === "reciente") {
                document.querySelector(".songs-library")?.scrollIntoView({ behavior: "smooth" });
                return;
            }

            window.dispatchEvent(new CustomEvent("musicplay:home-footer-action", {
                detail: { action }
            }));
        });
    });
}

function sortSongsAlphabetically(sourceSongs) {
    return [...sourceSongs].sort((first, second) => {
        const firstName = normalizeText(first.song_nombre || "");
        const secondName = normalizeText(second.song_nombre || "");
        return firstName.localeCompare(secondName, "es", { sensitivity: "base" });
    });
}

function normalizeText(value) {
    return String(value)
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase();
}

async function loadSuggestions() {
    const suggestionsList = document.getElementById("suggestions-list");
    const suggestionsCount = document.getElementById("suggestions-count");

    if (!getAuthToken() || isGuestSession()) {
        recommendedSongs = [];
        renderSuggestionsState("Inicia sesion para ver sugerencias personalizadas.");
        return;
    }

    try {
        const userResponse = await apiFetch("/api/auth/me");

        if (!userResponse.ok) {
            throw new Error("Sin usuario autenticado.");
        }

        const user = await userResponse.json();
        const response = await apiFetch(`/api/algoritmo/usuarios/${user.usuario_id}/recomendaciones`);

        if (!response.ok) {
            throw new Error("Sin sugerencias disponibles.");
        }

        const body = await response.json();
        recommendedSongs = (body.recomendaciones || [])
            .map((recommendation) => {
                const song = songs.find((item) => item.song_id === recommendation.cancion_id);
                return song ? { ...song, score_recomendacion: recommendation.score_recomendacion } : null;
            })
            .filter(Boolean);

        if (!recommendedSongs.length) {
            recommendedSongs = songs.filter((song) => song.song_archivo_ruta).slice(0, 10);
        }

        suggestionsCount.textContent = `${recommendedSongs.length} canciones`;
        renderSuggestions(suggestionsList);
    } catch (error) {
        recommendedSongs = songs.filter((song) => song.song_archivo_ruta).slice(0, 10);
        suggestionsCount.textContent = `${recommendedSongs.length} canciones`;
        renderSuggestions(suggestionsList);
        console.error("No se pudieron cargar sugerencias del modelo:", error);
    }
}

function renderSuggestionsState(message) {
    const suggestionsList = document.getElementById("suggestions-list");
    const suggestionsCount = document.getElementById("suggestions-count");

    if (suggestionsList) {
        suggestionsList.innerHTML = `<div class="song-list-state">${message}</div>`;
    }

    if (suggestionsCount) {
        suggestionsCount.textContent = "Sin datos";
    }
}

function renderSuggestions(list) {
    if (!recommendedSongs.length) {
        renderSuggestionsState("Todavia no hay sugerencias para este usuario.");
        return;
    }

    list.innerHTML = recommendedSongs.map((song, index) => `
        <button class="suggestion-item" type="button" data-suggestion-index="${index}" ${song.song_archivo_ruta ? "" : "disabled"}>
            <img src="${apiAssetUrl(song.song_portada_ruta)}" alt="Portada">
            <span>
                <strong>${escapeHtml(song.song_nombre ?? "Sin nombre")}</strong>
                <small>${escapeHtml(getArtistName(song.song_artista))}</small>
            </span>
        </button>
    `).join("");

    list.querySelectorAll("[data-suggestion-index]").forEach((button) => {
        button.addEventListener("click", () => {
            playSuggestion(Number(button.dataset.suggestionIndex));
        });
    });
}

function renderSongs(list) {
    if (!songs.length) {
        list.innerHTML = `<div class="song-list-state">Todavia no hay canciones cargadas.</div>`;
        return;
    }

    list.innerHTML = `
        <div class="song-list-row song-list-header">
            <span>#</span>
            <span>Titulo</span>
            <span>Artista</span>
            <span>Duracion</span>
        </div>
        ${songs.map((song, index) => `
            <button class="song-list-row song-play-row" type="button" data-song-index="${index}" ${song.song_archivo_ruta ? "" : "disabled"}>
                <span class="song-index">${index + 1}</span>
                <span class="song-main">
                    <img src="${apiAssetUrl(song.song_portada_ruta)}" alt="Portada">
                    <span>
                        <strong>${song.song_nombre ?? "Sin nombre"}</strong>
                        ${song.song_archivo_ruta ? "" : `<small>Sin archivo de audio</small>`}
                    </span>
                </span>
                <span>${getArtistName(song.song_artista)}</span>
                <span class="song-actions">
                    <span id="song-duration-${index}">${song.song_archivo_ruta ? "--:--" : "-"}</span>
                    ${!isGuestSession() ? `<span class="add-playlist-btn" role="button" tabindex="0" data-add-song="${song.song_id}">+</span>` : ""}
                    ${!isGuestSession() ? `<span class="like-song-btn ${likedSongIds.includes(song.song_id) ? "liked" : ""}" role="button" tabindex="0" data-like-song="${song.song_id}">${likedSongIds.includes(song.song_id) ? "\u2665" : "\u2661"}</span>` : ""}
                </span>
            </button>
        `).join("")}
    `;

    list.querySelectorAll(".song-play-row").forEach((row) => {
        row.addEventListener("click", () => playSong(Number(row.dataset.songIndex)));
    });

    list.querySelectorAll("[data-like-song]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            toggleSongLike(Number(button.dataset.likeSong), button);
        });
    });

    list.querySelectorAll("[data-add-song]").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
            window.dispatchEvent(new CustomEvent("musicplay:add-song-to-playlist", {
                detail: { songId: Number(button.dataset.addSong) }
            }));
        });
    });
}

async function loadLikedSongIds() {
    if (!getAuthToken() || isGuestSession()) {
        return [];
    }

    try {
        const response = await apiFetch("/api/likes");
        return response.ok ? await response.json() : [];
    } catch {
        return [];
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
            return;
        }

        likedSongIds = liked
            ? likedSongIds.filter((id) => id !== songId)
            : [...likedSongIds, songId];

        button.classList.toggle("liked", !liked);
        button.textContent = !liked ? "\u2665" : "\u2661";
        window.dispatchEvent(new CustomEvent("musicplay:likes-updated"));
    } finally {
        button.disabled = false;
    }
}

function setupPlayerControls() {
    const audio = document.getElementById("audio-player");
    const playButton = document.getElementById("play-btn");
    const previousButton = document.getElementById("prev-btn");
    const nextButton = document.getElementById("next-btn");
    const viewSongButton = document.getElementById("view-song-btn");
    const volumeRange = document.getElementById("volume-range");
    const progressRange = document.getElementById("progress-range");

    if (!audio || audio.dataset.ready === "true") {
        return;
    }

    audio.dataset.ready = "true";
    audio.volume = Number(volumeRange.value);

    playButton.addEventListener("click", () => {
        if (!audio.src) {
            const firstPlayable = songs.findIndex((song) => song.song_archivo_ruta);
            if (firstPlayable >= 0) {
                startPlaybackQueue(songs, artists, firstPlayable, true);
            }
            return;
        }

        if (audio.paused) {
            audio.play();
        } else {
            audio.pause();
        }
    });

    previousButton.addEventListener("click", () => playAdjacent(-1));
    nextButton.addEventListener("click", () => playAdjacent(1));
    viewSongButton?.addEventListener("click", () => {
        if (currentPlaybackSong?.song_id) {
            loadSongDetailView(currentPlaybackSong.song_id);
        }
    });

    volumeRange.addEventListener("input", () => {
        audio.volume = Number(volumeRange.value);
    });

    progressRange.addEventListener("input", () => {
        if (audio.duration) {
            audio.currentTime = (Number(progressRange.value) / 100) * audio.duration;
        }
    });

    audio.addEventListener("play", () => {
        playButton.textContent = "\u23f8";
    });

    audio.addEventListener("pause", () => {
        playButton.textContent = "\u25b6";
    });

    audio.addEventListener("timeupdate", updateProgress);
    audio.addEventListener("loadedmetadata", updateProgress);
    audio.addEventListener("ended", () => playAdjacent(1));
}

function playSong(index) {
    startPlaybackQueue(songs, artists, index, true);
}

function playSuggestion(index) {
    startPlaybackQueue(recommendedSongs, artists, index, false);
}

function playPlaybackSong(index) {
    const song = playbackSongs[index];
    if (!song || !song.song_archivo_ruta) {
        return;
    }

    const audio = document.getElementById("audio-player");
    const viewSongButton = document.getElementById("view-song-btn");
    currentIndex = index;
    currentPlaybackSong = song;
    audio.src = apiAssetUrl(song.song_archivo_ruta);
    audio.play();

    document.getElementById("player-cover").src = apiAssetUrl(song.song_portada_ruta);
    document.getElementById("player-title").textContent = song.song_nombre ?? "Sin nombre";
    document.getElementById("player-artist").textContent = getArtistName(song.song_artista, playbackArtists);
    if (viewSongButton) {
        viewSongButton.disabled = false;
    }
    setPlayerSongInfoVisible(true);
    recordPlayback(song.song_id);
    document.querySelectorAll(".song-play-row").forEach((row) => row.classList.remove("active"));
    document.querySelector(`[data-song-index="${index}"]`)?.classList.add("active");
    window.dispatchEvent(new CustomEvent("musicplay:playback-song-changed", {
        detail: { songId: song.song_id }
    }));
}

async function loadSongDetailView(songId) {
    const content = document.getElementById("content-view");
    content.innerHTML = `<div class="song-list-state">Cargando informacion de la cancion...</div>`;

    try {
        const songResponse = await apiFetch(`/api/buscarCancion/${songId}`);

        if (!songResponse.ok) {
            throw new Error("No se pudo cargar la cancion.");
        }

        const song = await songResponse.json();
        const [artistsResponse, genreResponse, albumResponse] = await Promise.all([
            apiFetch("/api/artistas"),
            song.song_genero ? apiFetch(`/api/buscarGenero/${song.song_genero}`) : Promise.resolve(null),
            apiFetch(`/api/canciones/${song.song_id}/album`)
        ]);

        const detailArtists = artistsResponse?.ok ? await artistsResponse.json() : artists;
        const genre = genreResponse?.ok ? await genreResponse.json() : null;
        const album = albumResponse.ok ? await albumResponse.json() : await findAlbumForSong(song.song_id);
        const artistName = getArtistName(song.song_artista, detailArtists);
        const genreName = genre?.nombre_genero || "Sin genero";
        const albumName = album?.album_nombre || "Sin album";
        const canPlay = Boolean(song.song_archivo_ruta);

        content.innerHTML = `
            <section class="song-detail-view">
                <button class="song-detail-back" type="button" id="song-detail-back">Volver</button>

                <div class="song-detail-hero">
                    <img src="${apiAssetUrl(song.song_portada_ruta)}" alt="Portada">
                    <div class="song-detail-main">
                        <p>Cancion</p>
                        <h1>${escapeHtml(song.song_nombre || "Sin nombre")}</h1>
                        <button class="song-detail-title-link" type="button" data-song-artist="${song.song_artista}">
                            ${escapeHtml(artistName)}
                        </button>
                        <div class="song-detail-actions">
                            <button class="play-main-btn" id="song-detail-play" type="button" ${canPlay ? "" : "disabled"} aria-label="Reproducir">▶</button>
                            ${!isGuestSession() ? `<button class="song-detail-like ${likedSongIds.includes(song.song_id) ? "liked" : ""}" id="song-detail-like" type="button">${likedSongIds.includes(song.song_id) ? "\u2665" : "\u2661"}</button>` : ""}
                        </div>
                    </div>
                </div>

                <div class="song-detail-grid">
                    <button class="song-detail-info song-detail-selectable" type="button" data-song-artist="${song.song_artista}">
                        <span>Artista</span>
                        <strong>${escapeHtml(artistName)}</strong>
                    </button>
                    <div class="song-detail-info">
                        <span>Genero</span>
                        <strong>${escapeHtml(genreName)}</strong>
                    </div>
                    <button class="song-detail-info song-detail-selectable" type="button" data-song-album="${album?.album_id || ""}" ${album ? "" : "disabled"}>
                        <span>Album</span>
                        <strong>${escapeHtml(albumName)}</strong>
                    </button>
                    <div class="song-detail-info">
                        <span>Compositor</span>
                        <strong>${escapeHtml(song.song_compositor || "Sin compositor")}</strong>
                    </div>
                </div>
            </section>
        `;

        document.getElementById("song-detail-back")?.addEventListener("click", loadHomeView);
        document.getElementById("song-detail-play")?.addEventListener("click", () => {
            const localArtists = detailArtists.length ? detailArtists : artists;
            startPlaybackQueue([song], localArtists, 0, false);
        });
        document.getElementById("song-detail-like")?.addEventListener("click", (event) => {
            toggleSongLike(song.song_id, event.currentTarget);
        });
        content.querySelectorAll("[data-song-artist]").forEach((button) => {
            button.addEventListener("click", () => {
                window.dispatchEvent(new CustomEvent("musicplay:view-artist", {
                    detail: { artistId: Number(button.dataset.songArtist) }
                }));
            });
        });
        content.querySelectorAll("[data-song-album]").forEach((button) => {
            button.addEventListener("click", () => {
                window.dispatchEvent(new CustomEvent("musicplay:view-album", {
                    detail: { albumId: Number(button.dataset.songAlbum) }
                }));
            });
        });
    } catch (error) {
        content.innerHTML = `<div class="song-list-state">${error.message}</div>`;
    }
}

async function findAlbumForSong(songId) {
    try {
        const albumsResponse = await apiFetch("/api/albumes");

        if (!albumsResponse.ok) {
            return null;
        }

        const albums = await albumsResponse.json();
        for (const album of albums) {
            const songsResponse = await apiFetch(`/api/albumes/${album.album_id}/canciones`);

            if (!songsResponse.ok) {
                continue;
            }

            const albumSongs = await songsResponse.json();
            if (albumSongs.some((song) => song.song_id === songId)) {
                return album;
            }
        }
    } catch {
        return null;
    }

    return null;
}

export function playSongQueue(nextSongs, nextArtists = [], startIndex = 0) {
    setupPlayerControls();
    startPlaybackQueue(nextSongs, nextArtists, startIndex, false);
}

export function playSongShuffleQueue(nextSongs, nextArtists = []) {
    setupPlayerControls();

    const playableSongs = nextSongs.filter((song) => song.song_archivo_ruta);
    if (!playableSongs.length) {
        return false;
    }

    startPlaybackQueue(shuffleSongs(playableSongs), nextArtists, 0, false);
    return true;
}

function startPlaybackQueue(nextSongs, nextArtists = [], startIndex = 0, shouldWrap = true) {
    playbackSongs = nextSongs;
    playbackArtists = nextArtists;
    shouldWrapPlaybackQueue = shouldWrap;
    currentIndex = -1;
    playPlaybackSong(startIndex);
}

function playAdjacent(direction) {
    if (!playbackSongs.length) {
        return;
    }

    let nextIndex = currentIndex < 0
        ? (direction > 0 ? -1 : 0)
        : currentIndex;

    for (let tries = 0; tries < playbackSongs.length; tries++) {
        nextIndex += direction;

        if (nextIndex < 0 || nextIndex >= playbackSongs.length) {
            if (!shouldWrapPlaybackQueue) {
                if (direction > 0) {
                    playFallbackQueue();
                }
                return;
            }

            nextIndex = (nextIndex + playbackSongs.length) % playbackSongs.length;
        }

        if (playbackSongs[nextIndex]?.song_archivo_ruta) {
            playPlaybackSong(nextIndex);
            return;
        }
    }
}

function playFallbackQueue() {
    const firstPlayable = songs.findIndex((song) => song.song_archivo_ruta);

    if (firstPlayable < 0) {
        return;
    }

    startPlaybackQueue(songs, artists, firstPlayable, true);
}

function shuffleSongs(sourceSongs) {
    const shuffledSongs = [...sourceSongs];

    for (let index = shuffledSongs.length - 1; index > 0; index--) {
        const randomIndex = secureRandomIndex(index + 1);
        [shuffledSongs[index], shuffledSongs[randomIndex]] = [shuffledSongs[randomIndex], shuffledSongs[index]];
    }

    return shuffledSongs;
}

function secureRandomIndex(maxExclusive) {
    if (window.crypto?.getRandomValues) {
        const randomValues = new Uint32Array(1);
        window.crypto.getRandomValues(randomValues);
        return randomValues[0] % maxExclusive;
    }

    return Math.floor(Math.random() * maxExclusive);
}

function updateProgress() {
    const audio = document.getElementById("audio-player");
    const progressRange = document.getElementById("progress-range");
    const currentTime = document.getElementById("current-time");
    const durationTime = document.getElementById("duration-time");

    currentTime.textContent = formatDuration(audio.currentTime);
    durationTime.textContent = formatDuration(audio.duration);
    progressRange.value = audio.duration ? String((audio.currentTime / audio.duration) * 100) : "0";
}

function preloadDurations() {
    songs.forEach((song, index) => {
        if (!song.song_archivo_ruta) {
            return;
        }

        const audio = new Audio(apiAssetUrl(song.song_archivo_ruta));
        audio.preload = "metadata";
        audio.addEventListener("loadedmetadata", () => {
            const duration = document.getElementById(`song-duration-${index}`);
            if (duration) {
                duration.textContent = formatDuration(audio.duration);
            }
        });
    });
}

function getArtistName(artistId, sourceArtists = artists) {
    return sourceArtists.find((artist) => artist.artista_id === artistId)?.artista_nombre ?? "Artista desconocido";
}

function formatDuration(seconds) {
    if (!Number.isFinite(seconds)) {
        return "0:00";
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60).toString().padStart(2, "0");
    return `${minutes}:${remainingSeconds}`;
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
