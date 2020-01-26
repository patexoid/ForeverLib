package com.patex.zombie.core.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface BookRepository extends CrudRepository<BookEntity, Long> {

    Page<BookEntity> findAll(Pageable pageable);

    Optional<BookEntity> findFirstByTitleAndChecksum(String title, byte[] checksum);

    Page<BookEntity> findAllByOrderByCreatedDesc(Pageable pageable);

}
