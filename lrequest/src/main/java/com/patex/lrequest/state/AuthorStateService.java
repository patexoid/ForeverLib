package com.patex.lrequest.state;

import com.patex.entities.Author;
import com.patex.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorStateService {

    private final AuthorService authorService;

    public Page<Author> getAuthor(Pageable pageable, String prefix) {
        return authorService.getAuthor(pageable, prefix);
    }
}
