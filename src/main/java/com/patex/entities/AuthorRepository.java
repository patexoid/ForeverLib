package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

/**
 * Created by Alexey on 12.03.2016.
 */
@org.springframework.stereotype.Repository
public interface  AuthorRepository  extends CrudRepository<Author, Long> {

    Page<Author> findAll(Pageable pageable);

}
