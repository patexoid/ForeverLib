package com.patex.zombie.service;

import com.patex.zombie.model.AggrResult;
import com.patex.zombie.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Author getAuthor(long id);

    List<AggrResult> getAuthorsCount(String start);

    List<Author> findByName(String name);

    Optional<Author> findFirstByNameIgnoreCase(String name);

    Page<Author> getAuthor(Pageable pageable, String prefix);
}
