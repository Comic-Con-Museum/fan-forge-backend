package org.comic_con.museum.fcb.controllers.inputs;

public class LoginParams {
    public String username;
    // char[] so we can zero it out
    public char[] password;

    public void setUsername(String username) { this.username = username; }
    public void setPassword(char[] password) { this.password = password; }
}