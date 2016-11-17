package com.patex.service;

import com.patex.entities.AggrResult;
import com.patex.entities.Author;
import com.patex.entities.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
public class AuthorService {

    @Autowired
    AuthorRepository authorRepository;

    public Author getAuthors(long id) {
        return authorRepository.findOne(id);
    }

    public Page<Author> findByName(String name, Pageable pageable) {
        return authorRepository.findByNameStartingWithIgnoreCase(name, pageable);
    }

    public List<AggrResult> getAuthorsCount(String start) {
        return authorRepository.getAuthorsCount(start.length() + 1, start);
    }

    public List<Author> findByName(String name) {
        return authorRepository.findByNameStartingWithIgnoreCaseOrderByName(name);
    }

    public Page<Author> getAuthors(Pageable pageable, String prefix) {
        prefix=prefix==null?"":prefix;
        return authorRepository.getAuthorsByName(pageable, prefix);
    }
}
