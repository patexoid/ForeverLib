package com.patex.service;

import com.patex.entities.AggrResult;
import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public AuthorEntity getAuthor(long id) {
        return authorRepository.findById(id).get();
    }

    public List<AggrResult> getAuthorsCount(String start) {
        return authorRepository.getAuthorsCount(start.length() + 1, start);
    }

    public List<AuthorEntity> findByName(String name) {
        return authorRepository.findByNameStartingWithIgnoreCaseOrderByName(name);
    }

    public Optional<AuthorEntity> findFirstByNameIgnoreCase(String name) {
        return authorRepository.findFirstByNameIgnoreCase(name);
    }

    public Page<AuthorEntity> getAuthor(Pageable pageable, String prefix) {
        prefix = prefix == null ? "" : prefix;
        return authorRepository.getAuthorsByName(pageable, prefix);
    }
}
