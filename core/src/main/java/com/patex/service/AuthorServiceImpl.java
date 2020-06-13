package com.patex.service;

import com.patex.entities.AuthorRepository;
import com.patex.mapper.AuthorMapper;
import com.patex.zombie.model.AggrResult;
import com.patex.zombie.model.Author;
import com.patex.zombie.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper mapper;

    @Override
    public Author getAuthor(long id) {
        return authorRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<AggrResult> getAuthorsCount(String start) {
        return authorRepository.getAuthorsCount(start.length() + 1, start);
    }

    @Override
    public List<Author> findByName(String name) {
        return authorRepository.findByNameStartingWithIgnoreCaseOrderByName(name).stream().map(mapper::toListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Author> findFirstByNameIgnoreCase(String name) {
        return authorRepository.findFirstByNameIgnoreCase(name).map(mapper::toDto);
    }

    @Override
    public Page<Author> getAuthor(Pageable pageable, String prefix) {
        prefix = prefix == null ? "" : prefix;
        return authorRepository.getAuthorsByName(pageable, prefix).map(mapper::toListDto);
    }

}
