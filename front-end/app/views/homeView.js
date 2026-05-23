import { apiAssetUrl, apiFetch } from "../api.js";

let songs = [];
let artists = [];
let currentIndex = -1;

export async function loadHomeView(){

    const content = document.getElementById("content-view");

    content.innerHTML = `
        <section class="music-section songs-library">
            <div class="section-header">
                <h2>Canciones disponibles</h2>
                <span id="songs-count">Cargando...</span>
            </div>

            <div class="songs-list" id="songs-list">
                <div class="song-list-state">Cargando canciones...</div>
            </div>
        </section>
    `;

    setupPlayerControls();
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

        songs = await songsResponse.json();
        artists = artistsResponse.ok ? await artistsResponse.json() : [];

        count.textContent = `${songs.length} canciones`;
        renderSongs(list);
        preloadDurations();
    } catch (error) {
        list.innerHTML = `<div class="song-list-state">${error.message}</div>`;
        count.textContent = "Sin datos";
    }
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
                <span id="song-duration-${index}">${song.song_archivo_ruta ? "--:--" : "-"}</span>
            </button>
        `).join("")}
    `;

    list.querySelectorAll(".song-play-row").forEach((row) => {
        row.addEventListener("click", () => playSong(Number(row.dataset.songIndex)));
    });
}

function setupPlayerControls() {
    const audio = document.getElementById("audio-player");
    const playButton = document.getElementById("play-btn");
    const previousButton = document.getElementById("prev-btn");
    const nextButton = document.getElementById("next-btn");
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
                playSong(firstPlayable);
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

    volumeRange.addEventListener("input", () => {
        audio.volume = Number(volumeRange.value);
    });

    progressRange.addEventListener("input", () => {
        if (audio.duration) {
            audio.currentTime = (Number(progressRange.value) / 100) * audio.duration;
        }
    });

    audio.addEventListener("play", () => {
        playButton.textContent = "⏸";
    });

    audio.addEventListener("pause", () => {
        playButton.textContent = "▶";
    });

    audio.addEventListener("timeupdate", updateProgress);
    audio.addEventListener("loadedmetadata", updateProgress);
    audio.addEventListener("ended", () => playAdjacent(1));
}

function playSong(index) {
    const song = songs[index];
    if (!song || !song.song_archivo_ruta) {
        return;
    }

    const audio = document.getElementById("audio-player");
    currentIndex = index;
    audio.src = apiAssetUrl(song.song_archivo_ruta);
    audio.play();

    document.getElementById("player-cover").src = apiAssetUrl(song.song_portada_ruta);
    document.getElementById("player-title").textContent = song.song_nombre ?? "Sin nombre";
    document.getElementById("player-artist").textContent = getArtistName(song.song_artista);
    document.querySelectorAll(".song-play-row").forEach((row) => row.classList.remove("active"));
    document.querySelector(`[data-song-index="${index}"]`)?.classList.add("active");
}

function playAdjacent(direction) {
    if (!songs.length) {
        return;
    }

    let nextIndex = currentIndex < 0
        ? (direction > 0 ? -1 : 0)
        : currentIndex;

    for (let tries = 0; tries < songs.length; tries++) {
        nextIndex = (nextIndex + direction + songs.length) % songs.length;
        if (songs[nextIndex]?.song_archivo_ruta) {
            playSong(nextIndex);
            return;
        }
    }
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

function getArtistName(artistId) {
    return artists.find((artist) => artist.artista_id === artistId)?.artista_nombre ?? "Artista desconocido";
}

function formatDuration(seconds) {
    if (!Number.isFinite(seconds)) {
        return "0:00";
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = Math.floor(seconds % 60).toString().padStart(2, "0");
    return `${minutes}:${remainingSeconds}`;
}
