package org.comic_con.museum.fcb.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

public class User implements UserDetails {
    /**
     * The unique ID of the User in the database
     */
    private long uid;
    /**
     * The username of the User
     */
    private String username;
    /**
     * The token used to log in as this User for this request
     */
    private String token;
    /**
     * Whether or not this user is an administrator
     */
    private boolean admin;

    public User(int uid, String username, String token, boolean admin) {
        this.uid = uid;
        this.username = username;
        this.token = token;
        this.admin = admin;
    }

    public long getId() {
        return this.uid;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    @Override
    public String toString() {
        return String.format("User(%d, %s, *, %b)", this.uid, this.username, this.admin);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (admin) {
            return Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")
            );
        } else {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    /**
     * Get the token used in this request to login as this user.
     * @return The authentication token.
     */
    @Override
    public String getPassword() {
        return token;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
