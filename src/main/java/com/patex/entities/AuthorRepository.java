package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Map;


/**
 * Created by Alexey on 12.03.2016.
 */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> {

    Page<Author> findAll(Pageable pageable);

    List<Author> findByNameStartingWithIgnoreCase(String name);

    List<Author> findByName(String name);

    @Query("SELECT NEW com.patex.entities.AggrResult(substring(a.name,0, LENGTH(?1)+1 ) as  id, count(*) as result) FROM Author a where name like '?1%' group by col_0_0_")//TODO FIX THAT
    List<AggrResult> getAuthorsCount(String name);

    Page<Author> findByNameStartingWithIgnoreCase(String name, Pageable pageable);


}
