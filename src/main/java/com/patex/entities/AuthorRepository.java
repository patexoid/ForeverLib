package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * Created by Alexey on 12.03.2016.
 */
@Repository
public interface  AuthorRepository  extends CrudRepository<Author, Long> {

    Page<Author> findAll(Pageable pageable);

    Page<Author> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

    Author findByName(String name);
}
