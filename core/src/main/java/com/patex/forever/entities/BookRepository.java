package com.patex.forever.entities;

import com.patex.forever.model.AuthorBookData;
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

    @Query(nativeQuery = true,
    value = """
            select b.id         bookId,
                   b.title      bookTitle,
                   b.duplicate  bookDuplicate,
                   b.descr      bookDescr,
                   b.created    bookCreated,
                   fr.type      coverType,
                   fr.id        coverId,
                   s.id         sequenceId,
                   bs.seq_order seqOrder,
                   s.name       sequenceName,
                   a.id         authorId,
                   a.name       authorName
            from library.book b
                     join library.author_book ab on b.id = ab.book_id
                     left join library.file_resource fr on b.cover_id = fr.id
                     left join library.book_sequence bs on b.id = bs.book_id
                     left join library.sequence s on bs.sequence_id = s.id
                     join library.author_book allab on b.id = allab.book_id
                     join library.author a on a.id = allab.author_id
            where ab.author_id = :authorId
            order by b.title,
                     b.id,
                     s.id,
                     a.id
            
            """)
    List<AuthorBookData> getAuthorBookData(@Param("authorId") long authorId);
}
