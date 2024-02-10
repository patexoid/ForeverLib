package com.patex.service;

import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorRepository;
import com.patex.entities.BookSequenceEntity;
import com.patex.entities.SequenceEntity;
import com.patex.entities.SequenceRepository;
import com.patex.mapper.AuthorMapper;
import com.patex.model.CheckDuplicateMessage;
import com.patex.zombie.LibException;
import com.patex.zombie.model.AggrResult;
import com.patex.zombie.model.Author;
import com.patex.zombie.model.SimpleBook;
import com.patex.zombie.service.AuthorService;
import com.patex.zombie.service.TransactionService;
import com.patex.zombie.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    public static final int MIN_AGGR_RESULT = 3;
    private final AuthorRepository authorRepository;
    private final AuthorMapper mapper;

    private final RabbitService rabbitService;

    private final TransactionService transactionService;

    private final SequenceRepository sequenceRepository;

    @Override
    public Author getAuthor(long id) {
        return authorRepository.findById(id).map(mapper::toDto).orElse(null);
    }


    @Secured(UserService.ADMIN_AUTHORITY)
    public Author mergeAuthors(UserDetails user, Long... ids) {
        if (ids.length < 2) {
            throw new LibException("Please choose at least 2 exsted authors");
        }
        Long resultId = transactionService.transactionRequired(() -> {
            List<AuthorEntity> authors = authorRepository.findByIdIn(ids);
            if (authors.isEmpty()) {
                throw new LibException("Can't merge not existed authors");
            }
            if (authors.size() < 2) {
                throw new LibException("Please choose at least 2 exsted authors");
            }
            AuthorEntity mainAuthor = authors.stream().max(Comparator.comparing(AuthorEntity::getName)).get();
            authors.stream().
                    filter(authorEntity -> authorEntity != mainAuthor).
                    map(AuthorEntity::getBooks).
                    forEach(books -> {
                        books.forEach(b -> {
                            b.setAuthor(mainAuthor);
                            mainAuthor.getBooks().add(b);
                        });
                        books.clear();
                    });
            mainAuthor.getSequences().stream().sorted(Comparator.comparing(SequenceEntity::getId)).
                    collect(Collectors.groupingBy(sequenceEntity -> sequenceEntity.getName().toLowerCase())).
                    values().stream().
                    filter(e -> e.size() > 1).forEach(
                            sequences -> {
                                SequenceEntity main = sequences.get(0);
                                for (int i = 1; i < sequences.size(); i++) {
                                    SequenceEntity secondary = sequences.get(i);
                                    for (BookSequenceEntity bookSequence : secondary.getBookSequences()) {
                                        bookSequence.setSequence(main);
                                        main.getBookSequences().add(bookSequence);
                                    }
                                    secondary.getBookSequences().clear();
                                    sequenceRepository.delete(secondary);
                                }
                            }
                    );
            authors.stream().filter(author -> author != mainAuthor).forEach(entity -> {
                authorRepository.deleteAuthorLang(entity.getId());
                authorRepository.delete(entity);
            });
            return mainAuthor.getId();
        });

        Optional<Author> author = authorRepository.findById(resultId).map(mapper::toDto);
        author.stream().map(Author::getBooks).flatMap(Collection::stream).map(SimpleBook::getId).
                map(id -> new CheckDuplicateMessage(id, user.getUsername())).forEach(rabbitService::checkDuplicate);
        assert author.isPresent();
        return author.get();
    }

    public List<String> getLanguages() {
        return authorRepository.getLanguages();
    }

    @Override
    public List<AggrResult> getAuthorsCount(String start, String lang) {
        int length = 0;
        List<String> oldPrefixes;
        List<String> newPrefixes = new ArrayList<>();
        List<AggrResult> authorsCount;
        do {
            oldPrefixes = newPrefixes;
            authorsCount = authorRepository.getAuthorsCount(start.length() + ++length, start, lang);
            newPrefixes = authorsCount.stream().map(AggrResult::getPrefix).collect(Collectors.toList());
        } while (authorsCount.size() < MIN_AGGR_RESULT && !newPrefixes.equals(oldPrefixes));
        return authorsCount;
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
