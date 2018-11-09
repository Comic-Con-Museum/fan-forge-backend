package org.comic_con.museum.fcb.controllers.inputs;

public class LoginParams {
    private String username;
    private char[] password;
    
    public String getUsername() { return username; }
    public char[] getPassword() { return password; }
    
    public void setUsername(String username) { this.username = username; }
    public void setPassword(char[] password) { this.password = password; }
    
    public void zeroPassword() {
        for (int i = 0; i < password.length; ++i) {
            password[i] = 0;
        }
    }
}
