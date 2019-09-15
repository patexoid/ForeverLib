package com.patex.zombie.user.controller;

import lombok.Data;

import java.util.List;


@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private List<String> authorities;
    private String lang;

}
