package com.mohamedMoslemani.kyc.dto;

import com.mohamedMoslemani.kyc.model.Role;

public class UserDTO {
    private Long id;
    private String username;
    private Role role;

    public UserDTO(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public Role getRole() {
        return role;
    }
}
