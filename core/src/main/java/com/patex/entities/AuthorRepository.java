package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Created by Alexey on 12.03.2016.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> {

    Page<Author> findAllOrderByName(Pageable pageable);

    List<Author> findByNameStartingWithIgnoreCaseOrderByName(String name);

    Optional<Author> findFirstByNameIgnoreCase(String name);


    @Query(value = "SELECT " +
            "  substring(a.name, 0, :prefixLength) AS id, " +
            "  count(*)                            AS result " +
            "FROM Author a WHERE name LIKE :prefix% GROUP BY id ORDER BY id",nativeQuery = true)
    List<AggrResult> getAuthorsCount(@Param("prefixLength")int length, @Param("prefix") String name);

    Page<Author> findByNameStartingWithIgnoreCase(String name, Pageable pageable);


    @Query("SELECT NEW com.patex.entities.Author(a.id, a.name)" +
            " FROM Author a where name like :prefix% order by name")
    Page<Author> getAuthorsByName(Pageable pageable, @Param("prefix") String prefix);
}
