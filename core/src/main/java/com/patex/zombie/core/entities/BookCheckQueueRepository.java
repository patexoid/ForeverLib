package com.patex.zombie.core.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface BookCheckQueueRepository extends CrudRepository<BookCheckQueue, Long> {


    Page<BookCheckQueue> findAllByIdGreaterThanOrderByIdAsc(Pageable pageable, Long id);

    BookCheckQueue saveAndFlush(BookCheckQueue bcq);
}
