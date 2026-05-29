export function loadPlaylistView(id) {

    const content =
    document.getElementById(
        "content-view"
    );

    content.innerHTML = `

        <div class="playlist-header">

            <img
                    src="https://picsum.photos/300">

            <div>

                <p>
                    Lista publica
                </p>

                <h1>
                    Music
                </h1>

                <span>
                    Sergio - 23 canciones
                </span>

            </div>

        </div>

        <div class="playlist-actions">

            <button class="play-main-btn">
                ▶
            </button>

        </div>

        <div class="songs-table">

            <div class="song-row header-row">

                <span>#</span>
                <span>Titulo</span>
                <span>Album</span>
                <span>Duracion</span>

            </div>

            <div class="song-row">

                <span>1</span>
                <span>Borro Cassette</span>
                <span>Pretty Boy</span>
                <span>3:27</span>

            </div>

            <div class="song-row">

                <span>2</span>
                <span>11 PM</span>
                <span>11:11</span>
                <span>2:55</span>

            </div>

        </div>

    `;
}
