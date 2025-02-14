package com.patex.forever.service;

import com.patex.forever.entities.AuthorEntity;
import com.patex.forever.entities.AuthorRepository;
import com.patex.forever.entities.BookRepository;
import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.entities.SequenceRepository;
import com.patex.forever.mapper.AuthorBookDataMapper;
import com.patex.forever.mapper.AuthorMapper;
import com.patex.forever.model.AuthorBookData;
import com.patex.forever.model.AuthorDescription;
import com.patex.forever.model.Book;
import com.patex.forever.model.BookSequence;
import com.patex.forever.model.CheckDuplicateMessage;
import com.patex.forever.LibException;
import com.patex.forever.model.AggrResult;
import com.patex.forever.model.Author;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import com.patex.forever.model.SimpleBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    public static final int MIN_AGGR_RESULT = 3;
    private final AuthorRepository authorRepository;
    private final AuthorMapper mapper;
    private final AuthorBookDataMapper authorBookDataMapper;
    private final RabbitService rabbitService;

    private final TransactionService transactionService;

    private final SequenceRepository sequenceRepository;
    private final BookRepository bookRepository;

    @Override
    public Author getAuthor(long id) {
        return authorRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public AuthorDescription getAuthorDescription(long id) {
        return authorRepository.getAuthorDescription(id);
    }

    @Override
    public Author getAuthorSimplified(long id) {
        return authorRepository.findById(id).map(mapper::toListDto).
                map(author ->
                        {
                            List<AuthorBookData> authorBookData = bookRepository.getAuthorBookData(id);
                            Map<Long, Book> bookMap = new HashMap<>();
                            Map<Long, Sequence> sequenceMap = new HashMap<>();
                            Book currentBook = null;
                            Long currentSequenceId = null;
                            Integer currentSeqOrder = null;
                            for (AuthorBookData datum : authorBookData) {
                                if (currentBook == null || !currentBook.getId().equals(datum.getBookId())) {
                                    currentBook = authorBookDataMapper.getBookDto(datum);
                                    bookMap.put(currentBook.getId(), currentBook);
                                    currentSequenceId = null;
                                    currentSeqOrder = null;
                                }
                                if (datum.getSequenceId() != null &&
                                        (!datum.getSequenceId().equals(currentSequenceId) || !datum.getSeqOrder().equals(currentSeqOrder))) {
                                    currentSequenceId = datum.getSequenceId();
                                    currentSeqOrder = datum.getSeqOrder();
                                    currentBook.getSequences().add(authorBookDataMapper.getBookSequenceDto(datum));

                                    Sequence sequence = sequenceMap.computeIfAbsent(datum.getSequenceId(), k -> authorBookDataMapper.getSequenceDto(datum));
                                    SequenceBook sequenceBook = new SequenceBook();
                                    sequenceBook.setSeqOrder(datum.getSeqOrder());
                                    sequenceBook.setBook(currentBook);
                                    sequence.getBooks().add(sequenceBook);
                                }
                                if (datum.getAuthorId() != null && currentBook.getAuthors().stream()
                                        .noneMatch(a -> a.getId().equals(datum.getAuthorId()))) {
                                    currentBook.getAuthors().add(authorBookDataMapper.getAuthorDto(datum));
                                }
                            }
                            author.setBooksNoSequence(bookMap.values().stream()
                                    .filter(b -> b.getSequences().isEmpty()).sorted(Comparator.comparing(SimpleBook::getTitle))
                                    .toList());
                            sequenceMap.values().stream().map(Sequence::getBooks).forEach(c -> c.sort(Comparator.comparing(SequenceBook::getSeqOrder)));
                            author.setSequences(new ArrayList<>(sequenceMap.values()));
                            author.getSequences().sort(Comparator.comparing(Sequence::getName));

                            return author;
                        }

                ).orElse(null);
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
        log.info("prefix: {} lang:{} count: {}", start, lang, authorsCount.size());
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
