package com.patex.forever.entities;

import com.patex.forever.model.AggrResult;
import com.patex.forever.model.AuthorDescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Created by Alexey on 12.03.2016.
 */
@Repository
public interface AuthorRepository extends CrudRepository<AuthorEntity, Long> {

    List<AuthorEntity> findByNameStartingWithIgnoreCaseOrderByName(String name);

    Optional<AuthorEntity> findFirstByNameIgnoreCase(String name);

    @Query(value = """
            SELECT lower(substring(a.name, 1, :prefixLength)) AS prefix,
                   count(*)                            AS result
            FROM author a
                     inner join author_lang al on a.id = al.author_id
              WHERE name ILIKE :prefix% and lang=:lang
            GROUP BY prefix
            ORDER BY prefix
            """, nativeQuery = true)
    List<AggrResult> getAuthorsCount(@Param("prefixLength")int length, @Param("prefix") String name, @Param("lang")  String lang);

    @Query("SELECT NEW com.patex.forever.entities.AuthorEntity(a.id, a.name)" +
            " FROM AuthorEntity a where name ilike :prefix% order by name")
    Page<AuthorEntity> getAuthorsByName(Pageable pageable, @Param("prefix") String prefix);

    @Query(nativeQuery = true,
    value = """
            delete from author_lang where author_id in :ids ;
            insert into author_lang(author_id, lang) select distinct ab.author_id, b.lang
            from author_book ab
                     inner join book b on b.id = ab.book_id
            where ab.author_id in :ids and
            b.lang is not null
            """)
    @Modifying
    void updateLang(@Param("ids") Collection<Long> ids);


    @SuppressWarnings("SqlWithoutWhere")
    @Query(nativeQuery = true,
            value = """
                    delete from author_lang;
                    insert into author_lang(author_id, lang) select distinct ab.author_id, b.lang
                    from author_book ab
                             inner join book b on b.id = ab.book_id
                             where b.lang is not null
                    on conflict do nothing
                    """)
    @Modifying
    void updateLang();

    @Query(nativeQuery = true,value= """
            select distinct lang from author_lang order by lang
            """)
    List<String> getLanguages();

    @Modifying
    @Query(nativeQuery = true,value= """
            delete from author_lang where author_id=:id
            """)
    void deleteAuthorLang(@Param("id") Long id);

    List<AuthorEntity> findByIdIn(Long... ids);


    @Query(nativeQuery = true, value = """
            SELECT
                a.id,
                a.descr,
                a.name,
                a.updated,
                COUNT(DISTINCT b.id) AS bookCount,
                COUNT(DISTINCT s.id) AS sequenceCount,
                COUNT(DISTINCT CASE WHEN s.id IS NOT NULL THEN b.id END) AS sequenceBookCount,
                MAX(CASE WHEN s.id IS NOT NULL THEN b.created END) AS sequenceUpdated,
                COUNT(DISTINCT CASE WHEN s.id IS NULL THEN b.id END) AS noSequenceBookCount,
                MAX(CASE WHEN s.id IS NULL THEN b.created END) AS noSequenceUpdated
            FROM author a
                     JOIN author_book ab ON a.id = ab.author_id
                     JOIN book b ON ab.book_id = b.id
                     LEFT JOIN book_sequence bs ON b.id = bs.book_id
                     LEFT JOIN sequence s ON bs.sequence_id = s.id
            WHERE a.id = :id
            GROUP BY a.id, a.descr, a.name, a.updated;
            """)
    AuthorDescription getAuthorDescription(@Param("id") long id);
}
