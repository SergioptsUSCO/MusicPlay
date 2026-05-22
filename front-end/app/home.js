import { loadHomeView }
from "./views/homeView.js";

import { loadPlaylistView }
from "./views/playlistView.js";

import { loadAlbumView }
from "./views/albumView.js";

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

loadHomeView();