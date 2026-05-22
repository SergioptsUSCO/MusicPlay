export function loadAlbumView(){

    const content =
    document.getElementById(
        "content-view"
    );

    content.innerHTML = `

        <div class="playlist-header">

            <img
                    src="https://picsum.photos/301">

            <div>

                <p>
                    Álbum
                </p>

                <h1>
                    Pretty Boy Dirty Boy
                </h1>

                <span>
                    Maluma
                </span>

            </div>

        </div>

    `;
}