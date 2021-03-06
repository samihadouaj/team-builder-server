package com.nexters.teambuilder.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nexters.teambuilder.session.domain.SessionUser;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "ux_user_id", columnNames = "id")})
public class User implements UserDetails {
    public enum Role{
        ROLE_ADMIN, ROLE_USER
    }

    public enum Position{
        DESIGNER, DEVELOPER
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(191)", name = "uuid", unique = true, nullable = false)
    private String uuid;

    private String id;

    private String password;

    private String name;

    private Integer nextersNumber;

    @Column(length = 100)
    private String email;

    private boolean activated;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Position position;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<SessionUser> sessionUsers = new HashSet<>();

    private int voteCount;

    private boolean voted;

    private boolean submitIdea;

    private boolean hasTeam;

    private boolean dissmissed;

    @Builder
    public User(String id, String password, String name, Integer nextersNumber, Role role,
                Position position, String email) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.nextersNumber = nextersNumber;
        this.role = role;
        this.position = position;
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePosition(Position position) {
        this.position = position;
    }

    @JsonIgnore
    private boolean authenticated = false;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }


    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return uuid;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void activate() {
        this.activated = true;
    }

    public void deactivate() {
        this.activated = false;
        this.hasTeam = false;
        this.submitIdea = false;
        this.voted = false;
        this.voteCount = 0;
    }

    public void updateHasTeam(boolean hasTeam) {
        this.hasTeam = hasTeam;
    }

    public void updateSubmitIdea(boolean submitIdea) {
        this.submitIdea = submitIdea;
    }

    public void updateVoted(boolean voted) {
        this.voted = voted;
    }

    public void updateVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void dismiss(long dissmissNumber) {
        this.dissmissed = true;
        this.name = "????????? ??????";
        this.id = "????????????" + String.format("%03d", dissmissNumber);
    }
}
