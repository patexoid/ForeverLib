package com.patex.service;

import com.patex.entities.BookSequenceEntity;
import com.patex.entities.SequenceEntity;
import com.patex.entities.SequenceRepository;
import com.patex.mapper.SequenceMapper;
import com.patex.zombie.model.Sequence;
import com.patex.zombie.service.SequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
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


    @Override
    public Sequence getSequence(long id) {
        return sequenceRepository.findById(id).map(mapper::toDto).get();
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
