package com.patex.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
public class DuplicateCheckResponse  implements Serializable {
    private final long first;
    private final long second;
    private final String username;
}
