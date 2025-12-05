package com.groupe.gestion_.de_.notes.security.Authentication;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String studentIdNum; 
    private String teacherIdNum;

    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles, String studentIdNum, String teacherIdNum) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.studentIdNum = studentIdNum;
        this.teacherIdNum = teacherIdNum;
    }
}