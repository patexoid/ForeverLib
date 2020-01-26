package com.patex.zombie.core.service;

import com.patex.zombie.core.entities.BookSequenceEntity;
import com.patex.zombie.core.entities.SequenceEntity;
import com.patex.zombie.core.entities.SequenceRepository;
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

    private final SequenceRepository sequenceRepository;

    private final  EntityManager entityManager;

    @Autowired
    public SequenceService(SequenceRepository sequenceRepository, EntityManager entityManager) {
        this.sequenceRepository = sequenceRepository;
        this.entityManager = entityManager;
    }

    public SequenceEntity getSequence(long id) {
        return sequenceRepository.findById(id).get();
    }

    @Transactional(propagation = MANDATORY, isolation = Isolation.SERIALIZABLE)
    public SequenceEntity mergeSequences(List<SequenceEntity> sequences) {
        SequenceEntity main = sequences.get(0);
        if (sequences.size() != 1) {
            sequences.forEach(entityManager::refresh);
            List<BookSequenceEntity> bookSequences = sequences.stream().
                    flatMap(s -> s.getBooks().stream()).collect(Collectors.toList());
            bookSequences.forEach(bs -> bs.setSequence(main));
            main.setBooks(bookSequences);
            sequenceRepository.save(main);
            sequences.stream().skip(1).forEach(s -> {
                s.setBooks(new ArrayList<>());
                sequenceRepository.delete(s);
            });

        }
        return main;
    }


}
