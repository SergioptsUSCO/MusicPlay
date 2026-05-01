fetch("http://localhost:8080/api/canciones")
  .then(res => res.json())
  .then(data => {

    const cards = document.querySelectorAll(".col");

    cards.forEach((card, index) => {
      const cancion = data[index];

      if (cancion) {
        card.querySelector(".nombre").textContent = cancion.song_nombre;
        card.querySelector(".artista").textContent = cancion.song_artista;
      }
    });

  });