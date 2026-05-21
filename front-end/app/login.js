const form = document.getElementById("loginForm");

if (!form) {
    console.error('Formulario de login no encontrado: id="loginForm"');
} else {
    form.addEventListener(
        "submit",
        async (e) => {
            e.preventDefault();

            const data = {
                usuario_correo:
                    document.getElementById("email").value,

                usuario_contraseña:
                    document.getElementById("password").value
            };

            try {
                const response = await fetch(
                    "http://localhost:8080/api/auth/login",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify(data)
                    }
                );

                console.log('Status:', response.status);

                if (response.ok) {
                    const body = await response.json();
                    if (body.token) {
                        localStorage.setItem('jwtToken', body.token);
                    }

                    alert("Bienvenido");

                    window.location.href =
                        "home.html";
                } else {
                    const errorText = await response.text();
                    console.error('Error:', response.status, errorText);
                    alert(`Error: ${response.status} - ${errorText || response.statusText}`);
                }
            } catch (error) {
                console.error('Fetch error:', error);
                alert('Error al conectar con el servidor: ' + error.message);
            }
        }
    );
}