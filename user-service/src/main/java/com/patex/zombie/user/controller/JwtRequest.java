package com.patex.zombie.user.controller;


import lombok.Data;

@Data
public class JwtRequest {

    private String username;
    private String password;

}
