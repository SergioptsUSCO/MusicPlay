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

    const response = await fetch(
        "http://localhost:8080/api/auth/register",
        {

            method: "POST",

            headers: {
                "Content-Type":
                "application/json"
            },

            body: JSON.stringify(data)
        }
    );

        if(response.ok){

            alert("Usuario registrado");

            window.location.href =
            "login.html";
        }
    }
    );
}