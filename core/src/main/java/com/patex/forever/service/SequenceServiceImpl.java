package com.patex.forever.service;

import com.patex.forever.entities.BookSequenceEntity;
import com.patex.forever.entities.SequenceEntity;
import com.patex.forever.entities.SequenceRepository;
import com.patex.forever.mapper.AuthorBookDataMapper;
import com.patex.forever.mapper.SequenceMapper;
import com.patex.forever.model.AuthorBookData;
import com.patex.forever.model.Book;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import com.patex.forever.model.SimpleBook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {

    private final SequenceRepository sequenceRepository;

    private final SequenceMapper mapper;
    private final AuthorBookDataMapper authorBookDataMapper;


    @Override
    public Sequence getSequence(long id) {
        return sequenceRepository.findById(id).map(mapper::toDto).get();
    }

    @Override
    public Sequence getSequenceSimplified(long id) {
        return sequenceRepository.findById(id).map(mapper::toListDto).map(sequence -> {
            List<AuthorBookData> sequenceBookData = sequenceRepository.getSequenceBookData(id);
            Map<Long, Book> bookMap = new HashMap<>();
            Book currentBook = null;
            Long currentSequenceId = null;
            Integer currentSeqOrder = null;
            for (AuthorBookData datum : sequenceBookData) {
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
                }
                if (datum.getAuthorId() != null && currentBook.getAuthors().stream()
                        .noneMatch(a -> a.getId().equals(datum.getAuthorId()))) {
                    currentBook.getAuthors().add(authorBookDataMapper.getAuthorDto(datum));
                }
            }
            //noinspection OptionalGetWithoutIsPresent always present
            sequence.setBooks(bookMap.values().stream().
                    map(b -> new SequenceBook(b.getSequences().stream().
                            filter(bs -> bs.getId() == id).
                            findFirst().get().getSeqOrder(),
                            b)).
                    sorted(Comparator.comparing(SequenceBook::getSeqOrder)).
                    toList());
            return sequence;
        }).orElse(null);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = Isolation.SERIALIZABLE)
    public Sequence mergeSequences(List<Sequence> sequences) {
        List<SequenceEntity> sequenceEntities = sequenceRepository.
                findAllByIdIn(sequences.stream().map(Sequence::getId).collect(Collectors.toList()));

        SequenceEntity main = sequenceEntities.get(0);
        if (sequenceEntities.size() != 1) {
            List<BookSequenceEntity> bookSequences = sequenceEntities.stream().
                    flatMap(s -> s.getBookSequences().stream()).collect(Collectors.toList());
            bookSequences.forEach(bs -> bs.setSequence(main));
            main.setBookSequences(bookSequences);
            sequenceRepository.save(main);
            sequenceEntities.stream().skip(1).forEach(s -> {
                s.setBookSequences(new ArrayList<>());
                sequenceRepository.delete(s);
            });

        }
        return mapper.toDto(main);
    }


}
