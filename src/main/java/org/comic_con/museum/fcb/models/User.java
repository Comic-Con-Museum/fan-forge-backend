package org.comic_con.museum.fcb.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class User implements UserDetails {
    private long uid;
    private String username;
    // we don't worry about zeroing this out because it only ever contains the password hash
    private byte[] password;
    private boolean admin;

    public User(int uid, String username, byte[] password, boolean admin) {
        this.uid = uid;
        this.username = username;
        this.password = password;
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
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    public String getPassword() {
        return new String(password, StandardCharsets.ISO_8859_1);
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
