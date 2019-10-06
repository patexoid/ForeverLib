package com.patex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final User anonim = new User("anonimus", true, Collections.emptyList(), null);

    private String username;

    private boolean enabled;

    private List<String> authorities = new ArrayList<>();

    private String token;
}
