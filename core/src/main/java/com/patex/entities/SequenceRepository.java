package com.patex.entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SequenceRepository extends CrudRepository<SequenceEntity, Long> {

    Page<SequenceEntity> findAll(Pageable pageable);

    List<SequenceEntity> findAllByIdIn(Collection<Long> ids);
}
