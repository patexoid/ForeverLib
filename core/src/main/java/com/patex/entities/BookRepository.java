package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    Page<Book> findAll(Pageable pageable);

    Optional<Book> findFirstByTitleAndChecksum(String title, byte[] checksum);
}
