export function loadHomeView(){

    const content =
    document.getElementById(
        "content-view"
    );

    content.innerHTML = `

        <section class="top-grid">

            <div class="top-card">

                <img
                        src="https://picsum.photos/100">

                <span>
                    Mix diario 1
                </span>

            </div>

            <div class="top-card">

                <img
                        src="https://picsum.photos/101">

                <span>
                    Romeo Santos
                </span>

            </div>

            <div class="top-card">

                <img
                        src="https://picsum.photos/102">

                <span>
                    Descubrimiento semanal
                </span>

            </div>

            <div class="top-card">

                <img
                        src="https://picsum.photos/103">

                <span>
                    Bad Bunny Radio
                </span>

            </div>

        </section>

        <section class="music-section">

            <div class="section-header">

                <h2>
                    Hecho para ti
                </h2>

                <a href="#">
                    Mostrar todos
                </a>

            </div>

            <div class="row g-4">

                <div class="col-md-3">

                    <div class="music-card">

                        <img
                                src="https://picsum.photos/200">

                        <h5>
                            Mix diario 1
                        </h5>

                        <p>
                            Karol G y más
                        </p>

                    </div>

                </div>

                <div class="col-md-3">

                    <div class="music-card">

                        <img
                                src="https://picsum.photos/201">

                        <h5>
                            Mix diario 2
                        </h5>

                        <p>
                            Reik y más
                        </p>

                    </div>

                </div>

            </div>

        </section>

    `;
}