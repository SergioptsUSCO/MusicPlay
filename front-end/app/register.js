const form = document.getElementById("register-form");

if (!form) {
    console.error('Formulario de registro no encontrado: id="register-form"');
} else {
    form.addEventListener(
"submit",
async (e) => {

    e.preventDefault();

    const data = {

        usuario_nombre:
        document.getElementById("username").value,

        usuario_correo:
        document.getElementById("email").value,

        usuario_contraseña:
        document.getElementById("password").value
    };

    const errorElement = document.getElementById("register-error");
    if (errorElement) {
        errorElement.textContent = "";
    }

    try {
        const response = await fetch(
            "http://localhost:8080/api/auth/register",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            }
        );

        if (response.ok) {
            alert("Usuario registrado");
            window.location.href = "login.html";
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

        if (errorElement) {
            errorElement.textContent = message || "Error al registrar al usuario.";
        } else {
            alert(message || "Error al registrar al usuario.");
        }
    } catch (error) {
        console.error('Fetch error:', error);
        if (errorElement) {
            errorElement.textContent = 'Error al conectar con el servidor: ' + error.message;
        } else {
            alert('Error al conectar con el servidor: ' + error.message);
        }
    }
}
    );
}