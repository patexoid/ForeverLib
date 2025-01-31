package com.patex.forever.entities;

import com.patex.forever.model.AuthorBookData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SequenceRepository extends CrudRepository<SequenceEntity, Long> {

    Page<SequenceEntity> findAll(Pageable pageable);

    List<SequenceEntity> findAllByIdIn(Collection<Long> ids);

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
                     join library.book_sequence mainBS on b.id = mainBS.book_id
                     left join library.file_resource fr on b.cover_id = fr.id
                     left join library.book_sequence bs on b.id = bs.book_id
                     left join library.sequence s on bs.sequence_id = s.id
                     join library.author_book allab on b.id = allab.book_id
                     join library.author a on a.id = allab.author_id
            where mainBS.sequence_id = :sequenceId
            order by mainBS.seq_order,
                     b.id,
                     s.id,
                     a.id
            
            """)
    List<AuthorBookData> getSequenceBookData(@Param("sequenceId") long sequenceId);

}
