package com.finances.dashboard.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
public class User extends DomainEntity {
    private String name;

    // Unique per active user, case-insensitive: enforced by the uq_users_email_active index (V4).
    private String email;

    private String profilePictureUrl;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
