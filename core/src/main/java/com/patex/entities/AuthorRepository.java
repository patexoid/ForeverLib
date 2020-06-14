package com.patex.entities;

import com.patex.zombie.model.AggrResult;
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
public interface AuthorRepository extends CrudRepository<AuthorEntity, Long> {

    List<AuthorEntity> findByNameStartingWithIgnoreCaseOrderByName(String name);

    Optional<AuthorEntity> findFirstByNameIgnoreCase(String name);

    @Query(value = "SELECT " +
            "  substring(a.name, 1, :prefixLength) AS id, " +
            "  count(*)                            AS result " +
            "FROM Author a WHERE name LIKE :prefix% GROUP BY 1 ORDER BY 1",nativeQuery = true)
    List<AggrResult> getAuthorsCount(@Param("prefixLength")int length, @Param("prefix") String name);

    Page<AuthorEntity> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

    @Query("SELECT NEW com.patex.entities.AuthorEntity(a.id, a.name)" +
            " FROM AuthorEntity a where name like :prefix% order by name")
    Page<AuthorEntity> getAuthorsByName(Pageable pageable, @Param("prefix") String prefix);
}
