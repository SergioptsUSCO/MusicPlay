import { loadHomeView }
from "./views/homeView.js";

import { loadPlaylistView }
from "./views/playlistView.js";

import { loadAlbumView }
from "./views/albumView.js";

export function navigate(route){

    switch(route){

        case "playlist":

            loadPlaylistView(1);

            break;

        case "album":

            loadAlbumView();

            break;

        default:

            loadHomeView();
    }
}