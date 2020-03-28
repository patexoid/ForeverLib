package com.patex.zombie.service;

import com.patex.zombie.model.Sequence;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public interface SequenceService {
    Sequence getSequence(long id);

    @Transactional(propagation = MANDATORY, isolation = Isolation.SERIALIZABLE)
    Sequence mergeSequences(List<Sequence> sequences);
}
