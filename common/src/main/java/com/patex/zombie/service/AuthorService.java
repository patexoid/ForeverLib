package com.patex.zombie.service;

import com.patex.zombie.model.AggrResult;
import com.patex.zombie.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author getAuthor(long id);

    List<String> getLanguages();

    List<AggrResult> getAuthorsCount(String start, String lang);

    List<Author> findByName(String name);

    Optional<Author> findFirstByNameIgnoreCase(String name);

    Page<Author> getAuthor(Pageable pageable, String prefix);

    Author mergeAuthors(UserDetails user, Long... ids);

}
