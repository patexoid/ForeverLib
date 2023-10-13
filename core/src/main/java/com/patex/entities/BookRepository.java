package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface BookRepository extends org.springframework.data.repository.Repository<BookEntity, Long> {

    Page<BookEntity> findAll(Pageable pageable);

    @Query("""
            select b.id from BookEntity b where b.lang is null
                        """)
    Page<Long> findAllByLangIsNull(Pageable pageable);


    Optional<BookEntity> findFirstByTitleAndChecksum(String title, byte[] checksum);

    boolean existsByTitleAndChecksum(String title, byte[] checksum);

    Page<BookEntity> findAllByOrderByCreatedDesc(Pageable pageable);

    BookEntity save(BookEntity entity);

    Stream<BookEntity> findAll();

    Optional<BookEntity> findById(long id);

    @Query("""
            SELECT b from
             BookEntity b,
             AuthorBookEntity ab,
             AuthorBookEntity primaryAB
            where ab.book=b
             and ab.author=primaryAB.author
             and primaryAB.book.id=:bookId
             and b.duplicate = false
            """)
    Stream<BookEntity> findSameAuthorBook(@Param("bookId") long bookId);


    @Query("""
            SELECT b.id
            from
              BookEntity b inner join AuthorBookEntity ab on ab.book=b
            where
              b.duplicate=false
            order by
               ab.author.id
            """)
    List<Long> booksForDuplicateCheck();

    Stream<BookEntity> findByIdIn(Collection<Long> ids);
}
