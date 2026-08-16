package com.patex.forever.service;

import com.patex.forever.model.Sequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

public interface SequenceService {

    Sequence getSequence(long id);

    Sequence getSequenceSimplified(long id);

    Page<Sequence> getSequences(Pageable pageable, String prefix);

    Sequence renameSequence(long id, String name);

    void deleteSequence(long id);

    void setBookOrder(long id, long bookId, int seqOrder);

    @Transactional(propagation = MANDATORY, isolation = Isolation.SERIALIZABLE)
    Sequence mergeSequences(List<Sequence> sequences);
}
