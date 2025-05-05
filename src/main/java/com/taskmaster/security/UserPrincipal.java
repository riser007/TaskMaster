package com.taskmaster.security; // Updated package

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taskmaster.model.User; // Updated import
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
// import java.util.stream.Collectors; // Keep if implementing role mapping

public class UserPrincipal implements UserDetails {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String firstName, String lastName, String username, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        if (authorities == null || authorities.isEmpty()) {
            this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER")); // Default role
        } else {
            this.authorities = authorities;
        }
    }

    public static UserPrincipal create(User user) {
        // TODO: Implement proper role/authority mapping here if you add Roles
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    // --- UserDetails implementation ---
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; } // TODO: Implement if needed
    @Override public boolean isAccountNonLocked() { return true; } // TODO: Implement if needed
    @Override public boolean isCredentialsNonExpired() { return true; } // TODO: Implement if needed
    @Override public boolean isEnabled() { return true; } // TODO: Implement if needed

    // --- Equals and HashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}