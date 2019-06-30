package com.patex.lrequest.state;

import com.patex.entities.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AuthorsState {

    public static final int PAGE_SIZE = 20;
    private final AuthorStateService authorStateService;
    private final String filter;
    private final Page<Author> authors;

    private AuthorsState(AuthorStateService authorStateService, String filter, Pageable pageable) {
        this.authorStateService = authorStateService;
        this.filter = filter;
        authors = authorStateService.getAuthor(pageable, filter);

    }
    public AuthorsState(AuthorStateService authorStateService, String filter) {
        this.authorStateService = authorStateService;
        this.authors = authorStateService.getAuthor(PageRequest.of(0, PAGE_SIZE), filter);
        this.filter = filter;
    }

    //@Transition
    private AuthorsState nextPage() {
        return new AuthorsState(authorStateService, filter, authors.nextPageable());
    }
}
