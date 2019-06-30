package com.patex.lrequest.state;

import com.patex.lrequest.FindAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitialState {

    private final AuthorStateService authorStateService;

    public AuthorsState toState(FindAuthor findAuthor) {
        return new AuthorsState(authorStateService, findAuthor.getAuthorName());
    }
}
