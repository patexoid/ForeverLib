package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Alexey on 12.03.2016.
 */

@org.springframework.stereotype.Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    Page<Book> findAll(Pageable pageable);

}
