package com.patex.entities;

import com.patex.zombie.model.AggrResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            SELECT substring(a.name, 1, :prefixLength) AS prefix,
                   count(*)                            AS result
            FROM author a
                     inner join author_lang al on a.id = al.author_id
            WHERE name LIKE :prefix% and lang=:lang
            GROUP BY prefix
            ORDER BY prefix
                        """, nativeQuery = true)
    List<AggrResult> getAuthorsCount(@Param("prefixLength")int length, @Param("prefix") String name, @Param("lang")  String lang);

    @Query("SELECT NEW com.patex.entities.AuthorEntity(a.id, a.name)" +
            " FROM AuthorEntity a where name like :prefix% order by name")
    Page<AuthorEntity> getAuthorsByName(Pageable pageable, @Param("prefix") String prefix);

    @Query(nativeQuery = true,
    value = """
            insert into author_lang(author_id, lang) select distinct ab.author_id, COALESCE(b.lang, 'Unknown')
            from author_book ab
                     inner join book b on b.id = ab.book_id
            where ab.author_id in :ids

            """)
    void updateLang(@Param("ids") Collection<Long> ids);


    @Query(nativeQuery = true,value= """
            select distinct lang from author_lang order by lang
            """)
    List<String> getLanguages();
}
