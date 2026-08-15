package com.patex.forever.entities;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookSequenceRepository extends CrudRepository<BookSequenceEntity, Long> {

    Optional<BookSequenceEntity> findBySequenceIdAndBookId(Long sequenceId, Long bookId);

    @Query("select max(bs.seqOrder) from BookSequenceEntity bs where bs.sequence.id = :sequenceId")
    Optional<Integer> findMaxSeqOrderBySequenceId(@Param("sequenceId") Long sequenceId);
}
