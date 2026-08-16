package com.patex.forever.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorBookRepository extends CrudRepository<AuthorBookEntity, Long> {

    Optional<AuthorBookEntity> findByBookIdAndAuthorId(Long bookId, Long authorId);

    long countByBookId(Long bookId);
}
