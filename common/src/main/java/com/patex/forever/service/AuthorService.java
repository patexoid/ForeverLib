package com.patex.forever.service;

import com.patex.forever.model.AggrResult;
import com.patex.forever.model.Author;
import com.patex.forever.model.AuthorDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author getAuthor(long id);

    AuthorDescription getAuthorDescription(long id);

    Author getAuthorSimplified(long id);

    List<String> getLanguages();

    List<AggrResult> getAuthorsCount(String start, String lang);

    List<Author> findByName(String name);

    Optional<Author> findFirstByNameIgnoreCase(String name);

    Page<Author> getAuthor(Pageable pageable, String prefix);

    Author mergeAuthors(UserDetails user, Long... ids);

}
