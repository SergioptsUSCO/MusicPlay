import { apiFetch, getAuthToken, handleOAuthRedirect }
from "./api.js";

import { loadHomeView }
from "./views/homeView.js";

import { loadPlaylistView }
from "./views/playlistView.js";

import { loadAlbumView }
from "./views/albumView.js";

handleOAuthRedirect();

if (!getAuthToken()) {
    window.location.href = "login.html";
}

const logoutButton = document.getElementById("logout-btn");
const profileInitial = document.getElementById("profile-initial");

if (logoutButton) {
    logoutButton.addEventListener("click", () => {
        localStorage.removeItem("jwtToken");
        window.location.href = "login.html";
    });
}

async function loadCurrentUser() {
    try {
        const response = await apiFetch("/api/auth/me");

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem("jwtToken");
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            return;
        }

        const user = await response.json();
        const name = user.usuario_nombre || user.usuario_correo || "";
        const initial = name.trim().charAt(0).toUpperCase();

        if (profileInitial && initial) {
            profileInitial.textContent = initial;
            profileInitial.title = name;
        }
    } catch (error) {
        console.error("Error al cargar el usuario actual:", error);
    }
}

window.loadHomeView = function(){

    loadHomeView();
}

window.loadPlaylistView = function(id){

    loadPlaylistView(id);
}

window.loadAlbumView = function(){

    loadAlbumView();
}

window.toggleSidebar = function(){
    const sidebar = document.querySelector('.sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    const body = document.body;
    if (sidebar && backdrop) {
        const isOpen = !sidebar.classList.contains('mobile-open');
        sidebar.classList.toggle('mobile-open');
        backdrop.classList.toggle('visible', isOpen);
        body.classList.toggle('sidebar-open', isOpen);
    }
}

window.closeSidebar = function(){
    const sidebar = document.querySelector('.sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    const body = document.body;
    if (sidebar && backdrop) {
        sidebar.classList.remove('mobile-open');
        backdrop.classList.remove('visible');
        body.classList.remove('sidebar-open');
    }
}

loadCurrentUser();
loadHomeView();
