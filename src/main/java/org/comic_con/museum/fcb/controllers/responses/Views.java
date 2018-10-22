package org.comic_con.museum.fcb.controllers.responses;

import org.comic_con.museum.fcb.models.User;

public class Views {
    public static class Unauthed extends Views {}
    public static class Authed extends Unauthed{}
    public static class Admin extends Authed {}
    
    public static Class<? extends Views> byPrincipal(User user) {
        if (user == null) {
            return Unauthed.class;
        } else if (user.isAdmin()) {
            return Admin.class;
        } else {
            return Authed.class;
        }
    }
}
