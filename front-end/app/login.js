import { apiFetch } from "./api.js";

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
                const response = await apiFetch(
                    "/api/auth/login",
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

                const errorElement = document.getElementById("login-error");
                if (errorElement) {
                    errorElement.textContent = "";
                }

                if (response.ok) {
                    const body = await response.json();
                    if (body.token) {
                        localStorage.setItem('jwtToken', body.token);
                    }

                    console.log("Inicio de sesión exitoso");
                    window.location.href = "home.html";
                    return;
                }

                const errorText = await response.text();
                let message = errorText;
                try {
                    const json = JSON.parse(errorText);
                    if (json.error) {
                        message = json.error;
                    }
                } catch {
                    // No JSON body, use raw text.
                }

                console.error('Error:', response.status, message);
                if (errorElement) {
                    errorElement.textContent = message || `Usuario o contraseña incorrectos.`;
                } else {
                    alert(`Error: ${response.status} - ${message || response.statusText}`);
                }
            } catch (error) {
                console.error('Fetch error:', error);
                alert('Error al conectar con el servidor: ' + error.message);
            }
        }
    );
}
