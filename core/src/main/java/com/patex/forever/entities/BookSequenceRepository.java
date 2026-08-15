package com.patex.forever.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookSequenceRepository extends CrudRepository<BookSequenceEntity, Long> {

    Optional<BookSequenceEntity> findBySequenceIdAndBookId(Long sequenceId, Long bookId);
}
