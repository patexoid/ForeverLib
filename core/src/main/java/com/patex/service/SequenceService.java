package com.patex.service;

import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.entities.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

/**
 * Created by potekhio on 15-Mar-16.
 */
@Service
public class SequenceService {

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private EntityManager entityManager;

    public Sequence getSequence(long id) {
        return sequenceRepository.findOne(id);
    }

    @Transactional(propagation = MANDATORY, isolation = Isolation.SERIALIZABLE)
    public Sequence mergeSequences(List<Sequence> sequences) {
        Sequence main = sequences.get(0);
        if (sequences.size() != 1) {
            sequences.forEach(s -> entityManager.refresh(s));
            List<BookSequence> bookSequences = sequences.stream().
                    flatMap(s -> s.getBookSequences().stream()).collect(Collectors.toList());
            bookSequences.forEach(bs -> bs.setSequence(main));
            main.setBookSequences(bookSequences);
            sequenceRepository.save(main);
            sequences.stream().skip(1).forEach(s -> {
                s.setBookSequences(new ArrayList<>());
                sequenceRepository.delete(s);
            });

        }
        return main;
    }


}
