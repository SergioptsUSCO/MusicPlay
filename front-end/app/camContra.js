import { apiFetch } from "./api.js";

const emailForm = document.getElementById("emailForm");
const codeForm = document.getElementById("codeForm");
const passwordForm = document.getElementById("passwordForm");
const resendButton = document.getElementById("resend-code");
const backButtons = document.querySelectorAll("[data-back-login]");
const emailOutput = document.getElementById("email-output");
const requestEmailInput = document.getElementById("reset-email");
const codeInput = document.getElementById("verification-code");
const newPasswordInput = document.getElementById("new-password");
const confirmPasswordInput = document.getElementById("confirm-password");
const statusElement = document.getElementById("reset-status");

document.querySelectorAll("[data-toggle-password]").forEach((toggle) => {
    toggle.addEventListener("change", () => {
        const input = document.getElementById(toggle.dataset.togglePassword);
        if (input) {
            input.type = toggle.checked ? "text" : "password";
        }
    });
});

const steps = {
    email: document.getElementById("step-email"),
    code: document.getElementById("step-code"),
    password: document.getElementById("step-password")
};

let recoveryEmail = "";
let verifiedCode = "";
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;
const passwordHelpText = "La contrasena debe tener al menos 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial.";

function setStatus(message = "", type = "error") {
    if (!statusElement) {
        return;
    }

    statusElement.textContent = message;
    statusElement.dataset.type = type;
}

function setStep(stepName) {
    Object.entries(steps).forEach(([name, element]) => {
        if (element) {
            element.hidden = name !== stepName;
        }
    });
}

async function readApiMessage(response, fallback) {
    const text = await response.text();

    if (!text) {
        return fallback;
    }

    try {
        const body = JSON.parse(text);
        return body.message || body.error || fallback;
    } catch {
        return text || fallback;
    }
}

async function requestCode(email) {
    const response = await apiFetch("/api/auth/forgot-password", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ correo: email })
    });

    const message = await readApiMessage(response, "Revisa tu correo para continuar.");

    if (!response.ok) {
        throw new Error(message);
    }

    return message;
}

if (emailForm) {
    emailForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        setStatus("");

        const email = requestEmailInput.value.trim();

        if (!email) {
            setStatus("Ingresa tu correo electronico.");
            return;
        }

        try {
            await requestCode(email);
            recoveryEmail = email;
            verifiedCode = "";
            emailOutput.textContent = email;
            setStatus("Te enviamos un codigo de 6 digitos.", "success");
            setStep("code");
            codeInput.focus();
        } catch (error) {
            setStatus(error.message);
        }
    });
}

if (codeForm) {
    codeForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        setStatus("");

        const codigo = codeInput.value.trim();

        if (!/^\d{6}$/.test(codigo)) {
            setStatus("El codigo debe tener 6 digitos.");
            return;
        }

        try {
            const response = await apiFetch("/api/auth/verify-reset-code", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ correo: recoveryEmail, codigo })
            });

            const message = await readApiMessage(response, "Codigo verificado.");

            if (!response.ok) {
                throw new Error(message);
            }

            verifiedCode = codigo;
            setStatus("Codigo verificado. Ahora crea tu nueva contrasena.", "success");
            setStep("password");
            newPasswordInput.focus();
        } catch (error) {
            setStatus(error.message);
        }
    });
}

if (passwordForm) {
    passwordForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        setStatus("");

        const nuevaContrasena = newPasswordInput.value;
        const confirmacion = confirmPasswordInput.value;

        if (!passwordPattern.test(nuevaContrasena)) {
            setStatus(passwordHelpText);
            return;
        }

        if (nuevaContrasena !== confirmacion) {
            setStatus("Las contrasenas no coinciden.");
            return;
        }

        try {
            const response = await apiFetch("/api/auth/reset-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    correo: recoveryEmail,
                    codigo: verifiedCode,
                    nuevaContrasena
                })
            });

            const message = await readApiMessage(response, "Contrasena actualizada.");

            if (!response.ok) {
                throw new Error(message);
            }

            setStatus(`${message} Redirigiendo al login...`, "success");
            setTimeout(() => {
                window.location.href = "login.html";
            }, 1600);
        } catch (error) {
            setStatus(error.message);
        }
    });
}

if (resendButton) {
    resendButton.addEventListener("click", async () => {
        if (!recoveryEmail) {
            setStep("email");
            return;
        }

        try {
            await requestCode(recoveryEmail);
            verifiedCode = "";
            codeInput.value = "";
            setStatus("Codigo reenviado.", "success");
        } catch (error) {
            setStatus(error.message);
        }
    });
}

backButtons.forEach((button) => {
    button.addEventListener("click", () => {
        window.location.href = "login.html";
    });
});
