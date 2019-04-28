package com.patex.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class DuplicateCheckRequest  implements Serializable {
    private final BookFileID bookFileID;

    private final List<BookFileID> other;

    private final String username;
}
