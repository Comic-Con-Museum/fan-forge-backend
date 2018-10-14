package org.comic_con.museum.fcb.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

//@Entity
public class User implements UserDetails {
    Logger LOG = LoggerFactory.getLogger("dao.user");
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final int uid;

    private final String username;

    // BCrypt hash is 60 characters long
    @Lob
    @Column(length = 60)
    private final byte[] password;

    @Column(name = "admin")
    private final boolean admin;

    public User(int uid, String username, byte[] password, boolean admin) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public int getId() {
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
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            return Collections.emptyList();
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
